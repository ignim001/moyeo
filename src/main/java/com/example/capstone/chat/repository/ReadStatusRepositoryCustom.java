package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.user.entity.UserEntity;

import java.util.List;
import java.util.Map;

public interface ReadStatusRepositoryCustom {
    Map<Long, Long> countUnreadByChatRoomsAndUser(List<ChatRoom> chatRooms, UserEntity user);
}
