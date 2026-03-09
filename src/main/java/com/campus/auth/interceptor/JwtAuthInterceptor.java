package com.campus.auth.interceptor;

import com.campus.common.context.UserContext;
import com.campus.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.info("Authorization header missing or invalid");
            return false;
        }

        String token = authHeader.substring(7);

        try{
            Long userId = jwtUtil.parseUserId(token);
            if(userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                log.info("user id not found in token");
                return false;
            }
            UserContext.setUserId(userId);
            return true;
        }catch(Exception e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.info("invalid token: " + e.getMessage());
            return false;
        }


    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
