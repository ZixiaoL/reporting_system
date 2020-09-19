package com.antra.evaluation.reporting_system;

import com.antra.evaluation.reporting_system.pojo.api.*;
import com.antra.evaluation.reporting_system.validation.MultiSheetGroupSequences;
import com.antra.evaluation.reporting_system.validation.SingleSheetGroupSequences;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingSystemApplicationTests {

    @Value("http://localhost:${local.server.port}")
    private String REST_SERVICE_URI ;

    private static Validator validator;

    private final ExcelRequest singleSheetExcelRequest = new ExcelRequest();

    private final ExcelRequest multiSheetExcelRequest = new ExcelRequest();

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach // We are using JUnit 5, @Before is replaced by @BeforeEach
    public void setUpData() {
        singleSheetExcelRequest.setHeaders(Arrays.asList("Student #","Name","Class","Score"));
        singleSheetExcelRequest.setDescription("Student Math Course Report");
        List<List<String>> SingleSheetExcelData = new ArrayList<>();
        SingleSheetExcelData.add(Arrays.asList("s-001", "James", "Class-A", "A+"));
        SingleSheetExcelData.add(Arrays.asList("s-002","Robert","Class-A","A"));
        SingleSheetExcelData.add(Arrays.asList("s-003","Jennifer","Class-A","A"));
        SingleSheetExcelData.add(Arrays.asList("s-004","Linda","Class-B","B"));
        SingleSheetExcelData.add(Arrays.asList("s-005","Elizabeth","Class-B","B+"));
        SingleSheetExcelData.add(Arrays.asList("s-006","Susan","Class-C","A"));
        SingleSheetExcelData.add(Arrays.asList("s-007","Jessica","Class-C","A+"));
        SingleSheetExcelData.add(Arrays.asList("s-008","Sarah","Class-A","B"));
        SingleSheetExcelData.add(Arrays.asList("s-009","Thomas","Class-A","B-"));
        SingleSheetExcelData.add(Arrays.asList("s-010","Joseph","Class-B","A-"));
        SingleSheetExcelData.add(Arrays.asList("s-011","Charles","Class-C","A"));
        SingleSheetExcelData.add(Arrays.asList("s-012","Lisa","Class-D","B"));
        singleSheetExcelRequest.setData(SingleSheetExcelData);

        multiSheetExcelRequest.setHeaders(Arrays.asList("Student #","Name","Class","Score"));
        multiSheetExcelRequest.setDescription("Student Math Course Report");
        List<List<String>> multiSheetExcelData = new ArrayList<>();
        multiSheetExcelData.add(Arrays.asList("s-001", "James", "Class-A", "A+"));
        multiSheetExcelData.add(Arrays.asList("s-002","Robert","Class-A","A"));
        multiSheetExcelData.add(Arrays.asList("s-003","Jennifer","Class-A","A"));
        multiSheetExcelData.add(Arrays.asList("s-004","Linda","Class-B","B"));
        multiSheetExcelData.add(Arrays.asList("s-005","Elizabeth","Class-B","B+"));
        multiSheetExcelData.add(Arrays.asList("s-006","Susan","Class-C","A"));
        multiSheetExcelData.add(Arrays.asList("s-007","Jessica","Class-C","A+"));
        multiSheetExcelData.add(Arrays.asList("s-008","Sarah","Class-A","B"));
        multiSheetExcelData.add(Arrays.asList("s-009","Thomas","Class-A","B-"));
        multiSheetExcelData.add(Arrays.asList("s-010","Joseph","Class-B","A-"));
        multiSheetExcelData.add(Arrays.asList("s-011","Charles","Class-C","A"));
        multiSheetExcelData.add(Arrays.asList("s-012","Lisa","Class-D","B"));
        multiSheetExcelRequest.setData(multiSheetExcelData);
        multiSheetExcelRequest.setSplitBy("Score");

    }

    @Test
    public void testExcelGeneration() {
        RestTemplate restTemplate = new RestTemplate();
        ExcelResponse excelResponse = restTemplate.postForObject(REST_SERVICE_URI+"/excel", singleSheetExcelRequest, ExcelResponse.class);
        assertTrue(excelResponse.getFileId() != null && excelResponse.getFileId().equals("1"));
        assertTrue(new File("1.xlsx").length() != 0);
    }

    @Test
    public void testMultiSheetExcelGeneration() {
        RestTemplate restTemplate = new RestTemplate();
        ExcelResponse excelResponse = restTemplate.postForObject(REST_SERVICE_URI+"/excel/auto", multiSheetExcelRequest, ExcelResponse.class);
        assertTrue(excelResponse.getFileId() != null && excelResponse.getFileId().equals("1"));
        assertTrue(new File("1.xlsx").length() != 0);
    }

    @Test
    public void testFileDownload() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(REST_SERVICE_URI+"/excel", singleSheetExcelRequest, ExcelResponse.class);
        when().get(REST_SERVICE_URI+"/excel/1/content").peek().then().assertThat().statusCode(200);
    }

    @Test
    public void testFileDownloadWithInvalidId() {
        when().get(REST_SERVICE_URI+"/excel/1/content").peek().then().assertThat().statusCode(400)
                .body("message", Matchers.equalTo("file not exists"));
    }

    @Test
    public void testFileDeletion() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(REST_SERVICE_URI+"/excel", singleSheetExcelRequest, ExcelResponse.class);
        when().delete(REST_SERVICE_URI+"/excel/1").peek().then().assertThat().statusCode(200)
                .body("fileId", Matchers.equalTo("1"));
    }

    @Test
    public void testFileDeletionWithInvalidId() {
        when().delete(REST_SERVICE_URI+"/excel/1").peek().then().assertThat().statusCode(400)
                .body("message", Matchers.equalTo("file not exists"));
    }

    @Test
    public void testListFiles() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(REST_SERVICE_URI+"/excel/auto", multiSheetExcelRequest, ExcelResponse.class);
        restTemplate.postForObject(REST_SERVICE_URI+"/excel/auto", multiSheetExcelRequest, ExcelResponse.class);
        when().get(REST_SERVICE_URI+"/excel").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.hasItems("1","2"));
    }


    @Test
    public void testMultiSheetExcelGenerationWithNoSplitBy() {
        multiSheetExcelRequest.setSplitBy(null);
        Set<ConstraintViolation<ExcelRequest>> violations = validator.validate(multiSheetExcelRequest, MultiSheetGroupSequences.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testMultiSheetExcelGenerationWithSplitByNotInHeaders() {
        multiSheetExcelRequest.setSplitBy("1234abcd");
        Set<ConstraintViolation<ExcelRequest>> violations = validator.validate(multiSheetExcelRequest, MultiSheetGroupSequences.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testExcelGenerationWithEmptyHeader() {
        singleSheetExcelRequest.setHeaders(null);
        Set<ConstraintViolation<ExcelRequest>> violations = validator.validate(singleSheetExcelRequest, SingleSheetGroupSequences.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testBatchExcelGeneration() {
        BatchExcelRequest batchExcelRequest = new BatchExcelRequest();
        List<ExcelRequest> excelRequests = new ArrayList<>();
        excelRequests.add(singleSheetExcelRequest);
        excelRequests.add(multiSheetExcelRequest);
        batchExcelRequest.setRequests(excelRequests);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(REST_SERVICE_URI+"/excel/batch", batchExcelRequest, List.class);
        assertTrue(new File("1.xlsx").length() != 0);
        assertTrue(new File("2.xlsx").length() != 0);
    }


    @Test
    public void testBatchExcelGenerationWithNoHeader() {
        BatchExcelRequest batchExcelRequest = new BatchExcelRequest();
        List<ExcelRequest> excelRequests = new ArrayList<>();
        singleSheetExcelRequest.setHeaders(null);
        excelRequests.add(singleSheetExcelRequest);
        excelRequests.add(multiSheetExcelRequest);
        batchExcelRequest.setRequests(excelRequests);
        Set<ConstraintViolation<ExcelRequest>> violations = validator.validate(singleSheetExcelRequest, SingleSheetGroupSequences.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testBatchFileDownload() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(REST_SERVICE_URI+"/excel", singleSheetExcelRequest, ExcelResponse.class);
        restTemplate.postForObject(REST_SERVICE_URI+"/excel", multiSheetExcelRequest, ExcelResponse.class);
        when().get(REST_SERVICE_URI+"/excel/content/batch?fileId=1&fileId=2").peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    public void testBatchFileDownloadWithInvalidId() {
        when().get(REST_SERVICE_URI+"/excel/content/batch?fileId=1&fileId=2").peek().
                then().assertThat()
                .statusCode(400);
    }

}
