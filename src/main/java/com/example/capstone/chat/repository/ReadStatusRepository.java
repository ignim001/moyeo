package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.chat.entity.ReadStatus;
import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, Integer>, ReadStatusRepositoryCustom {
    List<ReadStatus> findByUserAndChatRoom(UserEntity user, ChatRoom chatRoom);
}
