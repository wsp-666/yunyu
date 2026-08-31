package com.yunyu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunyu.dao.CommentDao;
import com.yunyu.dao.PostDao;
import com.yunyu.dto.PostDTO;
import com.yunyu.entity.Comment;
import com.yunyu.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    @Autowired
    private PostDao postDao;

    @Autowired
    private CommentDao commentDao;

    public int createPost(PostDTO postDTO, int userId) {
        Post post = new Post();
        post.setUserId(userId);
        post.setType(postDTO.getType());
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setImages(postDTO.getImages());
        post.setVideoUrl(postDTO.getVideoUrl());
        post.setSpotId(postDTO.getSpotId());
        post.setVenueId(postDTO.getVenueId());
        post.setTopicTag(postDTO.getTopicTag());
        post.setStatus(1);
        postDao.insert(post);
        return post.getId();
    }

    public Page<Post> getPostList(Integer type, int page, int size) {
        Page<Post> p = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1);
        if (type != null && type > 0) {
            wrapper.eq(Post::getType, type);
        }
        wrapper.orderByDesc(Post::getCreateTime);
        return postDao.selectPage(p, wrapper);
    }

    public Post getPostDetail(int postId) {
        Post post = postDao.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        return post;
    }

    public void addComment(int postId, int userId, String content, Integer parentId) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId != null ? parentId : 0);
        commentDao.insert(comment);

        Post post = postDao.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            postDao.updateById(post);
        }
    }

    public Page<Comment> getComments(int postId, int page, int size) {
        Page<Comment> p = new Page<>(page, size);
        return commentDao.selectPage(p, new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .orderByDesc(Comment::getCreateTime));
    }

    public void likePost(int postId) {
        Post post = postDao.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            postDao.updateById(post);
        }
    }
}
