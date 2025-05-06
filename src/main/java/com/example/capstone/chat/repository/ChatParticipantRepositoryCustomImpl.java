package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatParticipant;
import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.chat.entity.QChatParticipant;
import com.example.capstone.chat.entity.QChatRoom;
import com.example.capstone.user.entity.UserEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.example.capstone.chat.entity.QChatParticipant.*;
import static com.example.capstone.chat.entity.QChatRoom.*;

@RequiredArgsConstructor
public class ChatParticipantRepositoryCustomImpl implements ChatParticipantRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ChatRoom> findExistRoom(Long myId, Long otherUserId) {
        QChatParticipant cp1 = new QChatParticipant("cp1");
        QChatParticipant cp2 = new QChatParticipant("cp2");

        return Optional.ofNullable(queryFactory.select(cp1.chatRoom)
                .from(cp1)
                .join(cp2).on(cp1.chatRoom.id.eq(cp2.chatRoom.id))
                .where(cp1.id.eq(myId),
                        cp2.id.eq(otherUserId))
                .fetchOne());
    }

    @Override
    public List<ChatParticipant> findByUserOrderByChatRoomUpdatedTimeDesc(UserEntity user) {
        return queryFactory.selectFrom(chatParticipant)
                .join(chatParticipant.chatRoom, chatRoom).fetchJoin()
                .where(chatParticipant.user.id.eq(user.getId()))
                .orderBy(chatParticipant.chatRoom.updatedTime.desc())
                .fetch();
    }
}
