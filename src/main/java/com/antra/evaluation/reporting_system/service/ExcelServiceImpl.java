package com.antra.evaluation.reporting_system.service;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.repo.ExcelRepository;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.util.RequestDataConverter;
import com.antra.evaluation.reporting_system.util.RequestFileConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExcelServiceImpl implements ExcelService {

    private final AtomicInteger id = new AtomicInteger(0);

    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    ExcelGenerationService excelGenerationService;

    @Override
    public InputStream getExcelBodyById(String id) {

        Optional<ExcelFile> fileInfo = excelRepository.getFileById(id);
       // if (fileInfo.isPresent()) {
            File file = new File("temp.xlsx");
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
      //  }
        return null;
    }

    @Override
    public ExcelFile saveRequest(ExcelRequest excelRequest) throws IOException {
        int curId = id.incrementAndGet();
        ExcelData excelData = RequestDataConverter.convertRequestToData(excelRequest, curId);
        ExcelFile excelFile = RequestFileConverter.convertRequestToFile(excelRequest, curId);
        excelRepository.saveFile(excelFile);
        excelGenerationService.generateExcelReport(excelData);
        return excelFile;
    }
}
