package com.mealchak.mealchakserverapplication.controller;

import com.mealchak.mealchakserverapplication.dto.request.PostRequestDto;
import com.mealchak.mealchakserverapplication.model.Post;
import com.mealchak.mealchakserverapplication.model.AllChatInfo;
import com.mealchak.mealchakserverapplication.oauth2.UserDetailsImpl;
import com.mealchak.mealchakserverapplication.service.ChatRoomService;
import com.mealchak.mealchakserverapplication.service.PostService;
import com.mealchak.mealchakserverapplication.service.UserRoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Api(tags = {"1. 모집글"}) // Swagger # 1. 모집글
@RequiredArgsConstructor
@RestController
public class PostController {
    private final PostService postService;
    private final ChatRoomService chatRoomService;
    private final UserRoomService userRoomService;

    // 모집글 생성
    @ApiOperation(value = "모집글 작성", notes = "전체 모집글 조회합니다.")
    @PostMapping("/posts")
    public void createPost(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody PostRequestDto requestDto) {
        if (userDetails != null) {
            //글 저장후 해당글의 id 반환
            Long postId = postService.createPost(userDetails.getUser(), requestDto);
            //uuid생성
            String uuid = UUID.randomUUID().toString();
            //새로운 채팅방을 생성 -> uuid값과 postid값을 가짐
            chatRoomService.createChatRoom(postId,uuid, userDetails.getUser());
            //본인이 해당 채팅방에 입장했다는 정보를 생성
            AllChatInfo allChatInfo = new AllChatInfo(userDetails.getUser().getUserId(),postId);
            //입장 정보를 저장
            userRoomService.save(allChatInfo);
        } else {
            throw new IllegalArgumentException("로그인 하지 않았습니다.");
        }
    }

    // 모집글 불러오기
    @ApiOperation(value = "전체 모집글 조회", notes = "전체 모집글 조회합니다.")
    @GetMapping("/posts")
    public List<Post> getAllPost() {
        return postService.getAllPost();
    }

    // 특정 모집글 불러오기
    @ApiOperation(value = "전체 모집글 조회", notes = "전체 모집글 조회합니다.")
    @GetMapping("/posts/{postId}")
    public Post getPostDetail(@PathVariable Long postId) {
        return postService.getPostDetail(postId);
    }

    // 특정 모집글 수정
    @ApiOperation(value = "전체 모집글 조회", notes = "전체 모집글 조회합니다.")
    @PutMapping("/posts/{postId}")
    public Post updatePostDetail(@PathVariable Long postId, @RequestBody PostRequestDto requestDto) {
        return postService.updatePostDetail(postId, requestDto);
    }

    // 특정 모집글 삭제
    @ApiOperation(value = "전체 모집글 조회", notes = "전체 모집글 조회합니다.")
    @DeleteMapping("/posts/{postId}")
    public void getPostDelete(@PathVariable Long postId) {
        postService.deletePost(postId);
    }
}