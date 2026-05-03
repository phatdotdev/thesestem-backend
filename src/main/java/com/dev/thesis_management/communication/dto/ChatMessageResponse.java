package com.dev.thesis_management.communication.dto;

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
public class ChatMessageResponse {
    UUID id;
    String content;
    SenderResponse sender;
    String receiverId;
    String groupId;
    LocalDateTime createdAt;
}
