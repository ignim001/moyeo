package com.example.capstone.chat.service;

import com.example.capstone.chat.controller.StompHandler;
import com.example.capstone.chat.dto.ChatMessageReqDto;
import com.example.capstone.chat.dto.ChatMessageResDto;
import com.example.capstone.chat.dto.MyChatRoomListResDto;
import com.example.capstone.chat.dto.ReadNoticeDto;
import com.example.capstone.chat.entity.ChatMessage;
import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.chat.entity.ReadStatus;
import com.example.capstone.chat.repository.ChatMessageRepository;
import com.example.capstone.chat.repository.ChatParticipantRepository;
import com.example.capstone.chat.repository.ChatRoomRepository;
import com.example.capstone.chat.repository.ReadStatusRepository;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.repository.UserRepository;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final StompHandler stompHandler;
    private final SimpMessageSendingOperations messagingTemplate;

    // 메시지 저장
    @Transactional
    public ChatMessageResDto saveMessage(Long roomId, ChatMessageReqDto chatMessageReqDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Room not found"));

        UserEntity sender = userRepository.findByNickname(chatMessageReqDto.getSender())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("member cannot be found"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(sender)
                .content(chatMessageReqDto.getMessage())
                .build();

        chatRoom.updateChatRoom(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
        chatMessageRepository.save(chatMessage);

        // 해당 메시지에 대한 읽음 처리 (현재 접속한 참여자 정보 포함)
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        Set<String> subscribers = stompHandler.getSubscribersProviderId(roomId);
        List<ReadStatus> readStatuses = new ArrayList<>();
        for (ChatParticipant participant : chatParticipants) {
            UserEntity user = participant.getUser();
            boolean isSender = user.equals(sender);
            boolean isSubscribed = subscribers.contains(user.getProviderId());
            boolean isRead = isSender || isSubscribed;

            ReadStatus readStatus = ReadStatus.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .chatMessage(chatMessage)
                    .isRead(isRead)
                    .build();

            readStatuses.add(readStatus);
            readStatusRepository.save(readStatus);
        }

        return ChatMessageResDto.builder()
                .message(chatMessageReqDto.getMessage())
                .sender(chatMessageReqDto.getSender())
                .unReadUserCount(readStatuses.stream()
                        .filter(rs -> !rs.getIsRead())
                        .count())
                .timestamp(chatMessage.getCreatedTime().atOffset(ZoneOffset.ofHours(9)))
                .build();
    }

    // 자신이 속한 채팅방 조회
    @Transactional(readOnly = true)
    public List<MyChatRoomListResDto> getMyRoom(CustomOAuth2User userDetails) {
        UserEntity currentUser = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found"));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByUserAndIsDeletedFalseOrderByChatRoomUpdatedTimeDesc(currentUser);

        List<ChatRoom> chatRooms = chatParticipants.stream()
                .map(ChatParticipant::getChatRoom)
                .toList();

        Map<Long, Long> unreadCountMap = readStatusRepository.countUnreadByChatRoomsAndUser(chatRooms, currentUser);

        return chatParticipants.stream()
                .map(c -> {
                    ChatRoom chatRoom = c.getChatRoom();

                    // 현재 유저를 제외한 상대방 찾기
                    UserEntity otherUser = chatRoom.getChatParticipants().stream()
                            .map(ChatParticipant::getUser)
                            .filter(user -> !user.getId().equals(currentUser.getId()))
                            .findFirst()
                            .orElse(UserEntity.deletedUserPlaceholder()); // 또는 null-safe 처리

                    long unreadCount = unreadCountMap.getOrDefault(chatRoom.getId(), 0L);

                    return MyChatRoomListResDto.builder()
                            .roomId(chatRoom.getId())
                            .otherUserNickname(otherUser.getNickname())
                            .otherUserImageUrl(otherUser.getProfileImageUrl())
                            .unReadCount(unreadCount)
                            .build();
                })
                .toList();
    }

    // 채팅방 생성
    @Transactional
    public Long createRoom(CustomOAuth2User userDetails, String otherUserNickname) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserEntity otherUser = userRepository.findByNickname(otherUserNickname)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Optional<ChatRoom> existRoom = chatParticipantRepository.findExistRoom(user.getId(), otherUser.getId());
        if (existRoom.isPresent()){
            return existRoom.get().getId();
        }

        ChatRoom newRoom = ChatRoom.builder()
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
                .isDeleted(false)
                .build();
        chatParticipantRepository.save(chatParticipant);
    }

    // 채팅방 나가기
    @Transactional
    public void leaveRoom(CustomOAuth2User userDetails, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User not Found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Room not found"));

        ChatParticipant chatParticipant = chatParticipantRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("ChatParticipant not found"));

        chatParticipant.leave();

        // 모든 유저가 나갔는지 확인
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean allLeft = chatParticipants.stream().allMatch(ChatParticipant::getIsDeleted);
        if (allLeft) {
            chatRoomRepository.delete(chatRoom);
        }
    }

    // 이전 메시지 조회
    @Transactional(readOnly = true)
    public List<ChatMessageResDto> getChatHistory(CustomOAuth2User userDetails, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User not Found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("ChatRoom not Found"));

        // 조회하려는 사용자가 채팅방에 속해있는 사용자인지 검증
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        check = chatParticipants.stream()
                .anyMatch(c -> c.getUser().equals(user));

        if (!check) throw new IllegalArgumentException("본인이 속하지 않은 채팅방");

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        return chatMessages.stream()
                .map(m -> ChatMessageResDto.builder()
                        .message(m.getContent())
                        .sender(m.getUser().getNickname())
                        .unReadUserCount(m.getReadStatuses().stream()
                                .filter(rs -> !rs.getIsRead())
                                .count())
                        .timestamp(m.getCreatedTime().atOffset(ZoneOffset.ofHours(9)))
                        .build())
                .toList();
    }

    // Subscribe 요청 사용자 검증
    @Transactional(readOnly = true)
    public boolean isRoomParticipant(String userId, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("ChatRoom not found"));

        return chatRoom.getChatParticipants().stream()
                .anyMatch(c -> c.getUser().equals(user));
    }

    // 읽음 처리
    @Transactional
    public void readMessage(CustomOAuth2User userDetails, Long roomId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User not Found"));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("ChatRoom not found"));

        List<ReadStatus> readStatus = readStatusRepository.findByUserAndChatRoom(user, chatRoom);
        if (!readStatus.isEmpty()) {
            for (ReadStatus status : readStatus) {
                status.updateIsRead(true);
            }

            Set<String> subscribers = new HashSet<>(stompHandler.getSubscribersProviderId(roomId));
            subscribers.remove(user.getProviderId());

            // 자신을 제외한 구독자가 존재할 경우 알림 전송
            if (!subscribers.isEmpty()) {
                ReadNoticeDto notice = new ReadNoticeDto(user.getNickname());
                messagingTemplate.convertAndSend("/queue/" + roomId + "/read", notice);
            }
        }
    }
}
