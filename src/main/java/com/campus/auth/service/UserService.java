package com.campus.auth.service;

import com.campus.auth.dto.UserInfoResponse;

public interface UserService {

    // TODO: 实现从上下文或Token获取当前用户并返回信息

    UserInfoResponse getCurrentUser();
}
