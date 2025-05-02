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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    // Todo 채팅 기본 기능 구현 (메시지 저장, 이전 메시지 조회, 읽음 처리, 채팅방 목록 조회, 나가기, 채팅방 생성)
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
        chatRoom.updateChatRoom(LocalDateTime.now());

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
        List<ChatParticipant> sortedChatParticipants = chatParticipants.stream()
                .sorted(Comparator.comparing((ChatParticipant c) -> c.getChatRoom().getUpdatedTime()).reversed())
                .collect(Collectors.toList());

        return sortedChatParticipants.stream()
                .map(c -> MyChatRoomListResDto.builder()
                        .roomId(c.getChatRoom().getId())
                        .otherUserNickname(c.getChatRoom().getRoomName())
                        .unReadCount(readStatusRepository.countByChatRoomAndUserAndIsReadFalse(c.getChatRoom(), user))
                        .otherUserImageUrl(c.getChatRoom().getRoomImage())
                        .build())
                .collect(Collectors.toList());
    }

    // 채팅방 생성
    public Long createRoom(CustomOAuth2User userDetails, String otherUserNickname) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                        .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserEntity otherUser = userRepository.findByNickname(otherUserNickname)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Optional<ChatRoom> existRoom = chatParticipantRepository.findExistRoom(user.getId(), otherUser.getId());
        if (existRoom.isPresent()){
            return existRoom.get().getId();
        }

        ChatRoom newRoom = ChatRoom.builder()
                .roomName(otherUserNickname)
                .roomImage(otherUser.getProfileImageUrl())
                .build();

        chatRoomRepository.save(newRoom);
        addParticipantToRoom(newRoom, user);
        addParticipantToRoom(newRoom, otherUser);
        return newRoom.getId();
    }

    // 채팅방 참여자 추가
    private void addParticipantToRoom(ChatRoom chatRoom, UserEntity user) {
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    // 채팅방 나가기
    public void leaveRoom(CustomOAuth2User userDetails, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found"));

        ChatParticipant chatParticipant = chatParticipantRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() -> new EntityNotFoundException("ChatParticipant not found"));
        chatParticipantRepository.delete(chatParticipant);

        // 모든 참여자가 나간경우 채팅방 삭제
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        if (chatParticipants.isEmpty()) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    // 이전 메시지 조회
    public List<ChatMessageDto> getChatHistory(CustomOAuth2User userDetails, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new UserNotFoundException("User not Found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not Found"));

        // 조회하려는 사용자가 채팅방에 속해있는 사용자인지 검증
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        check = chatParticipants.stream()
                .anyMatch(c -> c.getUser().equals(user));

        if (!check) throw new IllegalArgumentException("본인이 속하지 않은 채팅방");

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        return chatMessages.stream()
                .map(m -> ChatMessageDto.builder()
                        .message(m.getContent())
                        .sender(m.getUser().getNickname())
                        .build())
                .collect(Collectors.toList());
    }

    // Subscribe 요청 사용자 검증
    public boolean isRoomParticipant(String userId, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found"));

        return chatRoom.getChatParticipants().stream()
                .anyMatch(c -> c.getUser().equals(user));
    }
}
