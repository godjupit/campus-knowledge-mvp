package com.campus.notification.mapper;

import com.campus.notification.dto.NotificationResponse;
import com.campus.notification.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    void insert(Notification notification);

    List<NotificationResponse> selectByReceiver(@Param("receiverUserId") Long receiverUserId,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit);
}
