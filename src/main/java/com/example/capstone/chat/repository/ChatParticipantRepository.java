package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long>, ChatParticipantRepositoryCustom {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
    List<ChatParticipant> findByUser(UserEntity user);
    Optional<ChatParticipant> findByUserAndChatRoom(UserEntity user, ChatRoom chatRoom);
}
