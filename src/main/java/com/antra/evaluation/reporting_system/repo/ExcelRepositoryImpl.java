package com.antra.evaluation.reporting_system.repo;

import com.antra.evaluation.reporting_system.exception.ExcelNotFoundException;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ExcelRepositoryImpl implements ExcelRepository {

    Map<String, ExcelFile> excelData = new ConcurrentHashMap<>();

    @Override
    public ExcelFile getFileById(String id) {
        return excelData.get(id);
    }

    @Override
    public ExcelFile saveFile(ExcelFile file) {
        excelData.put(file.getId(), file);
        return file;
    }

    @Override
    public ExcelFile deleteFile(String id) {
        return excelData.remove(id);
    }

    @Override
    public List<ExcelFile> getFiles() {
        return new ArrayList<ExcelFile>(excelData.values());
    }
}

