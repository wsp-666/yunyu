package com.yunyu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.common.PageResult;
import com.yunyu.common.Result;
import com.yunyu.dto.PostDTO;
import com.yunyu.entity.Comment;
import com.yunyu.entity.Post;
import com.yunyu.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createPost(@RequestBody PostDTO postDTO, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        int postId = postService.createPost(postDTO, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("postId", postId);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<PageResult<Post>> getPostList(
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Post> postPage = postService.getPostList(type, page, size);
        return Result.success(PageResult.from(postPage));
    }

    @GetMapping("/detail/{postId}")
    public Result<Post> getPostDetail(@PathVariable int postId) {
        Post post = postService.getPostDetail(postId);
        return Result.success(post);
    }

    @PostMapping("/like/{postId}")
    public Result<Void> likePost(@PathVariable int postId) {
        postService.likePost(postId);
        return Result.success();
    }

    @PostMapping("/comment")
    public Result<Void> addComment(@RequestParam int postId,
                                   @RequestParam String content,
                                   @RequestParam(required = false) Integer parentId,
                                   HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        postService.addComment(postId, userId, content, parentId);
        return Result.success();
    }

    @GetMapping("/comments")
    public Result<PageResult<Comment>> getComments(
            @RequestParam int postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Comment> commentPage = postService.getComments(postId, page, size);
        return Result.success(PageResult.from(commentPage));
    }
}
