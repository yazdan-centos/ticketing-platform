package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingParticipantResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingResponse;
import com.mapnaom.ticketingplatform.model.AgendaItem;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.MeetingNote;
import com.mapnaom.ticketingplatform.model.MeetingParticipant;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {
    public MeetingResponse toResponse(Meeting meeting) {
        var agendaItems = meeting.getAgendaItems().stream().map(this::toAgendaResponse).toList();
        var participants = meeting.getParticipants().stream().map(this::toParticipantResponse).toList();
        return new MeetingResponse(
                meeting.getId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getTeam().getId(),
                meeting.getTeam().getName(),
                meeting.getOrganizer().getId(),
                fullName(meeting.getOrganizer()),
                meeting.getStartTime(),
                meeting.getEndTime(),
                meeting.getLocation(),
                meeting.getStatus(),
                agendaItems,
                participants,
                participants.size(),
                meeting.getCreatedAt(),
                meeting.getUpdatedAt()
        );
    }

    public AgendaItemResponse toAgendaResponse(AgendaItem item) {
        AppUser presenter = item.getPresenter();
        return new AgendaItemResponse(
                item.getId(),
                item.getTopic(),
                item.getDescription(),
                item.getDisplayOrder(),
                item.getDurationMinutes(),
                presenter == null ? null : presenter.getId(),
                presenter == null ? null : fullName(presenter)
        );
    }

    public MeetingParticipantResponse toParticipantResponse(MeetingParticipant participant) {
        return new MeetingParticipantResponse(
                participant.getUser().getId(),
                participant.getUser().getUsername(),
                fullName(participant.getUser()),
                participant.getRsvpStatus(),
                participant.isAttended()
        );
    }

    public MeetingNoteResponse toNoteResponse(MeetingNote note) {
        return new MeetingNoteResponse(
                note.getId(),
                note.getAuthor().getId(),
                fullName(note.getAuthor()),
                note.getContent(),
                note.getType(),
                note.getCreatedAt()
        );
    }

    private String fullName(AppUser user) {
        return user.getFullName().trim();
    }
}
