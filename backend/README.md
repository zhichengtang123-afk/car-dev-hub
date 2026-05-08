# 汽车研发知识共享平台 - 后端

## 项目介绍

基于 Spring Boot 3.2 + MyBatis Plus 的汽车研发知识共享平台后端服务。

## 技术栈

- **框架**: Spring Boot 3.2.1
- **ORM**: MyBatis Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **API文档**: Knife4j (Swagger)
- **工具**: Lombok, Hutool

## 项目结构

```
src/main/java/com/cardev/hub/
├── CarDevHubApplication.java    # 启动类
├── common/                       # 通用类
│   ├── Result.java              # 统一响应
│   └── PageResult.java          # 分页结果
├── config/                       # 配置类
│   ├── MybatisPlusConfig.java
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── WebMvcConfig.java
├── controller/                   # 控制器层
│   ├── AuthController.java      # 认证接口
│   ├── UserController.java      # 用户接口
│   ├── KnowledgeController.java # 知识接口
│   ├── ProjectController.java   # 项目接口
│   ├── DiscussionController.java # 讨论接口
│   └── StatisticsController.java # 统计接口
├── dto/                          # 数据传输对象
├── entity/                       # 实体类
├── exception/                    # 异常处理
├── mapper/                       # Mapper接口
├── security/                     # 安全相关
│   ├── JwtUtils.java
│   ├── JwtAuthenticationFilter.java
│   ├── SecurityUserDetails.java
│   └── UserDetailsServiceImpl.java
└── service/                      # 业务逻辑层
    ├── UserService.java
    ├── KnowledgeService.java
    ├── ProjectService.java
    ├── DiscussionService.java
    ├── CommentService.java
    ├── StatisticsService.java
    ├── FileService.java
    └── SysConfigService.java
```

## 快速开始

### 1. 准备环境

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. 初始化数据库

```sql
-- 执行 sql/init.sql 初始化数据库和表
source sql/init.sql;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库和Redis连接配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/car_dev_hub
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动项目

```bash
cd backend
mvn spring-boot:run
```

### 5. 访问API文档

启动后访问: http://localhost:8080/api/doc.html

## 默认账户

| 用户名 | 密码 | 角色 |
|-------|------|-----|
| admin | 123456 | 管理员 |
| zhangwei | 123456 | 技术专家 |
| liming | 123456 | 技术专家 |
| wangfang | 123456 | 研发人员 |

## API接口说明

### 认证接口 `/auth`
- POST `/auth/register` - 用户注册
- POST `/auth/login` - 用户登录

### 用户接口 `/user`
- GET `/user/info` - 获取当前用户信息
- PUT `/user/info` - 更新用户信息
- PUT `/user/password` - 修改密码
- GET `/user/list` - 获取用户列表（管理员）
- PUT `/user/{id}/status` - 更新用户状态
- PUT `/user/{id}/role` - 更新用户角色
- POST `/user/{id}/reset-password` - 重置密码

### 知识接口 `/knowledge`
- GET `/knowledge/list` - 获取知识列表
- GET `/knowledge/search` - 搜索知识
- GET `/knowledge/{id}` - 获取知识详情
- POST `/knowledge` - 创建知识
- PUT `/knowledge/{id}` - 更新知识
- DELETE `/knowledge/{id}` - 删除知识
- POST `/knowledge/upload` - 上传附件
- GET `/knowledge/attachment/{id}/download` - 下载附件
- POST `/knowledge/{id}/like` - 点赞
- DELETE `/knowledge/{id}/like` - 取消点赞
- POST `/knowledge/{id}/collect` - 收藏
- DELETE `/knowledge/{id}/collect` - 取消收藏
- GET `/knowledge/collections` - 我的收藏
- GET `/knowledge/{id}/comments` - 获取评论
- POST `/knowledge/{id}/comments` - 发表评论
- GET `/knowledge/{id}/versions` - 版本历史
- POST `/knowledge/{id}/rollback/{versionId}` - 回滚版本
- GET `/knowledge/types` - 知识类型
- GET `/knowledge/domains` - 知识领域
- GET `/knowledge/tags/hot` - 热门标签
- GET `/knowledge/hot` - 热门知识
- GET `/knowledge/recommend` - 推荐知识

### 项目接口 `/project`
- GET `/project/list` - 获取项目列表
- GET `/project/{id}` - 获取项目详情
- POST `/project` - 创建项目
- PUT `/project/{id}` - 更新项目
- DELETE `/project/{id}` - 删除项目
- GET `/project/{id}/members` - 获取成员
- POST `/project/{id}/members` - 添加成员
- DELETE `/project/{id}/members/{userId}` - 移除成员
- GET `/project/{id}/knowledge` - 项目知识
- POST `/project/{projectId}/knowledge/{knowledgeId}` - 关联知识
- DELETE `/project/{projectId}/knowledge/{knowledgeId}` - 取消关联

### 讨论接口 `/discussion`
- GET `/discussion/list` - 获取讨论列表
- GET `/discussion/{id}` - 获取讨论详情
- POST `/discussion` - 创建讨论
- PUT `/discussion/{id}` - 更新讨论
- DELETE `/discussion/{id}` - 删除讨论
- GET `/discussion/{id}/replies` - 获取回复
- POST `/discussion/{id}/replies` - 创建回复
- DELETE `/discussion/{id}/replies/{replyId}` - 删除回复
- POST `/discussion/{id}/replies/{replyId}/accept` - 采纳回复
- POST `/discussion/{id}/replies/{replyId}/like` - 点赞回复

### 统计接口 `/statistics`
- GET `/statistics/dashboard` - 仪表盘数据
- GET `/statistics/knowledge` - 知识统计
- GET `/statistics/domain-distribution` - 领域分布
- GET `/statistics/type-distribution` - 类型分布
- GET `/statistics/knowledge-trend` - 增长趋势
- GET `/statistics/contribution-ranking` - 贡献排行
- GET `/statistics/hot-knowledge` - 热门知识
- GET `/statistics/contribution` - 贡献统计
- GET `/statistics/system` - 系统统计
- GET `/statistics/active-users` - 活跃用户

## 注意事项

1. 所有需要认证的接口需要在请求头中添加 `Authorization: Bearer {token}`
2. 文件上传大小限制为 100MB
3. 支持的文件类型: pdf, doc, docx, xls, xlsx, ppt, pptx, jpg, jpeg, png, gif, zip, rar, 7z
