package com.campus.event.mapper;

import com.campus.event.entity.EventOutbox;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventOutboxMapper {

    void insert(EventOutbox eventOutbox);

    List<EventOutbox> selectPending(@Param("limit") int limit, @Param("maxRetry") int maxRetry);

    int markSent(@Param("id") Long id);

    int markFailed(@Param("id") Long id);
}
