package com.example.capstone.chat.controller;

import com.example.capstone.chat.service.ChatService;
import com.example.capstone.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final ConcurrentHashMap<String, Set<String>> roomSubscribers = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            validateJwt(accessor);
        }
        
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String token = validateJwt(accessor);
            String userId = jwtUtil.getProviderIdFromJwt(token);
            String roomId = accessor.getDestination().split("/")[2];

            if (!chatService.isRoomParticipant(userId, Long.parseLong(roomId))){
                throw new AuthenticationServiceException("해당 room 에 권한이 없습니다");
            }

            roomSubscribers.computeIfAbsent(roomId, k -> new HashSet<>()).add(userId);

        }else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String token = validateJwt(accessor);
            String userId = jwtUtil.getProviderIdFromJwt(token);
            roomSubscribers.values().forEach(s -> s.remove(userId));
        }
        return message;
    }

    public String validateJwt(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader("Authorization");
        String token = bearerToken.substring(7);
        jwtUtil.validateJwt(token);
        return token;
    }

    // Service 계층에서 Subscribe 정보 참조
    public Set<String> getSubscribersProviderId(Long roomId) {
        return roomSubscribers.getOrDefault(roomId, Collections.emptySet());
    }
}
