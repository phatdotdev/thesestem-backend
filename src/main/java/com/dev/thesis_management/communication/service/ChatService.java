package com.dev.thesis_management.communication.service;

import com.dev.thesis_management.communication.dto.ChatMessageRequest;
import com.dev.thesis_management.communication.dto.ChatMessageResponse;
import com.dev.thesis_management.communication.entity.ChatMessage;
import com.dev.thesis_management.communication.mapper.ChatMessageMapper;
import com.dev.thesis_management.communication.repository.ChatMessageRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.thesis.repository.GroupRepository;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {

    UserRepository userRepository;
    GroupRepository groupRepository;
    ChatMessageRepository chatMessageRepository;
    WebSocketSender webSocketSender;

    @Transactional
    public ChatMessageResponse send(ChatMessageRequest request, UUID userId) {

        validateRequest(request);

        User sender = findUser(userId);

        ChatMessage message = ChatMessage.builder()
                .content(request.content())
                .sender(sender)
                .build();

        if (request.groupId() != null) {

            Group group = findGroup(request.groupId());

            validateGroupMember(group, sender);

            message.setGroup(group);

        } else {

            User receiver = findUser(request.receiverId());

            message.setReceiver(receiver);
        }

        chatMessageRepository.save(message);

        ChatMessageResponse response =
                ChatMessageMapper.mapToResponse(message);

        sendRealtime(message, response);

        return response;
    }

    private void validateRequest(ChatMessageRequest request) {

        if (request.content() == null || request.content().isBlank()) {
            throw new BadRequestException("Message content is empty");
        }

        if (request.groupId() == null && request.receiverId() == null) {
            throw new BadRequestException("Message must send to group or user");
        }

        if (request.groupId() != null && request.receiverId() != null) {
            throw new BadRequestException("Message cannot send to both group and user");
        }
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private Group findGroup(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));
    }

    private void validateGroupMember(Group group, User sender) {

        boolean isMember = group.getStudents()
                .stream()
                .anyMatch(member -> member.getUser().getId().equals(sender.getId()))
                || group.getMentor().getUser().getId().equals(sender.getId());

        if (!isMember) {
            throw new BadRequestException("User is not in group");
        }
    }

    private void sendRealtime(ChatMessage message,
                              ChatMessageResponse response) {

        if (message.getGroup() != null) {

            webSocketSender.sendGroupMessage(
                    message.getGroup().getId(),
                    response
            );

        } else {

            webSocketSender.sendPrivateMessage(
                    message.getReceiver().getId(),
                    response
            );
        }
    }

    public List<ChatMessageResponse> getGroupChatMessages(UUID groupId, UUID userId) {
        Group group = findGroup(groupId);

        validateGroupMember(group, findUser(userId));

        List<ChatMessage> messages = chatMessageRepository.findByGroupId(groupId);

        return messages.stream()
                .map(ChatMessageMapper::mapToResponse)
                .toList();
    }
}