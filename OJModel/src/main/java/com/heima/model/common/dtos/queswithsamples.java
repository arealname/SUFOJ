package com.heima.model.common.dtos;

import com.heima.model.ques.pojos.OjQuestion;
import com.heima.model.ques.pojos.OjTestSamples;
import lombok.Data;

import java.util.List;


@Data
public class queswithsamples extends OjQuestion {
    private List<OjTestSamples> testSamples;
    private boolean ispass;
}
