package com.antra.evaluation.reporting_system.service;

import java.io.InputStream;

public interface ExcelService {
    InputStream getExcelBodyById(String id);
}
