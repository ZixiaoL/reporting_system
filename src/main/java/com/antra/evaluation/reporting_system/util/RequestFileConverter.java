package com.antra.evaluation.reporting_system.util;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;

public class RequestFileConverter {
    public static ExcelFile convertRequestToFile(ExcelRequest excelRequest, int id) {
        ExcelFile excelFile = new ExcelFile();
        excelFile.setId(String.valueOf(id));
        return excelFile;
    }
}
