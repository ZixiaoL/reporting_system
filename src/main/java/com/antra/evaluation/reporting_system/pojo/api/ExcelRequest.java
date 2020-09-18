package com.antra.evaluation.reporting_system.pojo.api;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class ExcelRequest {
    @NotEmpty(message = "headers cannot be empty")
    private List<String> headers;
    private String description;
    @NotEmpty(message = "data cannot be empty")
    private List<List<String>> data;

    @AssertTrue(message = "data and headers should be of the same length")
    public boolean isSameLength() {
        for(List<String> row : data) {
            if(row.size() != headers.size())return false;
        }
        return true;
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
}
