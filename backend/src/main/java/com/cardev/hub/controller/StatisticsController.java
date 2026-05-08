package com.cardev.hub.controller;

import com.cardev.hub.common.Result;
import com.cardev.hub.entity.Knowledge;
import com.cardev.hub.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 */
@Tag(name = "统计接口")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = statisticsService.getDashboardData();
        return Result.success(data);
    }

    @Operation(summary = "获取知识统计")
    @GetMapping("/knowledge")
    public Result<Map<String, Object>> getKnowledgeStats() {
        Map<String, Object> stats = statisticsService.getKnowledgeStats();
        return Result.success(stats);
    }

    @Operation(summary = "获取领域分布")
    @GetMapping("/domain-distribution")
    public Result<List<Map<String, Object>>> getDomainDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getDomainDistribution();
        return Result.success(distribution);
    }

    @Operation(summary = "获取类型分布")
    @GetMapping("/type-distribution")
    public Result<List<Map<String, Object>>> getTypeDistribution() {
        List<Map<String, Object>> distribution = statisticsService.getTypeDistribution();
        return Result.success(distribution);
    }

    @Operation(summary = "获取知识增长趋势")
    @GetMapping("/knowledge-trend")
    public Result<List<Map<String, Object>>> getKnowledgeTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = statisticsService.getKnowledgeTrend(days);
        return Result.success(trend);
    }

    @Operation(summary = "获取用户贡献排行榜")
    @GetMapping("/contribution-ranking")
    public Result<List<Map<String, Object>>> getContributionRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> ranking = statisticsService.getContributionRanking(limit);
        return Result.success(ranking);
    }

    @Operation(summary = "获取热门知识排行榜")
    @GetMapping("/hot-knowledge")
    public Result<List<Knowledge>> getHotKnowledgeRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<Knowledge> ranking = statisticsService.getHotKnowledgeRanking(limit);
        return Result.success(ranking);
    }

    @Operation(summary = "获取用户贡献统计")
    @GetMapping("/contribution")
    public Result<Map<String, Object>> getUserContributionStats(
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = statisticsService.getUserContributionStats(userId);
        return Result.success(stats);
    }

    @Operation(summary = "获取系统使用统计")
    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = statisticsService.getSystemStats();
        return Result.success(stats);
    }

    @Operation(summary = "获取活跃用户统计")
    @GetMapping("/active-users")
    public Result<List<Map<String, Object>>> getActiveUserStats(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> stats = statisticsService.getActiveUserStats(days);
        return Result.success(stats);
    }
    @Operation(summary = "获取部门贡献排行榜")
    @GetMapping("/department-ranking")
    public Result<List<Map<String, Object>>> getDepartmentRanking(
            @RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> ranking = statisticsService.getDepartmentRanking(limit);
        return Result.success(ranking);
    }
}
