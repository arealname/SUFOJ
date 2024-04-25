package com.heima.model.ques.pojos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class OjUserQues {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private long uid;
    private long qid;
}
