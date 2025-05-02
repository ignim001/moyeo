package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    List<ChatParticipant> findByUser(UserEntity user);
}
