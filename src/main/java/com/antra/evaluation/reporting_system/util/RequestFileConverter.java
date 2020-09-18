package com.antra.evaluation.reporting_system.util;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;

import java.io.File;
import java.time.LocalDateTime;

public class RequestFileConverter {
    public static ExcelFile convertRequestToFile(ExcelRequest excelRequest, int id, LocalDateTime time) {
        ExcelFile excelFile = new ExcelFile();
        excelFile.setId(String.valueOf(id));
        excelFile.setDescription(excelRequest.getDescription());
        excelFile.setGeneratedTime(time);
        excelFile.setDownloadLink("localhost:8080/excel/"+id+"/content");
        excelFile.setFileSize(new File(id+".xlsx").length()+"B");
        return excelFile;
    }
}
