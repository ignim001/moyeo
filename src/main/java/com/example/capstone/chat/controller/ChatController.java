package com.example.capstone.chat.controller;

import com.example.capstone.chat.dto.ChatMessageDto;
import com.example.capstone.chat.dto.MyChatRoomListResDto;
import com.example.capstone.chat.entity.ChatMessage;
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

    // 읽음 처리

    // 이전 메시지 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                            @PathVariable Long roomId) {
        List<ChatMessageDto> chatMessages = chatService.getChatHistory(userDetails, roomId);
        return new ResponseEntity<>(chatMessages, HttpStatus.OK);
    }

    // 채팅방 나가기
    @DeleteMapping("/room/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                       @PathVariable Long roomId){
        chatService.leaveRoom(userDetails, roomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    // 채팅방 조회
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyRooms(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        List<MyChatRoomListResDto> myChatRoomList = chatService.getMyRoom(userDetails);
        return new ResponseEntity<>(myChatRoomList, HttpStatus.OK);
    }
    
    // 채팅방 생성
    @PostMapping("/room/create")
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                        @RequestParam String otherUserNickname) {
        Long roomId = chatService.createRoom(userDetails, otherUserNickname);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }
}
