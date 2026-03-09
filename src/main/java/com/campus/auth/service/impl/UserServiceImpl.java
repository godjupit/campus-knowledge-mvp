package com.campus.auth.service.impl;

import com.campus.auth.dto.UserInfoResponse;
import com.campus.auth.entity.User;
import com.campus.auth.mapper.UserMapper;
import com.campus.auth.service.UserService;
import com.campus.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    @Override
    public UserInfoResponse getCurrentUser() {

        User user = new User();
        Long id = UserContext.getUserId();
        if(id == null){
            return null;
        }
        user = userMapper.findById(id);
        if(user == null) {
            return null;
        }

        return new UserInfoResponse(id, user.getUsername(), user.getEmail());
    }
}
