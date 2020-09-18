package com.antra.evaluation.reporting_system.pojo.api;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

public class MultiSheetExcelRequest extends ExcelRequest{
    @NotBlank(message = "splitBy cannot be blank")
    private String splitBy;
    @AssertTrue(message = "splitBy should be one of the headers")
    public boolean isSplitByInHeaders() {
        for(String s : getHeaders()) {
            if(s.equals(splitBy))return true;
        }
        return false;
    }

    public String getSplitBy() {
        return splitBy;
    }

    public void setSplitBy(String splitBy) {
        this.splitBy = splitBy;
    }
}
