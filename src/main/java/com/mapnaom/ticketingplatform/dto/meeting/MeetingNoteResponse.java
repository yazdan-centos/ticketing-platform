package com.mapnaom.ticketingplatform.dto.meeting;

import com.mapnaom.ticketingplatform.model.enums.NoteType;

import java.time.LocalDateTime;

public record MeetingNoteResponse(
        Long id,
        Long authorId,
        String authorName,
        String content,
        NoteType type,
        LocalDateTime createdAt
) {
}
