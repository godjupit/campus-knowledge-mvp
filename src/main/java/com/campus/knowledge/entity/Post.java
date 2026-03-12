package com.campus.knowledge.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private long id;
    private long userId;
    private String title;
    private String content;
    private String tags;
    private int viewCount;
    private int likeCount;
    private int favoriteCount;
    private int commentCount;
    private int status;
    private Date createdAt;
    private Date updatedAt;



}
