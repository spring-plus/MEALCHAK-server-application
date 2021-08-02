package com.mealchak.mealchakserverapplication.service;

import com.mealchak.mealchakserverapplication.dto.response.ChatRoomListResponseDto;
import com.mealchak.mealchakserverapplication.model.ChatRoom;
import com.mealchak.mealchakserverapplication.model.Post;
import com.mealchak.mealchakserverapplication.model.User;
import com.mealchak.mealchakserverapplication.model.AllChatInfo;
import com.mealchak.mealchakserverapplication.repository.ChatRoomRepository;
import com.mealchak.mealchakserverapplication.repository.PostRepository;
import com.mealchak.mealchakserverapplication.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ChatRoomService {

    // HashPerations 레디스에서 쓰는 자료형
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRoomRepository userRoomRepository;
    private final PostRepository postRepository;

    public static final String ENTER_INFO = "ENTER_INFO";

    //채팅방생성
    public Long createChatRoom(Long postId, User user) {
        String uuid = UUID.randomUUID().toString();
        ChatRoom chatRoom = new ChatRoom(postId, uuid, user);
        chatRoomRepository.save(chatRoom);
        return chatRoom.getRoomId();
    }

    // 사용자별 채팅방 목록 조회
    public List<ChatRoomListResponseDto> getOnesChatRoom(User user) {
        List<ChatRoomListResponseDto> responseDtos = new ArrayList<>();
        List<AllChatInfo> allChatInfoList = userRoomRepository.findAllByUserId(user.getId());
        for (AllChatInfo allChatInfo : allChatInfoList) {
            Long roomId = allChatInfo.getRoomId();
            ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId);
            Long id = chatRoom.getPostId();
            Optional<Post> post = postRepository.findById(id);
            String title = post.get().getTitle();
            Long headCountChat = userRoomRepository.countAllByRoomId(roomId);
            ChatRoomListResponseDto responseDto = new ChatRoomListResponseDto(chatRoom, title, headCountChat);
            responseDtos.add(responseDto);
        }
        return responseDtos;
    }

    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);    // redistemplate에 (입장type, ,) 누가 어떤방에 들어갔는지 정보를 리턴
    }

    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }

    //채팅방전부찾기
    public List<ChatRoom> getAll() {
        return chatRoomRepository.findAll();
    }

    //채팅방에 입장
    public void joinChatRoom(User user, Long id) {
        AllChatInfo allChatInfo = new AllChatInfo();
        allChatInfo.setUserId(user.getId());
        ChatRoom chatRoom = chatRoomRepository.findByPostId(id);
        allChatInfo.setRoomId(chatRoom.getRoomId());
        userRoomRepository.save(allChatInfo);
    }

    @Transactional
    public void deletePost(Long postId) {
        ChatRoom chatRoom = chatRoomRepository.findByPostId(postId);
        userRoomRepository.deleteByRoomId(chatRoom.getRoomId());
        chatRoomRepository.deleteByPostId(postId);
    }
}
