package com.example.capstone.chat.controller;

import com.example.capstone.chat.dto.MyChatRoomListResDto;
import com.example.capstone.chat.service.ChatService;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    
    // Todo 채팅 기본 기능 구현 (이전 메시지 조회, 읽음 처리, 채팅방 목록 조회, 나가기, 1:1 채팅 방 생성)
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyRooms(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        List<MyChatRoomListResDto> myChatRoomList = chatService.getMyRoom(userDetails);
        return new ResponseEntity<>(myChatRoomList, HttpStatus.OK);
    }

    @PostMapping("/room/create")
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                        @RequestParam String otherUserNickname) {
        Long roomId = chatService.createRoom(userDetails, otherUserNickname);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }


}
