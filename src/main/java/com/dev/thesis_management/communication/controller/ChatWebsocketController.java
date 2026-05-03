package com.dev.thesis_management.communication.controller;

import com.dev.thesis_management.communication.dto.ChatMessageRequest;
import com.dev.thesis_management.communication.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebsocketController {

    ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request, Authentication authentication) {
        chatService.send(request, parseUUID(authentication.getName()));
    }
}
