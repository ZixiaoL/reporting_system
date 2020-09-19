package com.antra.evaluation.reporting_system.pojo.api;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class BatchExcelRequest {

    @NotNull(message = "requests connot be empty")
    @NotEmpty(message = "requests connot be empty")
    @Valid
    private List<ExcelRequest> requests = new ArrayList();

    public List<ExcelRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<ExcelRequest> requests) {
        this.requests = requests;
    }
}
