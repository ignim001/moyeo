package com.example.capstone.chat.service;

import com.example.capstone.chat.dto.ChatMessageDto;
import com.example.capstone.chat.dto.MyChatRoomListResDto;
import com.example.capstone.chat.entity.ChatMessage;
import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.chat.entity.ReadStatus;
import com.example.capstone.chat.repository.ChatMessageRepository;
import com.example.capstone.chat.repository.ChatParticipantRepository;
import com.example.capstone.chat.repository.ChatRoomRepository;
import com.example.capstone.chat.repository.ReadStatusRepository;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.exception.UserNotFoundException;
import com.example.capstone.user.repository.UserRepository;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    // Todo 채팅 기본 기능 구현 (메시지 저장, 이전 메시지 조회, 읽음 처리, 채팅방 목록 조회, 나가기)
    // Todo 1:1 채팅 방 생성 기능 구현

    // 메시지 저장
    public void saveMessage(Long roomId, ChatMessageDto chatMessageDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        UserEntity sender = userRepository.findByNickname(chatMessageDto.getSender())
                .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(chatMessageDto.getMessage())
                .build();

        chatMessageRepository.save(chatMessage);

        // 해당 메시지에 대한 읽음 처리
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        chatParticipants.stream()
                .map(c -> ReadStatus.builder()
                        .chatRoom(chatRoom)
                        .user(c.getUser())
                        .chatMessage(chatMessage)
                        .isRead(c.getUser().equals(sender)) 
                        .build())
                .forEach(readStatusRepository::save);
    }

    // 자신이 속한 채팅방 조회
    public List<MyChatRoomListResDto> getMyRoom(CustomOAuth2User userDetails) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByUser(user);

        return chatParticipants.stream()
                .map(c -> MyChatRoomListResDto.builder()
                        .roomId(c.getChatRoom().getId())
                        .otherUserNickname(c.getChatRoom().getRoomName())
                        .unReadCount(readStatusRepository.countByChatRoomAndUserAndIsReadFalse(c.getChatRoom(), user))
                        .build())
                .collect(Collectors.toList());
    }

    public Long createRoom(CustomOAuth2User userDetails, String otherUserNickname) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserEntity otherUser = userRepository.findByNickname(otherUserNickname)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Todo QueryDsl 이용해 해당 사용자간 기존 채팅방 존재여부 확인 쿼리 작성


        ChatRoom newRoom = ChatRoom.builder()
                .roomName(otherUserNickname)
                .build();
        chatRoomRepository.save(newRoom);
        addParticipantToRoom(newRoom, user);
        addParticipantToRoom(newRoom, otherUser);
        return newRoom.getId();
    }

    private void addParticipantToRoom(ChatRoom chatRoom, UserEntity user) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }
}
