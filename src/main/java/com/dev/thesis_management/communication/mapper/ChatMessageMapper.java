package com.dev.thesis_management.communication.mapper;

import com.dev.thesis_management.communication.dto.ChatMessageResponse;
import com.dev.thesis_management.communication.dto.SenderResponse;
import com.dev.thesis_management.user.entity.User;

public class ChatMessageMapper {
    public static SenderResponse mapToSenderResponse(User sender) {
        return SenderResponse.builder()
                .id(sender.getId())
                .name(
                        sender.getStudent() != null
                                ? sender.getStudent().getFullName() + " " + sender.getStudent().getStudentCode()
                                : sender.getLecturer() != null
                                ? sender.getLecturer().getFullName() + " " + sender.getLecturer().getLecturerCode()
                                : sender.getUsername()
                        )
                .build();
    }

    public static ChatMessageResponse mapToResponse(com.dev.thesis_management.communication.entity.ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .sender(mapToSenderResponse(message.getSender()))
                .build();
    }
}
