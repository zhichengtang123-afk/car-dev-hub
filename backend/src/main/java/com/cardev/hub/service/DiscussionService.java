package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardev.hub.common.PageResult;
import com.cardev.hub.dto.DiscussionRequest;
import com.cardev.hub.entity.*;
import com.cardev.hub.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 讨论服务
 */
@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final DiscussionMapper discussionMapper;
    private final DiscussionReplyMapper replyMapper;
    private final UserMapper userMapper;
    private final UserLikeMapper likeMapper;
    private final UserService userService;

    /**
     * 获取讨论列表
     */
    public PageResult<Discussion> getList(String keyword, Long knowledgeId, Long projectId, 
                                          Boolean resolved, int page, int size) {
        Page<Discussion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Discussion> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Discussion::getTitle, keyword)
                    .or().like(Discussion::getContent, keyword));
        }
        if (knowledgeId != null) {
            wrapper.eq(Discussion::getKnowledgeId, knowledgeId);
        }
        if (projectId != null) {
            wrapper.eq(Discussion::getProjectId, projectId);
        }
        if (resolved != null) {
            wrapper.eq(Discussion::getIsResolved, resolved ? 1 : 0);
        }
        
        wrapper.orderByDesc(Discussion::getCreateTime);
        
        Page<Discussion> result = discussionMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillDiscussionInfo);
        
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 获取讨论详情
     */
    public Discussion getDetail(Long id) {
        Discussion discussion = discussionMapper.selectById(id);
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 增加浏览量
        discussion.setViewCount(discussion.getViewCount() + 1);
        discussionMapper.updateById(discussion);
        
        fillDiscussionInfo(discussion);
        return discussion;
    }

    /**
     * 创建讨论
     */
    @Transactional
    public Long create(DiscussionRequest request) {
        Long userId = userService.getCurrentUserId();
        
        Discussion discussion = new Discussion();
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setAuthorId(userId);
        discussion.setKnowledgeId(request.getKnowledgeId());
        discussion.setProjectId(request.getProjectId());
        discussion.setViewCount(0);
        discussion.setReplyCount(0);
        discussion.setIsResolved(0);
        
        discussionMapper.insert(discussion);
        
        return discussion.getId();
    }

    /**
     * 更新讨论
     */
    @Transactional
    public void update(Long id, DiscussionRequest request) {
        Long userId = userService.getCurrentUserId();
        Discussion discussion = discussionMapper.selectById(id);
        
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        if (!discussion.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权修改此讨论");
        }
        
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setKnowledgeId(request.getKnowledgeId());
        discussion.setProjectId(request.getProjectId());
        
        discussionMapper.updateById(discussion);
    }

    /**
     * 删除讨论
     */
    @Transactional
    public void delete(Long id) {
        Long userId = userService.getCurrentUserId();
        Discussion discussion = discussionMapper.selectById(id);
        
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        if (!discussion.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权删除此讨论");
        }
        
        discussionMapper.deleteById(id);
        replyMapper.delete(new LambdaQueryWrapper<DiscussionReply>().eq(DiscussionReply::getDiscussionId, id));
    }

    /**
     * 获取讨论回复
     */
    public PageResult<DiscussionReply> getReplies(Long discussionId, int page, int size) {
        Page<DiscussionReply> pageParam = new Page<>(page, size);
        
        Page<DiscussionReply> result = replyMapper.selectPage(pageParam,
                new LambdaQueryWrapper<DiscussionReply>()
                        .eq(DiscussionReply::getDiscussionId, discussionId)
                        .isNull(DiscussionReply::getParentId)
                        .orderByDesc(DiscussionReply::getIsAccepted)
                        .orderByDesc(DiscussionReply::getLikeCount)
                        .orderByAsc(DiscussionReply::getCreateTime));
        
        Long userId = userService.getCurrentUserId();
        result.getRecords().forEach(r -> fillReplyInfo(r, userId));
        
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 创建回复
     */
    @Transactional
    public Long createReply(Long discussionId, String content, Long parentId) {
        Long userId = userService.getCurrentUserId();
        
        Discussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        
        DiscussionReply reply = new DiscussionReply();
        reply.setDiscussionId(discussionId);
        reply.setContent(content);
        reply.setAuthorId(userId);
        reply.setParentId(parentId);
        reply.setIsAccepted(0);
        reply.setLikeCount(0);
        
        replyMapper.insert(reply);
        
        // 更新回复数
        discussion.setReplyCount(discussion.getReplyCount() + 1);
        discussionMapper.updateById(discussion);
        
        return reply.getId();
    }

    /**
     * 删除回复
     */
    @Transactional
    public void deleteReply(Long discussionId, Long replyId) {
        Long userId = userService.getCurrentUserId();
        DiscussionReply reply = replyMapper.selectById(replyId);
        
        if (reply == null || !reply.getDiscussionId().equals(discussionId)) {
            throw new RuntimeException("回复不存在");
        }
        if (!reply.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权删除此回复");
        }
        
        replyMapper.deleteById(replyId);
        
        // 更新回复数
        Discussion discussion = discussionMapper.selectById(discussionId);
        if (discussion != null) {
            discussion.setReplyCount(Math.max(0, discussion.getReplyCount() - 1));
            discussionMapper.updateById(discussion);
        }
    }

    /**
     * 采纳回复
     */
    @Transactional
    public void acceptReply(Long discussionId, Long replyId) {
        Long userId = userService.getCurrentUserId();
        Discussion discussion = discussionMapper.selectById(discussionId);
        
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        if (!discussion.getAuthorId().equals(userId)) {
            throw new RuntimeException("只有提问者才能采纳回复");
        }
        
        DiscussionReply reply = replyMapper.selectById(replyId);
        if (reply == null || !reply.getDiscussionId().equals(discussionId)) {
            throw new RuntimeException("回复不存在");
        }
        
        // 取消之前的采纳
        List<DiscussionReply> acceptedReplies = replyMapper.selectList(
                new LambdaQueryWrapper<DiscussionReply>()
                        .eq(DiscussionReply::getDiscussionId, discussionId)
                        .eq(DiscussionReply::getIsAccepted, 1));
        acceptedReplies.forEach(r -> {
            r.setIsAccepted(0);
            replyMapper.updateById(r);
        });
        
        // 采纳当前回复
        reply.setIsAccepted(1);
        replyMapper.updateById(reply);
        
        // 标记讨论已解决
        discussion.setIsResolved(1);
        discussionMapper.updateById(discussion);
    }

    /**
     * 点赞回复
     */
    @Transactional
    public void likeReply(Long discussionId, Long replyId) {
        Long userId = userService.getCurrentUserId();
        
        // 检查是否已点赞
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, "reply")
                .eq(UserLike::getTargetId, replyId));
        
        if (count > 0) {
            throw new RuntimeException("已经点赞过了");
        }
        
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetType("reply");
        like.setTargetId(replyId);
        like.setIsLike(1);
        likeMapper.insert(like);
        
        // 更新点赞数
        DiscussionReply reply = replyMapper.selectById(replyId);
        if (reply != null) {
            reply.setLikeCount(reply.getLikeCount() + 1);
            replyMapper.updateById(reply);
        }
    }

    /**
     * 取消点赞回复
     */
    @Transactional
    public void unlikeReply(Long discussionId, Long replyId) {
        Long userId = userService.getCurrentUserId();
        
        int deleted = likeMapper.delete(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, "reply")
                .eq(UserLike::getTargetId, replyId));
        
        if (deleted > 0) {
            DiscussionReply reply = replyMapper.selectById(replyId);
            if (reply != null) {
                reply.setLikeCount(Math.max(0, reply.getLikeCount() - 1));
                replyMapper.updateById(reply);
            }
        }
    }

    // 辅助方法
    private void fillDiscussionInfo(Discussion discussion) {
        User author = userMapper.selectById(discussion.getAuthorId());
        if (author != null) {
            discussion.setAuthorName(author.getRealName() != null ? author.getRealName() : author.getUsername());
            discussion.setAuthorAvatar(author.getAvatar());
        }
    }

    private void fillReplyInfo(DiscussionReply reply, Long currentUserId) {
        User author = userMapper.selectById(reply.getAuthorId());
        if (author != null) {
            reply.setAuthorName(author.getRealName() != null ? author.getRealName() : author.getUsername());
            reply.setAuthorAvatar(author.getAvatar());
        }
        
        if (currentUserId != null) {
            reply.setIsLiked(likeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                    .eq(UserLike::getUserId, currentUserId)
                    .eq(UserLike::getTargetType, "reply")
                    .eq(UserLike::getTargetId, reply.getId())) > 0);
        }
    }
}
