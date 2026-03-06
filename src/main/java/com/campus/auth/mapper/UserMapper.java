package com.campus.auth.mapper;

import com.campus.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    // TODO: 根据用户名查询用户
    User findByUsername(String username);

    // TODO: 根据邮箱查询用户
    User findByEmail(String email);

    // TODO: 写入新用户
    void insert(User user);
}
