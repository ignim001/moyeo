package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.chat.entity.ReadStatus;
import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Integer> {
    Long countByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, UserEntity userEntity);
}
