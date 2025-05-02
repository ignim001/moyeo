package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepositoryCustom {
    Optional<ChatRoom> findExistRoom(Long myId, Long otherUserId);
}
