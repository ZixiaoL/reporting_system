package com.antra.evaluation.reporting_system.util;

import com.antra.evaluation.reporting_system.exception.ExcelFormatException;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataHeader;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataSheet;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataType;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RequestDataConverter {

    private static final String[] PARSE_PATTERNS = {"yyyy-MM-dd",
            "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd",
            "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyyMMdd"};


    public static ExcelData convertRequestToData(ExcelRequest excelRequest, int id, LocalDateTime time) {
        ExcelData excelData = new ExcelData();
        excelData.setTitle(Integer.toString(id));
        excelData.setGeneratedTime(time);

        List<ExcelDataSheet> sheets = new ArrayList<>();
        List<String> originHeaders = excelRequest.getHeaders();
        List<List<String>> originData = excelRequest.getData();
        ExcelDataSheet excelDataSheet = convertListToSheet(originHeaders, originData, null);
        sheets.add(excelDataSheet);
        excelData.setSheets(sheets);

        return excelData;
    }

    public static ExcelData convertMultiSheetRequestToData(ExcelRequest request, int id, LocalDateTime time) {
        ExcelData excelData = new ExcelData();
        excelData.setTitle(Integer.toString(id));
        excelData.setGeneratedTime(time);

        List<ExcelDataSheet> sheets = new ArrayList<>();
        List<String> originHeaders = request.getHeaders();
        int splitColumnIndex = 0;
        //may not found splitBy here
        while(splitColumnIndex < originHeaders.size() && !originHeaders.get(splitColumnIndex).equals(request.getSplitBy())) {
            splitColumnIndex++;
        }
        List<List<String>> originData = request.getData();
        int finalSplitColumnIndex = splitColumnIndex;
        Map<String, List<List<String>>> originSplittedData = originData.stream()
                .collect(Collectors.groupingBy(x->x.get(finalSplitColumnIndex)));

        for(Map.Entry<String, List<List<String>>> entry : originSplittedData.entrySet()) {
            ExcelDataSheet excelDataSheet = convertListToSheet(originHeaders, entry.getValue(), entry.getKey());
            sheets.add(excelDataSheet);
        }

        excelData.setSheets(sheets);

        return excelData;
    }

    private static ExcelDataSheet convertListToSheet(List<String> originHeaders, List<List<String>> originData, String title) {
        ExcelDataSheet excelDataSheet = new ExcelDataSheet();
        List<ExcelDataHeader> headers = new ArrayList<>();
        List<List<Object>> data = new ArrayList<>();
        List<String> sample = originData.get(0);
        for(int i = 0; i < originHeaders.size(); i++) {
            ExcelDataHeader excelDataHeader = new ExcelDataHeader();
            int finalI = i;
            if(originData.stream().map(x->x.get(finalI)).allMatch(x-> {
                try {
                    NumberUtils.createDouble(x);
                } catch(NumberFormatException nfe) {
                    return false;
                }
                return true;
            })) {
                excelDataHeader.setType(ExcelDataType.NUMBER);
            } else if(originData.stream().map(x->x.get(finalI)).allMatch(x-> {
                try {
                    DateUtils.parseDate(x, PARSE_PATTERNS);
                } catch(ParseException pe) {
                    return false;
                }
                return true;
            })) {
                excelDataHeader.setType(ExcelDataType.DATE);
            } else {
                excelDataHeader.setType(ExcelDataType.STRING);
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
                        try{
                            row.add(NumberUtils.createDouble(originData.get(i).get(j)));
                        } catch (NumberFormatException e) {
                            throw new ExcelFormatException("data in one column should be of the same type");
                        }
                        break;
                    case DATE:
                        try {
                            row.add(DateUtils.parseDate(originData.get(i).get(j), PARSE_PATTERNS));
                        } catch (ParseException e) {
                            throw new ExcelFormatException("data in one column should be of the same type");
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
        excelDataSheet.setTitle(title == null ? "sheet1" : title);
        return excelDataSheet;
    }
}
