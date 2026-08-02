package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.meeting.MeetingCreateRequest;
import com.mapnaom.ticketingplatform.exception.MeetingConflictException;
import com.mapnaom.ticketingplatform.mapper.MeetingMapper;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.MeetingParticipant;
import com.mapnaom.ticketingplatform.model.Team;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.enums.MeetingStatus;
import com.mapnaom.ticketingplatform.model.enums.RsvpStatus;
import com.mapnaom.ticketingplatform.repository.AgendaItemRepository;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.MeetingNoteRepository;
import com.mapnaom.ticketingplatform.repository.MeetingParticipantRepository;
import com.mapnaom.ticketingplatform.repository.MeetingRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.repository.TeamRepository;
import com.mapnaom.ticketingplatform.service.AccessChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {
    @Mock private MeetingRepository meetingRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TeamMembershipRepository membershipRepository;
    @Mock private AppUserRepository userRepository;
    @Mock private MeetingParticipantRepository participantRepository;
    @Mock private AgendaItemRepository agendaItemRepository;
    @Mock private MeetingNoteRepository noteRepository;
    @Mock private AccessChecker access;

    private MeetingServiceImpl service;
    private Team team;
    private TeamMember organizer;
    private TeamMember invitee;

    @BeforeEach
    void setUp() {
        service = new MeetingServiceImpl(
                meetingRepository,
                teamRepository,
                membershipRepository,
                userRepository,
                participantRepository,
                agendaItemRepository,
                noteRepository,
                new MeetingMapper(),
                access);

        team = new Team();
        team.setId(10L);
        team.setName("Platform");

        organizer = user(20L, "organizer");
        invitee = user(30L, "invitee");
        when(access.hasAllScope("MEETING")).thenReturn(true);
    }

    @Test
    void createMeetingAddsOrganizerAndInviteesWithExpectedRsvp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        MeetingCreateRequest request = new MeetingCreateRequest(
                "Weekly sync", null, team.getId(), organizer.getId(), start, end, "Room 1", List.of(invitee.getId()));

        when(teamRepository.findByIdAndActiveTrue(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(userRepository.findById(invitee.getId())).thenReturn(Optional.of(invitee));
        when(membershipRepository.existsByTeamIdAndUserId(team.getId(), organizer.getId())).thenReturn(true);
        when(membershipRepository.existsByTeamIdAndUserId(team.getId(), invitee.getId())).thenReturn(true);
        when(meetingRepository.existsOverlappingMeeting(
                team.getId(), start, end, null, MeetingStatus.CANCELLED)).thenReturn(false);
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> {
            Meeting meeting = invocation.getArgument(0);
            meeting.setId(40L);
            return meeting;
        });
        when(participantRepository.existsByMeetingIdAndUserId(any(), any())).thenReturn(false);

        var response = service.createMeeting(request);

        assertThat(response.id()).isEqualTo(40L);
        assertThat(response.participantCount()).isEqualTo(2);
        ArgumentCaptor<MeetingParticipant> captor = ArgumentCaptor.forClass(MeetingParticipant.class);
        verify(participantRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(participant -> participant.getUser().getId(), MeetingParticipant::getRsvpStatus)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(organizer.getId(), RsvpStatus.ACCEPTED),
                        org.assertj.core.groups.Tuple.tuple(invitee.getId(), RsvpStatus.PENDING));
    }

    @Test
    void createMeetingRejectsOverlappingTeamMeeting() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(1);
        MeetingCreateRequest request = new MeetingCreateRequest(
                "Weekly sync", null, team.getId(), organizer.getId(), start, end, null, List.of());

        when(teamRepository.findByIdAndActiveTrue(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(membershipRepository.existsByTeamIdAndUserId(team.getId(), organizer.getId())).thenReturn(true);
        when(meetingRepository.existsOverlappingMeeting(
                team.getId(), start, end, null, MeetingStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> service.createMeeting(request))
                .isInstanceOf(MeetingConflictException.class)
                .hasMessageContaining("already has a meeting");
    }

    @Test
    void createMeetingRejectsOrganizerOutsideTeam() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        MeetingCreateRequest request = new MeetingCreateRequest(
                "Weekly sync", null, team.getId(), organizer.getId(), start, start.plusHours(1), null, List.of());

        when(teamRepository.findByIdAndActiveTrue(team.getId())).thenReturn(Optional.of(team));
        when(userRepository.findById(organizer.getId())).thenReturn(Optional.of(organizer));
        when(membershipRepository.existsByTeamIdAndUserId(team.getId(), organizer.getId())).thenReturn(false);

        assertThatThrownBy(() -> service.createMeeting(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not a member");
    }

    private TeamMember user(Long id, String username) {
        TeamMember user = new TeamMember();
        user.setId(id);
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName("User");
        user.setDeleted(false);
        return user;
    }
}
