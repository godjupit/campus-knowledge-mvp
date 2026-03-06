package com.campus.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
