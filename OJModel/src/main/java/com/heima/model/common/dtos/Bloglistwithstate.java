package com.heima.model.common.dtos;

import com.heima.model.blog.pojos.OjBlogList;
import com.heima.model.user.pojos.OjUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bloglistwithstate extends OjBlogList {

    private int likeState;//点赞状态
    private int starState;//收藏状态
    private int subscribeState;//关注
    private OjUser user;



}
