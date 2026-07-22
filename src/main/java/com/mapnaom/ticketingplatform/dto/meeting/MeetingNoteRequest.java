package com.mapnaom.ticketingplatform.dto.meeting;

import com.mapnaom.ticketingplatform.model.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MeetingNoteRequest(
        @NotNull Long authorId,
        @NotBlank @Size(max = 20000) String content,
        @NotNull NoteType type
) {
}
