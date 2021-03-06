package com.antra.evaluation.reporting_system.service;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ExcelService {
    InputStream getExcelBodyById(String id);

    ExcelFile saveRequest(ExcelRequest excelRequest) throws IOException;

    ExcelFile deleteRequest(String id);

    List<ExcelFile> getAllFiles();

    ExcelFile saveMultiSheetRequest(ExcelRequest request) throws IOException;
}
