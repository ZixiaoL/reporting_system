package com.antra.evaluation.reporting_system.pojo.api;

import com.antra.evaluation.reporting_system.validation.LogicCheck;
import com.antra.evaluation.reporting_system.validation.NotNullCheck;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MultiSheetExcelRequest extends ExcelRequest{

    @NotNull(message = "splitBy cannot be blank", groups = {NotNullCheck.class})
    @NotBlank(message = "splitBy cannot be blank", groups = {NotNullCheck.class})
    private String splitBy;

    @AssertTrue(message = "splitBy should be one of the headers", groups = {LogicCheck.class})
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
