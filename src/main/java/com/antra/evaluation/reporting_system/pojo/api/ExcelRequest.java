package com.antra.evaluation.reporting_system.pojo.api;



import com.antra.evaluation.reporting_system.validation.LogicCheck;
import com.antra.evaluation.reporting_system.validation.MultiSheetLogicCheck;
import com.antra.evaluation.reporting_system.validation.MultiSheetNotNullCheck;
import com.antra.evaluation.reporting_system.validation.NotNullCheck;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


public class ExcelRequest {

    @NotNull(message = "headers cannot be empty", groups = {NotNullCheck.class})
    @NotEmpty(message = "headers cannot be empty", groups = {NotNullCheck.class})
    private List<String> headers;

    private String description;

    @NotNull(message = "data cannot be empty", groups = {NotNullCheck.class})
    @NotEmpty(message = "data cannot be empty", groups = {NotNullCheck.class})
    private List<List<String>> data;

    @NotNull(message = "splitBy cannot be empty", groups = {MultiSheetNotNullCheck.class})
    @NotEmpty(message = "splitBy cannot be empty", groups = {MultiSheetNotNullCheck.class})
    private String splitBy;

    @AssertTrue(message = "data and headers should be of the same length", groups = {LogicCheck.class})
    public boolean isSameLength() {
        for(List<String> row : data) {
            if(row.size() != headers.size())return false;
        }
        return true;
    }

    @AssertTrue(message = "splitBy should be one of the headers", groups = {MultiSheetLogicCheck.class})
    public boolean isSplitByInHeaders() {
        for(String s : getHeaders()) {
            if(s.equals(splitBy))return true;
        }
        return false;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public String getSplitBy() {
        return splitBy;
    }

    public void setSplitBy(String splitBy) {
        this.splitBy = splitBy;
    }
}
