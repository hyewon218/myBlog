package com.sparta.myblog.service;


import com.sparta.myblog.Image.config.AwsS3upload;
import com.sparta.myblog.Image.entity.Image;
import com.sparta.myblog.Image.repository.ImageRepository;
import com.sparta.myblog.dto.ChatRoomListResponseDto;
import com.sparta.myblog.dto.ChatRoomRequestDto;
import com.sparta.myblog.dto.ChatRoomResponseDto;
import com.sparta.myblog.entity.ChatRoom;
import com.sparta.myblog.entity.User;
import com.sparta.myblog.exception.BusinessException;
import com.sparta.myblog.exception.ErrorCode;
import com.sparta.myblog.repository.ChatRoomRedisRepository;
import com.sparta.myblog.repository.ChatRoomRepository;
import com.sparta.myblog.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomRedisRepository chatRoomRedisRepository;
    private final AwsS3upload awsS3upload;
    private final ImageRepository imageRepository;
    //private final NotificationService notificationService;

    // 오픈채팅방 목록 조회
    @Override
    @Transactional(readOnly = true)
    public ChatRoomListResponseDto getOpenChatRooms() {
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByOrderByCreatedAtAsc();
        return ChatRoomListResponseDto.of(chatRoomList);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomListResponseDto getMyOpenChatRooms(UserDetailsImpl userDetails) {
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByUserId(
            userDetails.getUser().getId());

        return ChatRoomListResponseDto.of(chatRoomList);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatRoomResponseDto getOpenChatRoom(String id) {
        ChatRoom chatRoom = findChatRoom(id);

        return ChatRoomResponseDto.of(chatRoom);
    }

    // 오픈채팅방 생성
    @Override
    public void createOpenChatRoom(ChatRoomRequestDto requestDto, User user,
        List<MultipartFile> files) throws IOException {

        ChatRoom chatRoom = requestDto.toEntity(user);
        chatRoomRepository.save(chatRoom);

        // 서버간 채팅방 공유를 위해 redis hash 에 저장한다.
        chatRoomRedisRepository.createChatRoom(requestDto, user);

        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "chatRoom " + chatRoom.getId());
                if (imageRepository.existsByImageUrlAndChatRoom_Id(fileUrl, chatRoom.getId())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(chatRoom, fileUrl));
            }
        }
    }

    // 오픈채팅방 수정
    @Override
    @Transactional
    public void updateOpenChatRoom(String id, ChatRoomRequestDto requestDto,
        User user, List<MultipartFile> files) throws IOException {
        ChatRoom chatRoom = findChatRoom(id);

        if (!chatRoom.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ONLY_MASTER_EDIT);
        }
        chatRoom.update(requestDto);
        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = awsS3upload.upload(file, "chatRoom " + chatRoom.getId());
                if (imageRepository.existsByImageUrlAndChatRoom_Id(fileUrl, chatRoom.getId())) {
                    throw new BusinessException(ErrorCode.EXISTED_FILE);
                }
                imageRepository.save(new Image(chatRoom, fileUrl));
            }
        }
    }

    // 오픈채팅방 삭제
    @Override
    @Transactional
    public void deleteChatRoom(String id, User user) {
        ChatRoom chatRoom = findChatRoom(id);

        if (!chatRoom.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ONLY_MASTER_DELETE);
        }
        chatRoomRepository.delete(chatRoom);
    }

    private ChatRoom findChatRoom(String id) {
        return chatRoomRepository.findById(id).orElseThrow(() ->
            new BusinessException(ErrorCode.NOT_FOUND_CHATROOM));
    }
}