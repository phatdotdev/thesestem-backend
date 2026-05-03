package com.dev.thesis_management.communication.controller;

import com.dev.thesis_management.common.response.ApiResponse;
import com.dev.thesis_management.communication.dto.ChatMessageResponse;
import com.dev.thesis_management.communication.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    ChatService chatService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getGroupChatMessages(
            @PathVariable UUID groupId,
            Authentication authentication
    ) {
        return ok(chatService.getGroupChatMessages(groupId, parseUUID(authentication.getName())));
    }

}
