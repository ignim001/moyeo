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

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() == StompCommand.CONNECT) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            jwtUtil.validateJwt(token);
        }

        if (accessor.getCommand() == StompCommand.SEND) {
            String roomId = accessor.getDestination().split("/")[2];
        }
        
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            String bearerToken = accessor.getFirstNativeHeader("Authorization");
            String token = bearerToken.substring(7);
            jwtUtil.validateJwt(token);

            String userId = jwtUtil.getProviderIdFromJwt(token);
            String roomId = accessor.getDestination().split("/")[2];
            
            if (!chatService.isRoomParticipant(userId, Long.parseLong(roomId))){
                throw new AuthenticationServiceException("해당 room 에 권한이 없습니다");
            }
        }

        return message;
    }
}
