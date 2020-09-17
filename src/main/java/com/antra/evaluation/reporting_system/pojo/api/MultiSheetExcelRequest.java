package com.antra.evaluation.reporting_system.pojo.api;

public class MultiSheetExcelRequest extends ExcelRequest{
    private String splitBy;

    public String getSplitBy() {
        return splitBy;
    }

    public void setSplitBy(String splitBy) {
        this.splitBy = splitBy;
    }
}
