package com.antra.evaluation.reporting_system;

import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.ExcelResponse;
import com.antra.evaluation.reporting_system.pojo.report.*;
import com.antra.evaluation.reporting_system.service.ExcelGenerationService;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.when;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReportingSystemApplicationTests {

    @Value("http://localhost:${local.server.port}")
    private String REST_SERVICE_URI ;

    @Autowired
    ExcelGenerationService reportService;

    ExcelData data = new ExcelData();

    @BeforeEach // We are using JUnit 5, @Before is replaced by @BeforeEach
    public void setUpData() {
        data.setTitle("Test book");
        data.setGeneratedTime(LocalDateTime.now());

        var sheets = new ArrayList<ExcelDataSheet>();
        var sheet1 = new ExcelDataSheet();
        sheet1.setTitle("First Sheet");

        var headersS1 = new ArrayList<ExcelDataHeader>();
        ExcelDataHeader header1 = new ExcelDataHeader();
        header1.setName("NameTest");
        //       header1.setWidth(10000);
        header1.setType(ExcelDataType.STRING);
        headersS1.add(header1);

        ExcelDataHeader header2 = new ExcelDataHeader();
        header2.setName("Age");
        //   header2.setWidth(10000);
        header2.setType(ExcelDataType.NUMBER);
        headersS1.add(header2);

        List<List<Object>> dataRows = new ArrayList<>();
        List<Object> row1 = new ArrayList<>();
        row1.add("Dawei");
        row1.add(12);
        List<Object> row2 = new ArrayList<>();
        row2.add("Dawei2");
        row2.add(23);
        dataRows.add(row1);
        dataRows.add(row2);

        sheet1.setDataRows(dataRows);
        sheet1.setHeaders(headersS1);
        sheets.add(sheet1);
        data.setSheets(sheets);

        var sheet2 = new ExcelDataSheet();
        sheet2.setTitle("second Sheet");
        sheet2.setDataRows(dataRows);
        sheet2.setHeaders(headersS1);
        sheets.add(sheet2);
    }

//    @Test
//    public void testExcelGegeration() {
//        File file = null;
//        try {
//            file = reportService.generateExcelReport(data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        assertTrue(file != null);
//    }

    @Test
    public void testExcelGegeration() {
        ExcelRequest excelRequest = new ExcelRequest();
        excelRequest.setHeaders(Arrays.asList("Student #","Name","Class","Score"));
        excelRequest.setDescription("Student Math Course Report");
        List<List<String>> data = new ArrayList<>();
        data.add(Arrays.asList("s-001", "James", "Class-A", "A+"));
        data.add(Arrays.asList("s-002","Robert","Class-A","A"));
        data.add(Arrays.asList("s-003","Jennifer","Class-A","A"));
        data.add(Arrays.asList("s-004","Linda","Class-B","B"));
        data.add(Arrays.asList("s-005","Elizabeth","Class-B","B+"));
        data.add(Arrays.asList("s-006","Susan","Class-C","A"));
        data.add(Arrays.asList("s-007","Jessica","Class-C","A+"));
        data.add(Arrays.asList("s-008","Sarah","Class-A","B"));
        data.add(Arrays.asList("s-009","Thomas","Class-A","B-"));
        data.add(Arrays.asList("s-010","Joseph","Class-B","A-"));
        data.add(Arrays.asList("s-011","Charles","Class-C","A"));
        data.add(Arrays.asList("s-012","Lisa","Class-D","B"));
        excelRequest.setData(data);
        RestTemplate restTemplate = new RestTemplate();
        ExcelResponse excelResponse = restTemplate.postForObject(REST_SERVICE_URI+"/excel", excelRequest, ExcelResponse.class);
        assertTrue(excelResponse.getFileId().equals("1"));
        assertTrue(new File("1.xlsx").length() != 0);
    }


}
