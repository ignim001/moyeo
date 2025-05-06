package com.example.capstone.chat.controller;

import com.example.capstone.chat.dto.ChatMessageReqDto;
import com.example.capstone.chat.dto.ChatMessageResDto;
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
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageReqDto chatMessageReqDto) {
        ChatMessageResDto chatMessageResDto = chatService.saveMessage(roomId, chatMessageReqDto);
        messagingTemplate.convertAndSend("/queue/" + roomId, chatMessageResDto);
    }
}
