package com.cardev.hub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardev.hub.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
