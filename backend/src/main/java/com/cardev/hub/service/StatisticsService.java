package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cardev.hub.entity.*;
import com.cardev.hub.mapper.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Service
@RequiredArgsConstructor
public class StatisticsService {

        private final KnowledgeMapper knowledgeMapper;
        private final UserMapper userMapper;
        private final DiscussionMapper discussionMapper;
        private final ProjectMapper projectMapper;
        private final UserCollectMapper collectMapper;
        private final UserLikeMapper likeMapper;

        /**
         * 获取仪表盘数据（缓存至Redis，5分钟过期）
         */
        @Cacheable(value = "statistics", key = "'dashboard'")
        public Map<String, Object> getDashboardData() {
                Map<String, Object> data = new HashMap<>();

                // 知识总数
                data.put("knowledgeCount", knowledgeMapper.selectCount(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2)));

                // 用户总数
                data.put("userCount", userMapper.selectCount(
                                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));

                // 讨论总数
                data.put("discussionCount", discussionMapper.selectCount(null));

                // 项目总数
                data.put("projectCount", projectMapper.selectCount(null));

                // 今日新增知识
                LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                data.put("todayKnowledge", knowledgeMapper.selectCount(
                                new LambdaQueryWrapper<Knowledge>()
                                                .ge(Knowledge::getCreateTime, todayStart)
                                                .eq(Knowledge::getStatus, 2)));

                // 今日新增用户
                data.put("todayUsers", userMapper.selectCount(
                                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));

