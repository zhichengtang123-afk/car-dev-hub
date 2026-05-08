package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardev.hub.common.PageResult;
import com.cardev.hub.entity.*;
import com.cardev.hub.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final UserMapper userMapper;
    private final UserLikeMapper likeMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 获取知识评论列表
     */
    public PageResult<Comment> getComments(Long knowledgeId, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        
        Page<Comment> result = commentMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getKnowledgeId, knowledgeId)
                        .isNull(Comment::getParentId)
                        .orderByDesc(Comment::getCreateTime));
        
        Long userId = userService.getCurrentUserId();
        result.getRecords().forEach(c -> {
            fillCommentInfo(c, userId);
            // 获取子评论
            List<Comment> replies = commentMapper.selectList(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getParentId, c.getId())
                            .orderByAsc(Comment::getCreateTime));
            replies.forEach(r -> fillCommentInfo(r, userId));
            c.setReplies(replies);
        });
        
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 发表评论
     */
    @Transactional
    public Long createComment(Long knowledgeId, String content, Long parentId) {
        Long userId = userService.getCurrentUserId();
        
        Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        
        Comment comment = new Comment();
        comment.setKnowledgeId(knowledgeId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikeCount(0);
        
        commentMapper.insert(comment);
        
        // 更新评论数
        knowledge.setCommentCount(knowledge.getCommentCount() + 1);
        knowledgeMapper.updateById(knowledge);
        
        // 发送通知
        if (!knowledge.getAuthorId().equals(userId)) {
            User currentUser = userMapper.selectById(userId);
            String name = currentUser != null ? (currentUser.getRealName() != null ? currentUser.getRealName() : currentUser.getUsername()) : "某人";
            notificationService.sendNotification(
                knowledge.getAuthorId(),
                "新评论",
                name + " 评论了你的内容: 《" + knowledge.getTitle() + "》",
                "comment",
                knowledgeId
            );
        }
        
        return comment.getId();
    }

    /**
     * 删除评论
     */
    @Transactional
    public void deleteComment(Long knowledgeId, Long commentId) {
        Long userId = userService.getCurrentUserId();
        Comment comment = commentMapper.selectById(commentId);
        
        if (comment == null || !comment.getKnowledgeId().equals(knowledgeId)) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        
        // 删除评论及其子评论
        int count = 1;
        List<Comment> children = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        count += children.size();
        
        commentMapper.deleteById(commentId);
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        
        // 更新评论数
        Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge != null) {
            knowledge.setCommentCount(Math.max(0, knowledge.getCommentCount() - count));
            knowledgeMapper.updateById(knowledge);
        }
    }

    /**
     * 点赞评论
     */
    @Transactional
    public void likeComment(Long commentId) {
        Long userId = userService.getCurrentUserId();
        
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, "comment")
                .eq(UserLike::getTargetId, commentId));
        
        if (count > 0) {
            throw new RuntimeException("已经点赞过了");
        }
        
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetType("comment");
        like.setTargetId(commentId);
        like.setIsLike(1);
        likeMapper.insert(like);
        
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
        }
    }

    // 辅助方法
    private void fillCommentInfo(Comment comment, Long currentUserId) {
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            comment.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            comment.setUserAvatar(user.getAvatar());
        }
        
        if (currentUserId != null) {
            comment.setIsLiked(likeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                    .eq(UserLike::getUserId, currentUserId)
                    .eq(UserLike::getTargetType, "comment")
                    .eq(UserLike::getTargetId, comment.getId())) > 0);
        }
    }
}
