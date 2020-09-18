package com.antra.evaluation.reporting_system.service;

import com.antra.evaluation.reporting_system.exception.ExcelDownloadException;
import com.antra.evaluation.reporting_system.exception.ExcelNotFoundException;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.repo.ExcelRepository;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.util.RequestDataConverter;
import com.antra.evaluation.reporting_system.util.RequestFileConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExcelServiceImpl implements ExcelService {

    private final AtomicInteger id = new AtomicInteger(0);

    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    ExcelGenerationService excelGenerationService;

    @Override
    public ExcelFile saveMultiSheetRequest(MultiSheetExcelRequest request) throws IOException {
        int curId = id.incrementAndGet();
        LocalDateTime curTime = LocalDateTime.now();
        ExcelData excelData = RequestDataConverter.convertMultiSheetRequestToData(request, curId, curTime);
        excelGenerationService.generateExcelReport(excelData);
        ExcelFile excelFile = RequestFileConverter.convertRequestToFile(request, curId, curTime);
        excelRepository.saveFile(excelFile);
        return excelFile;
    }

    @Override
    public InputStream getExcelBodyById(String id) {
        if(excelRepository.getFileById(id) == null) {
            throw new ExcelNotFoundException("file not exists");
        }
        File file = new File(id + ".xlsx");
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ExcelDownloadException("file not exists");
        }
    }

    @Override
    public ExcelFile saveRequest(ExcelRequest excelRequest) throws IOException {
        int curId = id.incrementAndGet();
        LocalDateTime curTime = LocalDateTime.now();
        ExcelData excelData = RequestDataConverter.convertRequestToData(excelRequest, curId, curTime);
        excelGenerationService.generateExcelReport(excelData);
        ExcelFile excelFile = RequestFileConverter.convertRequestToFile(excelRequest, curId, curTime);
        excelRepository.saveFile(excelFile);
        return excelFile;
    }

    @Override
    public ExcelFile deleteRequest(String id) {
        return excelRepository.deleteFile(id);
    }

    @Override
    public List<ExcelFile> getAllFiles() {
        return excelRepository.getFiles();
    }
}
