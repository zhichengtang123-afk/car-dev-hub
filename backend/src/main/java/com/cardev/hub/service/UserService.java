package com.cardev.hub.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardev.hub.common.PageResult;
import com.cardev.hub.dto.*;
import com.cardev.hub.entity.User;
import com.cardev.hub.mapper.UserMapper;
import com.cardev.hub.security.JwtUtils;
import com.cardev.hub.security.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 检查邮箱是否已存在
        if (existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRealName(request.getRealName());
        user.setDepartment(request.getDepartment());
        user.setPosition(request.getPosition());
        user.setRole("DEVELOPER"); // 默认角色为研发人员
        user.setStatus(1);
        user.setLoginFailCount(0);

        userMapper.insert(user);
    }

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        User user = findByUsernameOrPhoneOrEmail(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查账户是否被锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("账户已被锁定，请30分钟后重试");
        }

        // 检查账户状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 增加登录失败次数
            int failCount = user.getLoginFailCount() + 1;
            user.setLoginFailCount(failCount);
            
            if (failCount >= 5) {
                user.setLockTime(LocalDateTime.now().plusMinutes(30));
                userMapper.updateById(user);
                throw new RuntimeException("密码错误次数过多，账户已被锁定30分钟");
            }
            
            userMapper.updateById(user);
            throw new RuntimeException("密码错误");
        }

        // 重置登录失败次数
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setDepartment(user.getDepartment());
        userInfo.setPosition(user.getPosition());
        userInfo.setRole(user.getRole());
        response.setUser(userInfo);

        return response;
    }

    /**
     * 获取当前用户信息
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUserDetails userDetails) {
            return userMapper.selectById(userDetails.getId());
        }
        throw new RuntimeException("未登录");
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public void updateUserInfo(UpdateUserRequest request) {
        User user = getCurrentUser();
        
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            // 检查手机号是否被其他用户使用
            User existUser = findByPhone(request.getPhone());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new RuntimeException("手机号已被使用");
            }
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getEmail())) {
            // 检查邮箱是否被其他用户使用
            User existUser = findByEmail(request.getEmail());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                throw new RuntimeException("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userMapper.updateById(user);
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        
        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 获取用户列表（管理员）
     */
    public PageResult<User> getUserList(String keyword, String role, Integer status, int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword)
            );
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        
        // 清除密码信息
        result.getRecords().forEach(u -> u.setPassword(null));
        
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    /**
     * 更新用户角色
     */
    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRole(role);
        userMapper.updateById(user);
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public void resetUserPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
    }

    /**
     * 根据ID获取用户
     */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    // 辅助方法
    private boolean existsByUsername(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username)) > 0;
    }

    private boolean existsByPhone(String phone) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0;
    }

    private boolean existsByEmail(String email) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private User findByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    private User findByEmail(String email) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    private User findByUsernameOrPhoneOrEmail(String identifier) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, identifier)
                .or()
                .eq(User::getPhone, identifier)
                .or()
                .eq(User::getEmail, identifier)
        );
    }
}
