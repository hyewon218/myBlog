package com.sparta.myblog.repository;


import com.sparta.myblog.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Boolean existsByImageUrlAndId(String fileName, Long id);
    Boolean existsByImageUrlAndChatRoom_Id(String fileName, String id);
}