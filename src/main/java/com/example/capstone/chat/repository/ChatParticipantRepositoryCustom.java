package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepositoryCustom {
    Optional<ChatRoom> findExistRoom(Long myId, Long otherUserId);
    List<ChatParticipant> findByUserOrderByChatRoomUpdatedTimeDesc(UserEntity user);
}
