package com.example.capstone.chat.controller;

import com.example.capstone.chat.dto.ChatMessageDto;
import com.example.capstone.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto) {
        chatService.saveMessage(roomId, chatMessageDto);
        messagingTemplate.convertAndSend("/queue/" + roomId, chatMessageDto);
    }
}
