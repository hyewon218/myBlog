package com.sparta.myblog.service;

import com.sparta.myblog.dto.ChatListResponseDto;
import com.sparta.myblog.dto.ChatMessageDto;
import com.sparta.myblog.entity.Chat;
import com.sparta.myblog.entity.ChatRoom;
import com.sparta.myblog.entity.ChatType;
import com.sparta.myblog.redis.pubsub.RedisSubscriber;
import com.sparta.myblog.repository.ChatRepository;
import com.sparta.myblog.repository.ChatRoomRedisRepository;
import com.sparta.myblog.repository.ChatRoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final RedisSubscriber redisSubscriber;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    // 메세지 삭제 - DB Scheduler 적용 필요

    // 전달 받은 Message 를 Redis 로 보내기
    // 채팅방에 메시지 발송
    @Override
    @Transactional
    public void sendChatMessage(String roomId, ChatMessageDto messageDto) {
        messageDto.setType(ChatType.TALK);

        saveMessage(roomId, messageDto);

        redisMessageListenerContainer.addMessageListener(redisSubscriber,
            chatRoomRedisRepository.getTopic(roomId));

        log.info("레디스 topic 확인 : "+chatRoomRedisRepository.getTopic(roomId));

        // 📍Websocket 에 발행된 메시지를 redis 로 발행한다(publish)
        chatRoomRedisRepository.pushMessage(roomId, messageDto);
    }

    // 오픈채팅 메세지 저장
    @Override
    @Transactional
    public void saveMessage(String roomId, ChatMessageDto requestDto) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("오류"));

        Chat chat = requestDto.toEntity(chatRoom);
        chatRepository.save(chat);
    }

    // 오픈채팅방 채팅 목록 조회
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "chatListCache", key = "#roomId", cacheManager = "cacheManager")
    public ChatListResponseDto getAllChatByRoomId(String roomId) {
        List<Chat> chatList = chatRepository.findAllByChatRoomIdOrderByCreatedAtAsc(roomId);

        return ChatListResponseDto.of(chatList);
    }
}