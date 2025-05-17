package com.example.capstone.chat.repository;

import com.example.capstone.chat.entity.ChatRoom;
import com.example.capstone.user.entity.UserEntity;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.capstone.chat.entity.QReadStatus.readStatus;

@RequiredArgsConstructor
public class ReadStatusRepositoryCustomImpl implements ReadStatusRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, Long> countUnreadByChatRoomsAndUser(List<ChatRoom> chatRooms, UserEntity user) {
        List<Tuple> result = queryFactory
                .select(readStatus.chatRoom.id, readStatus.count())
                .from(readStatus)
                .where(
                        readStatus.chatRoom.in(chatRooms),
                        readStatus.user.eq(user),
                        readStatus.isRead.isFalse()
                )
                .groupBy(readStatus.chatRoom.id)
                .fetch();

        // Tuple → Map<Long, Long> 변환
        return result.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(readStatus.chatRoom.id),
                        tuple -> tuple.get(readStatus.count())
                ));    }
}
