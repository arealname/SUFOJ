package com.heima.model.blog.pojos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Blog帖子对象
 * @author Echo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OjBlogList {
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    private String blogContext;
    private String uid;
    private String title;
    private String type;
    private int likeCount;
    private int starCount;
    private String tag;
    private String adminTags;
    private String faceImage;
    private String time;
    private int commentCount;
}
