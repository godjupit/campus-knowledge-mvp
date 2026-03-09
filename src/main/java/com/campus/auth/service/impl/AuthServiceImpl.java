package com.campus.auth.service.impl;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.LoginResponse;
import com.campus.auth.dto.RegisterRequest;
import com.campus.auth.entity.User;
import com.campus.auth.mapper.UserMapper;
import com.campus.auth.service.AuthService;
import com.campus.common.exception.BusinessException;
import com.campus.common.exception.ErrorCode;
import com.campus.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        if(account.contains("@")){
            User user = userMapper.findByEmail(account);
            if(user == null){
                throw new BusinessException(ErrorCode.LOGIN_FAILED);
            }else{

                if(passwordEncoder.matches(password, user.getPasswordHash())){
                    LoginResponse loginResponse = new LoginResponse();
                    String token = jwtUtil.generateToken(user.getId(), user.getUsername());
                    loginResponse.setToken(token);
                    loginResponse.setTokenType("Bearer");
                    return loginResponse;
                }else{
                    throw new BusinessException(ErrorCode.LOGIN_FAILED);
                }
            }

        }else {
            User user = userMapper.findByUsername(account);
            if(user == null){
                throw new BusinessException(ErrorCode.LOGIN_FAILED);
            }else{
                String passwordHash = passwordEncoder.encode(password);
                if(passwordEncoder.matches(password, user.getPasswordHash())){
                    LoginResponse loginResponse = new LoginResponse();
                    String token = jwtUtil.generateToken(user.getId(), user.getUsername());
                    loginResponse.setToken(token);
                    loginResponse.setTokenType("Bearer");
                    return loginResponse;
                }else{
                    throw new BusinessException(ErrorCode.LOGIN_FAILED);
                }
            }

        }


    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        User user = new User();


        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        log.info("insert user");
        userMapper.insert(user);
    }

    @Override
    public boolean findByName(String username) {
        return userMapper.findByUsername(username) != null;
    }

    @Override
    public boolean findByEmail(String mail) {
        return userMapper.findByEmail(mail) != null;
    }
}
