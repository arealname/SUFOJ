package com.heima.model.common.dtos;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class Rquerydto {
   @JsonSerialize(using = ToStringSerializer.class)
   public Long uid;
   @JsonSerialize(using = ToStringSerializer.class)
   public Long qid;
}
