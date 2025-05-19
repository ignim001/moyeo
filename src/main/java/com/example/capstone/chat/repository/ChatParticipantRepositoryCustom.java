package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.user.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepositoryCustom {
    @EntityGraph(attributePaths = {
            "chatRoom",
            "chatRoom.participants",
            "chatRoom.participants.user"
    })
    Optional<ChatRoom> findExistRoom(Long myId, Long otherUserId);
}
