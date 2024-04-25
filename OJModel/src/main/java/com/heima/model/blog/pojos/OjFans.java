package com.heima.model.blog.pojos;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OjFans {
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    private String fid;
    private String uid;

}
