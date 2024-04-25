package com.heima.model.common.dtos;

import com.heima.model.submit.pojos.OjSubmitRecord;
import lombok.Data;

@Data
public class RecordDto extends OjSubmitRecord {
    private String quesName;
}
