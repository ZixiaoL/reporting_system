package com.antra.evaluation.reporting_system;

import com.antra.evaluation.reporting_system.endpoint.ExcelGenerationController;
import com.antra.evaluation.reporting_system.exception.ExcelFormatException;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.service.ExcelService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class APITest {
    @Mock
    ExcelService excelService;

    @BeforeEach
    public void configMock() {
        MockitoAnnotations.initMocks(this);
        RestAssuredMockMvc.standaloneSetup(new ExcelGenerationController(excelService));
    }

    @Test
    public void testFileDownload() throws FileNotFoundException {
        Mockito.when(excelService.getExcelBodyById(anyString())).thenReturn(new FileInputStream("temp.xlsx"));
        given().accept("application/json").get("/excel/temp/content").peek().
                then().assertThat()
                .statusCode(200);
    }

    @Test
    public void testFileDownloadWithInvalidId() {
        Mockito.when(excelService.getExcelBodyById(anyString())).thenReturn(null);
        given().accept("application/json").get("/excel/123abc/content").peek().
                then().assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("file not exists"));
    }

    @Test
    public void testFileDeletion() {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("temp");
        Mockito.when(excelService.deleteRequest(anyString())).thenReturn(savedExcelFile);
        given().accept("application/json").delete("/excel/temp").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.equalTo("temp"));
    }

    @Test
    public void testFileDeletionWithInvalidId() {
        Mockito.when(excelService.deleteRequest(anyString())).thenReturn(null);
        given().accept("application/json").delete("/excel/123abc").peek().
                then().assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("file not exists"));
    }

    @Test
    public void testListFiles() {
        List<ExcelFile> excelFiles = new ArrayList<>();
        ExcelFile savedExcelFile1 = new ExcelFile();
        savedExcelFile1.setId("1");
        ExcelFile savedExcelFile2 = new ExcelFile();
        savedExcelFile2.setId("2");
        excelFiles.add(savedExcelFile1);
        excelFiles.add(savedExcelFile2);
        Mockito.when(excelService.getAllFiles()).thenReturn(excelFiles);
        given().accept("application/json").get("/excel").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.hasItems("1","2"));
    }


    @Test
    public void testExcelGeneration() throws IOException {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("1");
        Mockito.when(excelService.saveRequest(any())).thenReturn(savedExcelFile);
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}")
                .post("/excel").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.equalTo(savedExcelFile.getId()));
    }


    @Test
    public void testMultiSHeetExcelGeneration() throws IOException {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("1");
        Mockito.when(excelService.saveMultiSheetRequest(any())).thenReturn(savedExcelFile);
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]], \"splitBy\":\"Age\"}")
                .post("/excel/auto").peek().
                then().assertThat()
                .statusCode(200)
                .body("fileId", Matchers.equalTo(savedExcelFile.getId()));
    }

    @Test
    public void testMultiSHeetExcelGenerationWithNoSplitBy() throws IOException {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("1");
        Mockito.when(excelService.saveMultiSheetRequest(any())).thenReturn(savedExcelFile);
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}")
                .post("/excel/auto").peek().
                then().assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("splitBy cannot be blank"));
    }

    @Test
    public void testMultiSHeetExcelGenerationWithSplitByNotInHeaders() throws IOException {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("1");
        Mockito.when(excelService.saveMultiSheetRequest(any())).thenReturn(savedExcelFile);
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]], \"splitBy\":\"Gender\"}")
                .post("/excel/auto").peek().
                then().assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("splitBy should be one of the headers"));
    }

    @Test
    public void testExcelGenerationWithEmptyHeader() throws IOException {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("1");
        Mockito.when(excelService.saveRequest(any())).thenReturn(savedExcelFile);
        given().accept("application/json").contentType(ContentType.JSON).body("{\"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}").post("/excel").peek().
                then().assertThat()
                .statusCode(400)
                .body("errorCode", Matchers.equalTo(400))
                .body("message", Matchers.equalTo("headers cannot be empty"));
    }

    @Test
    public void testExcelGenerationWithWrongUrl() throws IOException {
        ExcelFile savedExcelFile = new ExcelFile();
        savedExcelFile.setId("1");
        Mockito.when(excelService.saveRequest(any())).thenReturn(savedExcelFile);
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}")
                .post("/excel/1").peek().
                then().assertThat()
                .statusCode(405);
    }

    @Test
    public void testExcelGenerationButIOExceptionRaised() throws IOException {
        Mockito.when(excelService.saveRequest(any())).thenThrow(new IOException());
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}")
                .post("/excel").peek().
                then().assertThat()
                .statusCode(500);
    }

    @Test
    public void testExcelGenerationButExcelFormatExceptionRaised() throws IOException {
        Mockito.when(excelService.saveRequest(any())).thenThrow(new ExcelFormatException("data in one column should be of the same type"));
        given().accept("application/json").contentType(ContentType.JSON)
                .body("{\"headers\":[\"Name\",\"Age\"], \"data\":[[\"Teresa\",\"5\"],[\"Daniel\",\"1\"]]}")
                .post("/excel").peek().
                then().assertThat()
                .statusCode(400)
                .body("message", Matchers.equalTo("data in one column should be of the same type"));
    }
}
