package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardev.hub.common.PageResult;
import com.cardev.hub.dto.ProjectRequest;
import com.cardev.hub.entity.*;
import com.cardev.hub.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目服务
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper memberMapper;
    private final KnowledgeProjectMapper knowledgeProjectMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final UserMapper userMapper;
    private final UserService userService;

    /**
     * 获取项目列表
     */
    public PageResult<Project> getList(String keyword, Integer status, int page, int size) {
        Page<Project> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Project::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        
        wrapper.orderByDesc(Project::getCreateTime);
        
        Page<Project> result = projectMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillProjectInfo);
        
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 获取项目详情
     */
    public Project getDetail(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        fillProjectInfo(project);
        return project;
    }

    /**
     * 创建项目
     */
    @Transactional
    public Long create(ProjectRequest request) {
        Long userId = userService.getCurrentUserId();
        
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setLeaderId(request.getLeaderId() != null ? request.getLeaderId() : userId);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus(request.getStatus() != null ? request.getStatus() : 0);
        
        projectMapper.insert(project);
        
        // 添加负责人为成员
        addMember(project.getId(), project.getLeaderId());
        
        return project.getId();
    }

    /**
     * 更新项目
     */
    @Transactional
    public void update(Long id, ProjectRequest request) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        if (request.getLeaderId() != null) {
            project.setLeaderId(request.getLeaderId());
        }
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        
        projectMapper.updateById(project);
    }

    /**
     * 删除项目
     */
    @Transactional
    public void delete(Long id) {
        projectMapper.deleteById(id);
        memberMapper.delete(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, id));
        knowledgeProjectMapper.delete(new LambdaQueryWrapper<KnowledgeProject>().eq(KnowledgeProject::getProjectId, id));
    }

    /**
     * 获取项目成员
     */
    public List<User> getMembers(Long projectId) {
        List<ProjectMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, projectId));
        
        return members.stream()
                .map(m -> userMapper.selectById(m.getUserId()))
                .filter(u -> u != null)
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
    }

    /**
     * 添加项目成员
     */
    @Transactional
    public void addMember(Long projectId, Long userId) {
        // 检查是否已是成员
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
        
        if (count > 0) {
            return; // 已是成员
        }
        
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        memberMapper.insert(member);
    }

    /**
     * 移除项目成员
     */
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        memberMapper.delete(new LambdaQueryWrapper<ProjectMember>()
                .eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId));
    }

    /**
     * 获取项目关联的知识
     */
    public PageResult<Knowledge> getProjectKnowledge(Long projectId, int page, int size) {
        Page<KnowledgeProject> pageParam = new Page<>(page, size);
        
        Page<KnowledgeProject> kpPage = knowledgeProjectMapper.selectPage(pageParam,
                new LambdaQueryWrapper<KnowledgeProject>().eq(KnowledgeProject::getProjectId, projectId));
        
        List<Knowledge> knowledgeList = kpPage.getRecords().stream()
                .map(kp -> knowledgeMapper.selectById(kp.getKnowledgeId()))
                .filter(k -> k != null)
                .collect(Collectors.toList());
        
        return PageResult.of(knowledgeList, kpPage.getTotal(), page, size);
    }

    /**
     * 关联知识到项目
     */
    @Transactional
    public void linkKnowledge(Long projectId, Long knowledgeId) {
        // 检查是否已关联
        Long count = knowledgeProjectMapper.selectCount(new LambdaQueryWrapper<KnowledgeProject>()
                .eq(KnowledgeProject::getProjectId, projectId)
                .eq(KnowledgeProject::getKnowledgeId, knowledgeId));
        
        if (count > 0) {
            throw new RuntimeException("知识已关联到该项目");
        }
        
        KnowledgeProject kp = new KnowledgeProject();
        kp.setProjectId(projectId);
        kp.setKnowledgeId(knowledgeId);
        knowledgeProjectMapper.insert(kp);
    }

    /**
     * 取消关联
     */
    @Transactional
    public void unlinkKnowledge(Long projectId, Long knowledgeId) {
        knowledgeProjectMapper.delete(new LambdaQueryWrapper<KnowledgeProject>()
                .eq(KnowledgeProject::getProjectId, projectId)
                .eq(KnowledgeProject::getKnowledgeId, knowledgeId));
    }

    // 辅助方法
    private void fillProjectInfo(Project project) {
        // 填充负责人信息
        User leader = userMapper.selectById(project.getLeaderId());
        if (leader != null) {
            project.setLeaderName(leader.getRealName() != null ? leader.getRealName() : leader.getUsername());
        }
        
        // 填充成员数
        Long memberCount = memberMapper.selectCount(
                new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getProjectId, project.getId()));
        project.setMemberCount(memberCount.intValue());
        
        // 填充知识数
        Long knowledgeCount = knowledgeProjectMapper.selectCount(
                new LambdaQueryWrapper<KnowledgeProject>().eq(KnowledgeProject::getProjectId, project.getId()));
        project.setKnowledgeCount(knowledgeCount.intValue());
    }
}
