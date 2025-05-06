package com.example.capstone.chat.controller;

import com.example.capstone.chat.dto.ChatMessageResDto;
import com.example.capstone.chat.dto.MyChatRoomListResDto;
import com.example.capstone.chat.service.ChatService;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
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
    
    @Operation(summary = "읽음 처리 API", 
            description = "사용자가 접속한 채팅방에 현재까지 발행된 메시지 읽음 처리")
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> readMessage(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                         @PathVariable Long roomId) {
        chatService.readMessage(userDetails, roomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "이전 메시지 조회 API",
            description = "사용자가 접속한 채팅방에 현재까지 발행된 메시지 조회")
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                            @PathVariable Long roomId) {
        List<ChatMessageResDto> chatMessages = chatService.getChatHistory(userDetails, roomId);
        return new ResponseEntity<>(chatMessages, HttpStatus.OK);
    }

    @Operation(summary = "채팅방 나가기 API",
            description = "특정 채팅방 나가기")
    @DeleteMapping("/room/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                       @PathVariable Long roomId){
        chatService.leaveRoom(userDetails, roomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "채팅방 조회 API",
            description = "사용자가 속해있는 채팅방 목록 조회")
    @GetMapping("/my/rooms")
    public ResponseEntity<?> getMyRooms(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        List<MyChatRoomListResDto> myChatRoomList = chatService.getMyRoom(userDetails);
        return new ResponseEntity<>(myChatRoomList, HttpStatus.OK);
    }

    @Operation(summary = "1:1 채팅방 생성 API",
            description = "사용자와 대상자를 참여자로 추가해 1:1 채팅방 생성")
    @PostMapping("/room/create")
    public ResponseEntity<?> createRoom(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                        @RequestParam String otherUserNickname) {
        Long roomId = chatService.createRoom(userDetails, otherUserNickname);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }
}
