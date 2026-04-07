package com.example.mysqldemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mysqldemo.entity.User;
import org.apache.ibatis.annotations.Mapper;

// MyBatis-Plus mapper, reuses the same User entity
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
