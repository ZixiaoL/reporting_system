package com.antra.evaluation.reporting_system.service;

import com.antra.evaluation.reporting_system.pojo.report.ExcelData;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataHeader;
import com.antra.evaluation.reporting_system.pojo.report.ExcelDataSheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Data Structure
 * data - title, generatedTime
 * - sheets
 *      -sheet1 - title (required)
 *              - headers
 *                   - name
 *                   - width
 *                   - type
 *              - dataRows
 *                   - List of objects/values
 */
@Service
public class ExcelGenerationServiceImpl implements ExcelGenerationService {

    @Override
    public File generateExcelReport(ExcelData data) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        headerStyle.setFont(font);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);


        for (ExcelDataSheet sheetData : data.getSheets()) {
            Sheet sheet = workbook.createSheet(sheetData.getTitle());

            Row header = sheet.createRow(0);
            List<ExcelDataHeader> headersData = sheetData.getHeaders();
            for (int i = 0; i < headersData.size(); i++) {
                ExcelDataHeader headerData = headersData.get(i);
                Cell headerCell = header.createCell(i);
                headerCell.setCellValue(headerData.getName());
                if(headerData.getWidth() > 0) sheet.setColumnWidth(i, headerData.getWidth());
                headerCell.setCellValue(headerData.getName());
                headerCell.setCellStyle(headerStyle);
            }
            var rowData = sheetData.getDataRows();
            for (int i = 0; i < rowData.size(); i++) {
                Row row = sheet.createRow(1 + i);
                var eachRow = rowData.get(i);
                for (int j = 0; j < eachRow.size(); j++) {
                    Cell cell = row.createCell(j);
                    switch (headersData.get(j).getType()) {
                        case STRING:
                            cell.setCellValue(String.valueOf(eachRow.get(j)));
                            break;
                        case NUMBER:
                            cell.setCellValue((Double)eachRow.get(j));
                            break;
                        case DATE:
                            cell.setCellValue((Date)eachRow.get(j));
                            break;
                        default:
                            cell.setCellValue(String.valueOf(eachRow.get(j)));
                            break;
                    }
                    cell.setCellValue(String.valueOf(eachRow.get(j)));
                    cell.setCellStyle(style);
                }
            }
            for (int i = 0; i < headersData.size(); i++) {
                sheet.autoSizeColumn(i);
            }
        }


        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + data.getTitle() + ".xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(fileLocation);
    }

}
