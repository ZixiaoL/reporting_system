package com.antra.evaluation.reporting_system.pojo.api;

import com.antra.evaluation.reporting_system.validation.NotNullCheck;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BatchExcelRequest {

    @NotNull(message = "requests cannot be empty", groups = {NotNullCheck.class})
    @NotEmpty(message = "requests cannot be empty", groups = {NotNullCheck.class})
    @Valid
    private List<ExcelRequest> requests;

    public List<ExcelRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<ExcelRequest> requests) {
        this.requests = requests;
    }
}
