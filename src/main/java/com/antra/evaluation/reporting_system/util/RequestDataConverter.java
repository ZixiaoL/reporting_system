package com.antra.evaluation.reporting_system.util;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataHeader;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataSheet;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataType;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class RequestDataConverter {

    private static final String[] PARSE_PATTERNS = {"yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd",
            "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyyMMdd"};


    public static ExcelData convertRequestToData(ExcelRequest excelRequest, int id) {
        ExcelData excelData = new ExcelData();
        excelData.setTitle(Integer.toString(id));
        excelData.setGeneratedTime(LocalDateTime.now());

        List<ExcelDataSheet> sheets = new ArrayList<>();

        ExcelDataSheet excelDataSheet = new ExcelDataSheet();
        List<ExcelDataHeader> headers = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();
        List<String> originHeaders = excelRequest.getHeaders();
        List<List<String>> originData = excelRequest.getData();
        //ExcelDataHeader excelDataHeader = new ExcelDataHeader();
        List<String> sample = originData.get(0);
        for(int i = 0; i < originHeaders.size(); i++) {
            ExcelDataHeader excelDataHeader = new ExcelDataHeader();
            try {
                NumberUtils.createNumber(sample.get(i));
                excelDataHeader.setType(ExcelDataType.NUMBER);
            } catch(NumberFormatException nfe) {
                try {
                    DateUtils.parseDate(sample.get(i), PARSE_PATTERNS);
                    excelDataHeader.setType(ExcelDataType.DATE);
                } catch(ParseException pe) {
                    excelDataHeader.setType(ExcelDataType.STRING);
                }
            }
            excelDataHeader.setName(originHeaders.get(i));
            //excelDataHeader.setWidth(0);
            headers.add(excelDataHeader);
        }
        excelDataSheet.setHeaders(headers);
        for(int i = 0; i < originData.size(); i++) {
            List<Object> row = new ArrayList<>();
            for(int j = 0; j < sample.size(); j++) {
                switch (headers.get(j).getType()) {
                    case STRING:
                        row.add(originData.get(i).get(j));
                        break;
                    case NUMBER:
                        row.add(NumberUtils.createNumber(originData.get(i).get(j)));
                        break;
                    case DATE:
                        try {
                            row.add(DateUtils.parseDate(sample.get(i), PARSE_PATTERNS));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        row.add(originData.get(i).get(j));
                        break;
                }
            }
            data.add(row);
        }
        excelDataSheet.setDataRows(data);
        excelDataSheet.setTitle("1");
        sheets.add(excelDataSheet);
        excelData.setSheets(sheets);

        return excelData;
    }

}