                return data;
        }

        /**
         * 获取知识统计
         */
        public Map<String, Object> getKnowledgeStats() {
                Map<String, Object> stats = new HashMap<>();

                // 总数
                stats.put("total", knowledgeMapper.selectCount(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2)));

                // 本月新增
                LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                stats.put("monthlyNew", knowledgeMapper.selectCount(
                                new LambdaQueryWrapper<Knowledge>()
                                                .ge(Knowledge::getCreateTime, monthStart)
                                                .eq(Knowledge::getStatus, 2)));

                // 总浏览量
                List<Knowledge> allKnowledge = knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2));
                stats.put("totalViews", allKnowledge.stream().mapToInt(Knowledge::getViewCount).sum());
                stats.put("totalLikes", allKnowledge.stream().mapToInt(Knowledge::getLikeCount).sum());
                stats.put("totalCollects", allKnowledge.stream().mapToInt(Knowledge::getCollectCount).sum());

                return stats;
        }

        /**
         * 获取领域分布（缓存至Redis，5分钟过期）
         */
        @Cacheable(value = "statistics", key = "'domainDistribution'")
        public List<Map<String, Object>> getDomainDistribution() {
                List<Knowledge> knowledgeList = knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2));

                return knowledgeList.stream()
                                .collect(Collectors.groupingBy(Knowledge::getDomain, Collectors.counting()))
                                .entrySet().stream()
                                .map(e -> {
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("name", e.getKey());
                                        item.put("value", e.getValue());
                                        return item;
                                })
                                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                                .collect(Collectors.toList());
        }

        /**
         * 获取类型分布（缓存至Redis，5分钟过期）
         */
        @Cacheable(value = "statistics", key = "'typeDistribution'")
        public List<Map<String, Object>> getTypeDistribution() {
                List<Knowledge> knowledgeList = knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2));

                return knowledgeList.stream()
                                .collect(Collectors.groupingBy(Knowledge::getType, Collectors.counting()))
                                .entrySet().stream()
                                .map(e -> {
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("name", e.getKey());
                                        item.put("value", e.getValue());
                                        return item;
                                })
                                .sorted((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")))
                                .collect(Collectors.toList());
        }

        /**
         * 获取知识增长趋势
         */
        public List<Map<String, Object>> getKnowledgeTrend(int days) {
                List<Map<String, Object>> trend = new ArrayList<>();
                LocalDate today = LocalDate.now();

                for (int i = days - 1; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        LocalDateTime start = date.atStartOfDay();
                        LocalDateTime end = date.plusDays(1).atStartOfDay();

                        Long count = knowledgeMapper.selectCount(
                                        new LambdaQueryWrapper<Knowledge>()
                                                        .ge(Knowledge::getCreateTime, start)
                                                        .lt(Knowledge::getCreateTime, end)
                                                        .eq(Knowledge::getStatus, 2));

                        Map<String, Object> item = new HashMap<>();
                        item.put("date", date.toString());
                        item.put("count", count);
                        trend.add(item);
                }

                return trend;
        }

        /**
         * 获取用户贡献排行榜
         */
        public List<Map<String, Object>> getContributionRanking(int limit) {
                List<Knowledge> knowledgeList = knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2));

                Map<Long, Long> countByUser = knowledgeList.stream()
                                .collect(Collectors.groupingBy(Knowledge::getAuthorId, Collectors.counting()));

                return countByUser.entrySet().stream()
                                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                                .limit(limit)
                                .map(e -> {
                                        User user = userMapper.selectById(e.getKey());
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("userId", e.getKey());
                                        String displayName = user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "未知用户";
                                        item.put("username", displayName);
                                        item.put("name", displayName);
                                        item.put("avatar", user != null ? user.getAvatar() : null);
                                        item.put("department", user != null ? user.getDepartment() : "未知部门");
                                        item.put("count", e.getValue());
                                        item.put("score", e.getValue());
                                        return item;
                                })
                                .collect(Collectors.toList());
        }

        /**
         * 获取热门知识排行榜
         */
        public List<Knowledge> getHotKnowledgeRanking(int limit) {
                return knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>()
                                                .eq(Knowledge::getStatus, 2)
                                                .orderByDesc(Knowledge::getViewCount)
                                                .last("LIMIT " + limit));
        }

        /**
         * 获取用户贡献统计
         */
        public Map<String, Object> getUserContributionStats(Long userId) {
                Map<String, Object> stats = new HashMap<>();

                // 上传的知识数
                stats.put("knowledgeCount", knowledgeMapper.selectCount(
                                new LambdaQueryWrapper<Knowledge>()
                                                .eq(Knowledge::getAuthorId, userId)
                                                .eq(Knowledge::getStatus, 2)));

                // 获得的点赞数
                List<Knowledge> userKnowledge = knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>()
                                                .eq(Knowledge::getAuthorId, userId)
                                                .eq(Knowledge::getStatus, 2));
                stats.put("totalLikes", userKnowledge.stream().mapToInt(Knowledge::getLikeCount).sum());
                stats.put("totalCollects", userKnowledge.stream().mapToInt(Knowledge::getCollectCount).sum());
                stats.put("totalViews", userKnowledge.stream().mapToInt(Knowledge::getViewCount).sum());

                // 发起的讨论数
                stats.put("discussionCount", discussionMapper.selectCount(
                                new LambdaQueryWrapper<Discussion>().eq(Discussion::getAuthorId, userId)));

                return stats;
        }

        /**
         * 获取系统使用统计
         */
        public Map<String, Object> getSystemStats() {
                Map<String, Object> stats = new HashMap<>();

                // 活跃用户数（最近7天有登录）
                LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
                stats.put("activeUsers", userMapper.selectCount(
                                new LambdaQueryWrapper<User>()
                                                .eq(User::getStatus, 1)
                                                .ge(User::getUpdateTime, weekAgo)));

                // 总知识数
                stats.put("totalKnowledge", knowledgeMapper.selectCount(null));

                // 总讨论数
                stats.put("totalDiscussions", discussionMapper.selectCount(null));

                // 已解决的讨论数
                stats.put("resolvedDiscussions", discussionMapper.selectCount(
                                new LambdaQueryWrapper<Discussion>().eq(Discussion::getIsResolved, 1)));

                return stats;
        }

        /**
         * 获取活跃用户统计
         */
        public List<Map<String, Object>> getActiveUserStats(int days) {
                List<Map<String, Object>> stats = new ArrayList<>();
                LocalDate today = LocalDate.now();

                for (int i = days - 1; i >= 0; i--) {
                        LocalDate date = today.minusDays(i);
                        LocalDateTime start = date.atStartOfDay();
                        LocalDateTime end = date.plusDays(1).atStartOfDay();

                        // 简单实现：统计当天有更新时间的用户数
                        Long count = userMapper.selectCount(
                                        new LambdaQueryWrapper<User>()
                                                        .ge(User::getUpdateTime, start)
                                                        .lt(User::getUpdateTime, end));

                        Map<String, Object> item = new HashMap<>();
                        item.put("date", date.toString());
                        item.put("count", count);
                        stats.add(item);
                }

                return stats;
        }
        /**
         * 获取部门贡献排行榜
         */
        public List<Map<String, Object>> getDepartmentRanking(int limit) {
                List<Knowledge> knowledgeList = knowledgeMapper.selectList(
                                new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getStatus, 2));

                // 按作者ID分组，获取每个作者的部门
                Map<String, Long> countByDept = new HashMap<>();
                for (Knowledge k : knowledgeList) {
                        User user = userMapper.selectById(k.getAuthorId());
                        String dept = (user != null && user.getDepartment() != null) ? user.getDepartment() : "未知部门";
                        countByDept.merge(dept, 1L, Long::sum);
                }

                return countByDept.entrySet().stream()
                                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                                .limit(limit)
                                .map(e -> {
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("name", e.getKey());
                                        item.put("count", e.getValue());
                                        return item;
                                })
                                .collect(Collectors.toList());
        }
}
