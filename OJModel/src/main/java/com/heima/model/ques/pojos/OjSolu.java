package com.heima.model.ques.pojos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.heima.model.common.dtos.Rquerydto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OjSolu extends Rquerydto {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String text;
    private String subTime;
    private int ups;
    private String title;

    public OjSolu(Long _uid, Long _qid, String _text, String _td, int i,String t) {
        uid=_uid;
        qid=_qid;
        text=_text;
        title=t;
        subTime=_td;
        ups=i;
    }
}
