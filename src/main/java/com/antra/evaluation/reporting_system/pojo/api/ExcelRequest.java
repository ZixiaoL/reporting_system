package com.antra.evaluation.reporting_system.pojo.api;

import java.util.List;

public class ExcelRequest {
    private List<String> headers;
    private String description;

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
