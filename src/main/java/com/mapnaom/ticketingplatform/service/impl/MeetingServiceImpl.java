package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemRequest;
import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingCreateRequest;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteRequest;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingUpdateRequest;
import com.mapnaom.ticketingplatform.exception.MeetingConflictException;
import com.mapnaom.ticketingplatform.mapper.MeetingMapper;
import com.mapnaom.ticketingplatform.model.AgendaItem;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.MeetingNote;
import com.mapnaom.ticketingplatform.model.MeetingParticipant;
import com.mapnaom.ticketingplatform.model.Team;
import com.mapnaom.ticketingplatform.model.enums.MeetingStatus;
import com.mapnaom.ticketingplatform.model.enums.RsvpStatus;
import com.mapnaom.ticketingplatform.repository.AgendaItemRepository;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.MeetingNoteRepository;
import com.mapnaom.ticketingplatform.repository.MeetingParticipantRepository;
import com.mapnaom.ticketingplatform.repository.MeetingRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.repository.TeamRepository;
import com.mapnaom.ticketingplatform.service.MeetingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingServiceImpl implements MeetingService {
    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository membershipRepository;
    private final AppUserRepository userRepository;
    private final MeetingParticipantRepository participantRepository;
    private final AgendaItemRepository agendaItemRepository;
    private final MeetingNoteRepository noteRepository;
    private final MeetingMapper meetingMapper;

    @Override
    public MeetingResponse createMeeting(MeetingCreateRequest request) {
        validateTimeRange(request.startTime(), request.endTime());
        Team team = findActiveTeam(request.teamId());
        AppUser organizer = findActiveUser(request.organizerId());
        requireTeamMember(team.getId(), organizer.getId());
        ensureNoConflict(team.getId(), request.startTime(), request.endTime(), null);

        Meeting meeting = new Meeting();
        meeting.setTitle(request.title().trim());
        meeting.setDescription(request.description());
        meeting.setTeam(team);
        meeting.setOrganizer(organizer);
        meeting.setStartTime(request.startTime());
        meeting.setEndTime(request.endTime());
        meeting.setLocation(request.location());
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meetingRepository.save(meeting);

        Set<Long> participantIds = new LinkedHashSet<>();
        participantIds.add(organizer.getId());
        if (request.participantUserIds() != null) {
            participantIds.addAll(request.participantUserIds());
        }
        addParticipantsInternal(meeting, participantIds, organizer.getId());
        return meetingMapper.toResponse(meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponse getMeeting(Long meetingId) {
        return meetingMapper.toResponse(findActiveMeeting(meetingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeetingResponse> getMeetingsForTeam(Long teamId, Pageable pageable) {
        findActiveTeam(teamId);
        return meetingRepository.findByTeamIdAndActiveTrue(teamId, pageable).map(meetingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> getUpcomingMeetingsForUser(Long userId, LocalDateTime from, LocalDateTime to) {
        findActiveUser(userId);
        validateTimeRange(from, to);
        return meetingRepository.findUpcomingForUser(userId, from, to).stream()
                .map(meetingMapper::toResponse)
                .toList();
    }

    @Override
    public MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request) {
        Meeting meeting = findActiveMeeting(meetingId);
        requireEditable(meeting);

        LocalDateTime startTime = request.startTime() == null ? meeting.getStartTime() : request.startTime();
        LocalDateTime endTime = request.endTime() == null ? meeting.getEndTime() : request.endTime();
        validateTimeRange(startTime, endTime);
        ensureNoConflict(meeting.getTeam().getId(), startTime, endTime, meetingId);

        if (request.title() != null) meeting.setTitle(request.title().trim());
        if (request.description() != null) meeting.setDescription(request.description());
        if (request.startTime() != null) meeting.setStartTime(request.startTime());
        if (request.endTime() != null) meeting.setEndTime(request.endTime());
        if (request.location() != null) meeting.setLocation(request.location());
        if (request.status() != null) meeting.setStatus(request.status());
        return meetingMapper.toResponse(meetingRepository.save(meeting));
    }

    @Override
    public void cancelMeeting(Long meetingId) {
        Meeting meeting = findActiveMeeting(meetingId);
        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            throw new IllegalStateException("A completed meeting cannot be cancelled");
        }
        meeting.setStatus(MeetingStatus.CANCELLED);
        meetingRepository.save(meeting);
    }

    @Override
    public void deleteMeeting(Long meetingId) {
        Meeting meeting = findActiveMeeting(meetingId);
        meeting.setActive(false);
        meetingRepository.save(meeting);
    }

    @Override
    public void addParticipants(Long meetingId, List<Long> userIds) {
        Meeting meeting = findActiveMeeting(meetingId);
        requireEditable(meeting);
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("At least one participant user id is required");
        }
        addParticipantsInternal(meeting, new LinkedHashSet<>(userIds), null);
    }

    @Override
    public void respondToInvite(Long meetingId, Long userId, RsvpStatus response) {
        Meeting meeting = findActiveMeeting(meetingId);
        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot respond to a cancelled meeting");
        }
        MeetingParticipant participant = findParticipant(meetingId, userId);
        participant.setRsvpStatus(response);
        participantRepository.save(participant);
    }

    @Override
    public void markAttendance(Long meetingId, Long userId, boolean attended) {
        Meeting meeting = findActiveMeeting(meetingId);
        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot record attendance for a cancelled meeting");
        }
        MeetingParticipant participant = findParticipant(meetingId, userId);
        participant.setAttended(attended);
        participantRepository.save(participant);
    }

    @Override
    public AgendaItemResponse addAgendaItem(Long meetingId, AgendaItemRequest request) {
        Meeting meeting = findActiveMeeting(meetingId);
        requireEditable(meeting);
        AgendaItem item = new AgendaItem();
        item.setMeeting(meeting);
        item.setTopic(request.topic().trim());
        item.setDescription(request.description());
        item.setDisplayOrder(request.displayOrder() == null
                ? Math.toIntExact(agendaItemRepository.countByMeetingId(meetingId))
                : request.displayOrder());
        item.setDurationMinutes(request.durationMinutes());
        if (request.presenterId() != null) {
            requireTeamMember(meeting.getTeam().getId(), request.presenterId());
            item.setPresenter(findActiveUser(request.presenterId()));
        }
        agendaItemRepository.save(item);
        meeting.getAgendaItems().add(item);
        return meetingMapper.toAgendaResponse(item);
    }

    @Override
    public void reorderAgenda(Long meetingId, List<Long> orderedAgendaItemIds) {
        Meeting meeting = findActiveMeeting(meetingId);
        requireEditable(meeting);
        List<AgendaItem> agendaItems = agendaItemRepository.findByMeetingIdOrderByDisplayOrderAsc(meetingId);
        if (orderedAgendaItemIds == null
                || orderedAgendaItemIds.size() != agendaItems.size()
                || new LinkedHashSet<>(orderedAgendaItemIds).size() != agendaItems.size()) {
            throw new IllegalArgumentException("Ordered agenda ids must contain every agenda item exactly once");
        }
        var itemsById = agendaItems.stream().collect(java.util.stream.Collectors.toMap(AgendaItem::getId, item -> item));
        for (int index = 0; index < orderedAgendaItemIds.size(); index++) {
            AgendaItem item = itemsById.get(orderedAgendaItemIds.get(index));
            if (item == null) {
                throw new IllegalArgumentException("Agenda item does not belong to meeting: " + orderedAgendaItemIds.get(index));
            }
            item.setDisplayOrder(index);
        }
        agendaItemRepository.saveAll(agendaItems);
    }

    @Override
    public MeetingNoteResponse addNote(Long meetingId, MeetingNoteRequest request) {
        Meeting meeting = findActiveMeeting(meetingId);
        AppUser author = findActiveUser(request.authorId());
        requireTeamMember(meeting.getTeam().getId(), author.getId());
        MeetingNote note = new MeetingNote();
        note.setMeeting(meeting);
        note.setAuthor(author);
        note.setContent(request.content().trim());
        note.setType(request.type());
        return meetingMapper.toNoteResponse(noteRepository.save(note));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingNoteResponse> getNotes(Long meetingId) {
        findActiveMeeting(meetingId);
        return noteRepository.findByMeetingIdOrderByCreatedAtAsc(meetingId).stream()
                .map(meetingMapper::toNoteResponse)
                .toList();
    }

    private void addParticipantsInternal(Meeting meeting, Set<Long> userIds, Long organizerId) {
        for (Long userId : userIds) {
            if (userId == null || participantRepository.existsByMeetingIdAndUserId(meeting.getId(), userId)) {
                continue;
            }
            requireTeamMember(meeting.getTeam().getId(), userId);
            AppUser user = findActiveUser(userId);
            MeetingParticipant participant = new MeetingParticipant();
            participant.setMeeting(meeting);
            participant.setUser(user);
            participant.setRsvpStatus(userId.equals(organizerId) ? RsvpStatus.ACCEPTED : RsvpStatus.PENDING);
            participantRepository.save(participant);
            meeting.getParticipants().add(participant);
        }
    }

    private Meeting findActiveMeeting(Long meetingId) {
        return meetingRepository.findByIdAndActiveTrue(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Active meeting not found with id: " + meetingId));
    }

    private Team findActiveTeam(Long teamId) {
        return teamRepository.findByIdAndActiveTrue(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Active team not found with id: " + teamId));
    }

    private AppUser findActiveUser(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new EntityNotFoundException("Active user not found with id: " + userId);
        }
        return user;
    }

    private MeetingParticipant findParticipant(Long meetingId, Long userId) {
        return participantRepository.findByMeetingIdAndUserId(meetingId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Meeting participant not found for meeting " + meetingId + " and user " + userId));
    }

    private void requireTeamMember(Long teamId, Long userId) {
        if (!membershipRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("User " + userId + " is not a member of team " + teamId);
        }
    }

    private void ensureNoConflict(Long teamId, LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        if (meetingRepository.existsOverlappingMeeting(
                teamId, startTime, endTime, excludeId, MeetingStatus.CANCELLED)) {
            throw new MeetingConflictException("The team already has a meeting in the requested time range");
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Meeting end time must be after start time");
        }
    }

    private void requireEditable(Meeting meeting) {
        if (meeting.getStatus() == MeetingStatus.CANCELLED || meeting.getStatus() == MeetingStatus.COMPLETED) {
            throw new IllegalStateException("Completed or cancelled meetings cannot be modified");
        }
    }
}
