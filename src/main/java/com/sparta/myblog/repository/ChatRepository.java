package com.sparta.myblog.repository;

import com.sparta.myblog.entity.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, String> {
    List<Chat> findAllByChatRoomIdOrderByCreatedAtAsc(String chatRoom_id); // 방 메세지 생성날짜 기준 오름차순 정렬
}