package com.dev.thesis_management.thesis.dto.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeetingResponse {
    UUID id;
    String title;
    String description;
    LocalDateTime startAt;
    LocalDateTime endAt;
    String url;
}
