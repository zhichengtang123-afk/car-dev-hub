package com.cardev.hub.dto;

import lombok.Data;

/**
 * 更新用户信息请求DTO
 */
@Data
public class UpdateUserRequest {
    
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String department;
    private String position;
    private String bio;
}
