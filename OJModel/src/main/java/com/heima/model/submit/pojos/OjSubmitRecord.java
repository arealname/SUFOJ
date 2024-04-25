package com.heima.model.submit.pojos;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OjSubmitRecord {
    @JsonSerialize(using = ToStringSerializer.class)
    private long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private long uid;
    @JsonSerialize(using = ToStringSerializer.class)
    private long qid;
    private double time;
    private double memory;
    private String language;
    private String code;
    private String submitTime;
    private String title;
    private String message;
    private String testSample;
    private String userName;
}
