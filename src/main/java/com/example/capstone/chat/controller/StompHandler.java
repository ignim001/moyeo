package com.example.capstone.chat.controller;

import com.example.capstone.chat.service.ChatService;
import com.example.capstone.util.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> roomSubscribers = new ConcurrentHashMap<>();


    public StompHandler(JwtUtil jwtUtil, @Lazy ChatService chatService) {
        this.jwtUtil = jwtUtil;
        this.chatService = chatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        try {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = validateJwt(accessor);
                String userId = jwtUtil.getProviderIdFromJwt(token);
                sessionUserMap.put(accessor.getSessionId(), userId);
            }

            if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String sessionId = accessor.getSessionId();
                String userId = sessionUserMap.get(sessionId);
                String roomId = accessor.getDestination().split("/")[2];

                if (userId == null) throw new AuthenticationServiceException("세션 정보가 없습니다");

                if (!chatService.isRoomParticipant(userId, Long.parseLong(roomId))) {
                    throw new AuthenticationServiceException("해당 room 에 권한이 없습니다");
                }

                roomSubscribers.computeIfAbsent(roomId, k -> new HashSet<>()).add(userId);
            }

            if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                String sessionId = accessor.getSessionId();
                String userId = sessionUserMap.remove(sessionId); // 세션 종료되면 매핑 제거

                if (userId != null) {
                    roomSubscribers.values().forEach(set -> set.remove(userId));
                }
            }
            return message;

        } catch (Exception e) {
            log.warn("STOMP preSend 예외 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    public String validateJwt(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new AuthenticationServiceException("유효하지 않은 Authorization 헤더 형식");
        }

        String token = bearerToken.substring(7);
        jwtUtil.validateJwt(token);
        return token;
    }

    // Service 계층에서 Subscribe 정보 참조
    public Set<String> getSubscribersProviderId(Long roomId) {
        return roomSubscribers.getOrDefault(roomId, Collections.emptySet());
    }
}
