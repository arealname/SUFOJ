package com.heima.model.ques.pojos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OjDayPro {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String dayStr;

    private String qid;
}
