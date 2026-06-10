package com.campus.modules.notification.mapper;

import com.campus.modules.notification.dto.NotificationResponse;
import com.campus.modules.notification.entity.Notification;
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
