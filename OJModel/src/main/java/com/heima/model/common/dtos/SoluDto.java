package com.heima.model.common.dtos;

import com.heima.model.ques.pojos.OjSolu;
import lombok.Data;

@Data
public class SoluDto extends OjSolu {
    private String url;
    private String nickName;
}
