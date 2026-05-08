package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardev.hub.common.PageResult;
import com.cardev.hub.dto.KnowledgeRequest;
import com.cardev.hub.entity.*;
import com.cardev.hub.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识服务
 */
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeTagMapper tagMapper;
    private final KnowledgeProjectMapper knowledgeProjectMapper;
    private final KnowledgeVersionMapper versionMapper;
    private final KnowledgeAttachmentMapper attachmentMapper;
    private final UserCollectMapper collectMapper;
    private final UserLikeMapper likeMapper;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * 获取知识列表
     */
    public PageResult<Knowledge> getList(String keyword, String type, String domain, String tag,
            Long authorId, Integer status, String sort, int page, int size) {
        Page<Knowledge> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Knowledge> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Knowledge::getTitle, keyword)
                    .or().like(Knowledge::getSummary, keyword));
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Knowledge::getType, type);
        }
        if (StringUtils.hasText(domain)) {
            wrapper.eq(Knowledge::getDomain, domain);
        }
        if (authorId != null) {
            wrapper.eq(Knowledge::getAuthorId, authorId);
        }
        if (status != null) {
            wrapper.eq(Knowledge::getStatus, status);
        } else {
            wrapper.eq(Knowledge::getStatus, 2); // 默认只显示已发布的
        }

        // 排序
        if ("views".equals(sort)) {
            wrapper.orderByDesc(Knowledge::getViewCount);
        } else if ("likes".equals(sort)) {
            wrapper.orderByDesc(Knowledge::getLikeCount);
        } else if ("collects".equals(sort)) {
            wrapper.orderByDesc(Knowledge::getCollectCount);
        } else {
            wrapper.orderByDesc(Knowledge::getCreateTime);
        }

        Page<Knowledge> result = knowledgeMapper.selectPage(pageParam, wrapper);

        // 填充额外信息
        result.getRecords().forEach(this::fillKnowledgeInfo);

        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 搜索知识
     */
    public PageResult<Knowledge> search(String keyword, String type, String domain, List<String> tags,
            String sort, int page, int size) {
        return getList(keyword, type, domain, null, null, 2, sort, page, size);
    }

    /**
     * 获取知识详情
     */
    public Knowledge getDetail(Long id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }

        // 增加浏览量
        knowledge.setViewCount(knowledge.getViewCount() + 1);
        knowledgeMapper.updateById(knowledge);

        // 填充额外信息
        fillKnowledgeInfo(knowledge);

        // 填充用户交互状态
        Long userId = userService.getCurrentUserId();
        if (userId != null) {
            knowledge.setIsLiked(isLiked(userId, id));
            knowledge.setIsCollected(isCollected(userId, id));
        }

        return knowledge;
    }

    /**
     * 创建知识（清除热门/推荐缓存）
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotKnowledge", allEntries = true),
            @CacheEvict(value = "recommendKnowledge", allEntries = true),
            @CacheEvict(value = "hotTags", allEntries = true)
    })
    public Long create(KnowledgeRequest request) {
        Long userId = userService.getCurrentUserId();

        Knowledge knowledge = new Knowledge();
        knowledge.setTitle(request.getTitle());
        knowledge.setType(request.getType());
        knowledge.setDomain(request.getDomain());
        knowledge.setSummary(request.getSummary());
        knowledge.setContent(request.getContent());
        knowledge.setAuthorId(userId);
        knowledge.setViewCount(0);
        knowledge.setLikeCount(0);
        knowledge.setDislikeCount(0);
        knowledge.setCollectCount(0);
        knowledge.setCommentCount(0);
        knowledge.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        knowledge.setVersion(1);

        knowledgeMapper.insert(knowledge);

        // 保存标签
        saveTags(knowledge.getId(), request.getTags());

        // 保存项目关联
        saveProjectLinks(knowledge.getId(), request.getProjectIds());

        // 保存版本历史
        saveVersion(knowledge, userId);

        return knowledge.getId();
    }

    /**
     * 更新知识（清除热门/推荐缓存）
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotKnowledge", allEntries = true),
            @CacheEvict(value = "recommendKnowledge", allEntries = true),
            @CacheEvict(value = "hotTags", allEntries = true)
    })
    public void update(Long id, KnowledgeRequest request) {
        Long userId = userService.getCurrentUserId();
        Knowledge knowledge = knowledgeMapper.selectById(id);

        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        if (!knowledge.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权修改此知识");
        }

        knowledge.setTitle(request.getTitle());
        knowledge.setType(request.getType());
        knowledge.setDomain(request.getDomain());
        knowledge.setSummary(request.getSummary());
        knowledge.setContent(request.getContent());
        if (request.getStatus() != null) {
            knowledge.setStatus(request.getStatus());
        }
        knowledge.setVersion(knowledge.getVersion() + 1);

        knowledgeMapper.updateById(knowledge);

        // 更新标签
        tagMapper.delete(new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getKnowledgeId, id));
        saveTags(id, request.getTags());

        // 更新项目关联
        knowledgeProjectMapper
                .delete(new LambdaQueryWrapper<KnowledgeProject>().eq(KnowledgeProject::getKnowledgeId, id));
        saveProjectLinks(id, request.getProjectIds());

        // 保存版本历史
        saveVersion(knowledge, userId);
    }

    /**
     * 删除知识（清除热门/推荐缓存）
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotKnowledge", allEntries = true),
            @CacheEvict(value = "recommendKnowledge", allEntries = true),
            @CacheEvict(value = "hotTags", allEntries = true)
    })
    public void delete(Long id) {
        Long userId = userService.getCurrentUserId();
        Knowledge knowledge = knowledgeMapper.selectById(id);

        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        if (!knowledge.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权删除此知识");
        }

        knowledgeMapper.deleteById(id);
        tagMapper.delete(new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getKnowledgeId, id));
        knowledgeProjectMapper
                .delete(new LambdaQueryWrapper<KnowledgeProject>().eq(KnowledgeProject::getKnowledgeId, id));
    }

    /**
     * 点赞知识（清除推荐缓存，因为推荐按点赞排序）
     */
    @Transactional
    @CacheEvict(value = "recommendKnowledge", allEntries = true)
    public void like(Long id) {
        Long userId = userService.getCurrentUserId();

        // 检查是否已点赞
        UserLike existLike = likeMapper.selectOne(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, "knowledge")
                .eq(UserLike::getTargetId, id));

        if (existLike != null) {
            throw new RuntimeException("已经点赞过了");
        }

        UserLike userLike = new UserLike();
        userLike.setUserId(userId);
        userLike.setTargetType("knowledge");
        userLike.setTargetId(id);
        userLike.setIsLike(1);
        likeMapper.insert(userLike);

        // 更新点赞数
        Knowledge knowledge = knowledgeMapper.selectById(id);
        knowledge.setLikeCount(knowledge.getLikeCount() + 1);
        knowledgeMapper.updateById(knowledge);

        // 发送通知
        if (!knowledge.getAuthorId().equals(userId)) {
            User currentUser = userMapper.selectById(userId);
            String name = currentUser != null ? (currentUser.getRealName() != null ? currentUser.getRealName() : currentUser.getUsername()) : "某人";
            notificationService.sendNotification(
                knowledge.getAuthorId(),
                "新点赞",
                name + " 点赞了你的内容: 《" + knowledge.getTitle() + "》",
                "like",
                id
            );
        }
    }

    /**
     * 取消点赞（清除推荐缓存）
     */
    @Transactional
    @CacheEvict(value = "recommendKnowledge", allEntries = true)
    public void unlike(Long id) {
        Long userId = userService.getCurrentUserId();

        int deleted = likeMapper.delete(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, "knowledge")
                .eq(UserLike::getTargetId, id));

        if (deleted > 0) {
            Knowledge knowledge = knowledgeMapper.selectById(id);
            knowledge.setLikeCount(Math.max(0, knowledge.getLikeCount() - 1));
            knowledgeMapper.updateById(knowledge);
        }
    }

    /**
     * 收藏知识
     */
    @Transactional
    public void collect(Long id, Long folderId) {
        Long userId = userService.getCurrentUserId();

        // 检查是否已收藏
        UserCollect existCollect = collectMapper.selectOne(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getKnowledgeId, id));

        if (existCollect != null) {
            throw new RuntimeException("已经收藏过了");
        }

        UserCollect collect = new UserCollect();
        collect.setUserId(userId);
        collect.setKnowledgeId(id);
        collect.setFolderId(folderId);
        collectMapper.insert(collect);

        // 更新收藏数
        Knowledge knowledge = knowledgeMapper.selectById(id);
        knowledge.setCollectCount(knowledge.getCollectCount() + 1);
        knowledgeMapper.updateById(knowledge);
    }

    /**
     * 取消收藏
     */
    @Transactional
    public void uncollect(Long id) {
        Long userId = userService.getCurrentUserId();

        int deleted = collectMapper.delete(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getKnowledgeId, id));

        if (deleted > 0) {
            Knowledge knowledge = knowledgeMapper.selectById(id);
            knowledge.setCollectCount(Math.max(0, knowledge.getCollectCount() - 1));
            knowledgeMapper.updateById(knowledge);
        }
    }

    /**
     * 获取我的收藏
     */
    public PageResult<Knowledge> getMyCollections(Long folderId, int page, int size) {
        Long userId = userService.getCurrentUserId();

        Page<UserCollect> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollect::getUserId, userId);
        if (folderId != null) {
            wrapper.eq(UserCollect::getFolderId, folderId);
        }
        wrapper.orderByDesc(UserCollect::getCreateTime);

        Page<UserCollect> collectPage = collectMapper.selectPage(pageParam, wrapper);

        List<Knowledge> knowledgeList = collectPage.getRecords().stream()
                .map(c -> knowledgeMapper.selectById(c.getKnowledgeId()))
                .filter(k -> k != null)
                .peek(this::fillKnowledgeInfo)
                .collect(Collectors.toList());

        return PageResult.of(knowledgeList, collectPage.getTotal(), page, size);
    }

    /**
     * 获取知识版本历史
     */
    public List<KnowledgeVersion> getVersions(Long id) {
        List<KnowledgeVersion> versions = versionMapper.selectList(
                new LambdaQueryWrapper<KnowledgeVersion>()
                        .eq(KnowledgeVersion::getKnowledgeId, id)
                        .orderByDesc(KnowledgeVersion::getVersion));

        versions.forEach(v -> {
            User user = userMapper.selectById(v.getOperatorId());
            if (user != null) {
                v.setOperatorName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
        });

        return versions;
    }

    /**
     * 回滚版本
     */
    @Transactional
    public void rollbackVersion(Long id, Long versionId) {
        Long userId = userService.getCurrentUserId();
        Knowledge knowledge = knowledgeMapper.selectById(id);

        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        if (!knowledge.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权操作此知识");
        }

        KnowledgeVersion version = versionMapper.selectById(versionId);
        if (version == null || !version.getKnowledgeId().equals(id)) {
            throw new RuntimeException("版本不存在");
        }

        knowledge.setTitle(version.getTitle());
        knowledge.setSummary(version.getSummary());
        knowledge.setContent(version.getContent());
        knowledge.setVersion(knowledge.getVersion() + 1);
        knowledgeMapper.updateById(knowledge);

        // 保存新版本
        saveVersion(knowledge, userId);
    }

    /**
     * 获取热门知识（缓存至Redis，10分钟过期）
     */
    @Cacheable(value = "hotKnowledge", key = "#limit")
    public List<Knowledge> getHotKnowledge(int limit) {
        List<Knowledge> list = knowledgeMapper.selectList(
                new LambdaQueryWrapper<Knowledge>()
                        .eq(Knowledge::getStatus, 2)
                        .orderByDesc(Knowledge::getViewCount)
                        .last("LIMIT " + limit));
        list.forEach(this::fillKnowledgeInfo);
        return list;
    }

    /**
     * 获取推荐知识（缓存至Redis，10分钟过期）
     */
    @Cacheable(value = "recommendKnowledge", key = "#limit")
    public List<Knowledge> getRecommendKnowledge(int limit) {
        List<Knowledge> list = knowledgeMapper.selectList(
                new LambdaQueryWrapper<Knowledge>()
                        .eq(Knowledge::getStatus, 2)
                        .orderByDesc(Knowledge::getLikeCount)
                        .last("LIMIT " + limit));
        list.forEach(this::fillKnowledgeInfo);
        return list;
    }

    /**
     * 获取热门标签（缓存至Redis，15分钟过期）
     */
    @Cacheable(value = "hotTags", key = "#limit")
    public List<String> getHotTags(int limit) {
        // 简单实现：获取所有标签并按出现次数排序
        List<KnowledgeTag> tags = tagMapper.selectList(null);
        return tags.stream()
                .collect(Collectors.groupingBy(KnowledgeTag::getTagName, Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }

    /**
     * 审核通过
     */
    @Transactional
    public void approve(Long id) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        knowledge.setStatus(2);
        knowledge.setAuditorId(userService.getCurrentUserId());
        knowledge.setAuditTime(java.time.LocalDateTime.now());
        knowledgeMapper.updateById(knowledge);

        // 发送审核通过通知
        notificationService.sendNotification(
            knowledge.getAuthorId(),
            "审核通过",
            "您的知识《" + knowledge.getTitle() + "》已通过审核并成功发布。",
            "audit",
            id
        );
    }

    /**
     * 审核拒绝
     */
    @Transactional
    public void reject(Long id, String reason) {
        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        knowledge.setStatus(3);
        knowledge.setRejectReason(reason);
        knowledge.setAuditorId(userService.getCurrentUserId());
        knowledge.setAuditTime(java.time.LocalDateTime.now());
        knowledgeMapper.updateById(knowledge);

        // 发送审核驳回通知
        notificationService.sendNotification(
            knowledge.getAuthorId(),
            "审核被驳回",
            "您的知识《" + knowledge.getTitle() + "》未通过审核。驳回原因：" + reason,
            "audit",
            id
        );
    }

    // 辅助方法
    private void fillKnowledgeInfo(Knowledge knowledge) {
        // 填充作者信息
        User author = userMapper.selectById(knowledge.getAuthorId());
        if (author != null) {
            knowledge.setAuthorName(author.getRealName() != null ? author.getRealName() : author.getUsername());
            knowledge.setAuthorAvatar(author.getAvatar());
            knowledge.setAuthorDepartment(author.getDepartment());
        }

        // 填充审核人信息
        if (knowledge.getAuditorId() != null) {
            User auditor = userMapper.selectById(knowledge.getAuditorId());
            if (auditor != null) {
                knowledge.setAuditorName(auditor.getRealName() != null ? auditor.getRealName() : auditor.getUsername());
            }
        }

        // 填充标签
        List<KnowledgeTag> tags = tagMapper.selectList(
                new LambdaQueryWrapper<KnowledgeTag>().eq(KnowledgeTag::getKnowledgeId, knowledge.getId()));
        knowledge.setTags(tags.stream().map(KnowledgeTag::getTagName).collect(Collectors.toList()));

        // 填充关联项目ID
        List<KnowledgeProject> projects = knowledgeProjectMapper.selectList(
                new LambdaQueryWrapper<KnowledgeProject>().eq(KnowledgeProject::getKnowledgeId, knowledge.getId()));
        knowledge.setProjectIds(projects.stream().map(KnowledgeProject::getProjectId).collect(Collectors.toList()));
    }

    private void saveTags(Long knowledgeId, List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(tag -> {
                KnowledgeTag knowledgeTag = new KnowledgeTag();
                knowledgeTag.setKnowledgeId(knowledgeId);
                knowledgeTag.setTagName(tag);
                tagMapper.insert(knowledgeTag);
            });
        }
    }

    private void saveProjectLinks(Long knowledgeId, List<Long> projectIds) {
        if (projectIds != null && !projectIds.isEmpty()) {
            projectIds.forEach(projectId -> {
                KnowledgeProject kp = new KnowledgeProject();
                kp.setKnowledgeId(knowledgeId);
                kp.setProjectId(projectId);
                knowledgeProjectMapper.insert(kp);
            });
        }
    }

    private void saveVersion(Knowledge knowledge, Long operatorId) {
        KnowledgeVersion version = new KnowledgeVersion();
        version.setKnowledgeId(knowledge.getId());
        version.setVersion(knowledge.getVersion());
        version.setTitle(knowledge.getTitle());
        version.setSummary(knowledge.getSummary());
        version.setContent(knowledge.getContent());
        version.setOperatorId(operatorId);
        versionMapper.insert(version);
    }

    private boolean isLiked(Long userId, Long knowledgeId) {
        return likeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, "knowledge")
                .eq(UserLike::getTargetId, knowledgeId)) > 0;
    }

    private boolean isCollected(Long userId, Long knowledgeId) {
        return collectMapper.selectCount(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getKnowledgeId, knowledgeId)) > 0;
    }
}
