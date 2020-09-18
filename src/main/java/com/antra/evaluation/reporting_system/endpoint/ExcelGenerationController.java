package com.antra.evaluation.reporting_system.endpoint;

import com.antra.evaluation.reporting_system.exception.*;
import com.antra.evaluation.reporting_system.pojo.api.ErrorResponse;
import com.antra.evaluation.reporting_system.pojo.api.ExcelRequest;
import com.antra.evaluation.reporting_system.pojo.api.ExcelResponse;
import com.antra.evaluation.reporting_system.pojo.api.MultiSheetExcelRequest;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.service.ExcelService;
import com.antra.evaluation.reporting_system.validation.GroupSequences;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ExcelGenerationController {

    private static final Logger log = LoggerFactory.getLogger(ExcelGenerationController.class);

    ExcelService excelService;
    @Autowired
    public ExcelGenerationController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/excel")
    @ApiOperation("Generate Excel")
    public ResponseEntity<ExcelResponse> createExcel(@RequestBody @Validated({GroupSequences.class}) ExcelRequest request) {
        log.info("Create Single Sheet Excel, Description {}", request.getDescription());
        ExcelFile excelFile = new ExcelFile();
        try {
            excelFile = excelService.saveRequest(request);
        } catch (IOException e) {
            throw new ExcelUploadException("file save failed");
        }
        ExcelResponse response = new ExcelResponse();
        response.setFileId(excelFile.getId());
        response.setDescription(excelFile.getDescription());
        response.setGeneratedTime(excelFile.getGeneratedTime());
        response.setFileSize(excelFile.getFileSize());
        response.setDownloadLink(excelFile.getDownloadLink());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excel/auto")
    @ApiOperation("Generate Multi-Sheet Excel Using Split field")
    public ResponseEntity<ExcelResponse> createMultiSheetExcel(@RequestBody @Validated({GroupSequences.class}) MultiSheetExcelRequest request) {
        log.info("Create Multi Sheet Excel, Description {}", request.getDescription());
        ExcelFile excelFile = null;
        try {
            excelFile = excelService.saveMultiSheetRequest(request);
        } catch (IOException e) {
            throw new ExcelUploadException("file save failed");
        }
        ExcelResponse response = new ExcelResponse();
        response.setFileId(excelFile.getId());
        response.setDescription(excelFile.getDescription());
        response.setGeneratedTime(excelFile.getGeneratedTime());
        response.setFileSize(excelFile.getFileSize());
        response.setDownloadLink(excelFile.getDownloadLink());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/excel")
    @ApiOperation("List all existing files")
    public ResponseEntity<List<ExcelResponse>> listExcels() {
        log.info("List All Excels");
        List<ExcelFile> excelResponses = excelService.getAllFiles();

        var response = new ArrayList<ExcelResponse>();
        response = excelResponses.stream().map(x -> {
                ExcelResponse curResponse = new ExcelResponse();
                curResponse.setFileId(x.getId());
                curResponse.setDescription(x.getDescription());
                curResponse.setGeneratedTime(x.getGeneratedTime());
                curResponse.setFileSize(x.getFileSize());
                curResponse.setDownloadLink(x.getDownloadLink());return curResponse;
        }).collect(Collectors.toCollection(ArrayList::new));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/excel/{id}/content")
    public void downloadExcel(@PathVariable String id, HttpServletResponse response) {
        log.info("Download Excel, id {}", id);
        InputStream fis = excelService.getExcelBodyById(id);
        if(fis == null) {
            throw new ExcelNotFoundException("file not exists");
        }
        response.setHeader("Content-Type","application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ id + ".xlsx\"");
        try {
            FileCopyUtils.copy(fis, response.getOutputStream());
        } catch (IOException e) {
            throw new ExcelNotFoundException("file not exists");
        }
    }

    @DeleteMapping("/excel/{id}")
    public ResponseEntity<ExcelResponse> deleteExcel(@PathVariable String id) {
        log.info("Delete Excel, id {}", id);
        ExcelFile excelFile = excelService.deleteRequest(id);
        if(excelFile == null) {
            throw new ExcelNotFoundException("file not exists");
        }
        var response = new ExcelResponse();
        response.setFileId(excelFile.getId());
        response.setDescription(excelFile.getDescription());
        response.setGeneratedTime(excelFile.getGeneratedTime());
        response.setFileSize(excelFile.getFileSize());
        response.setDownloadLink(excelFile.getDownloadLink());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage("request not valid");
        log.error("request not valid", ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        error.setMessage(message);
        log.error(message, ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExcelUploadException.class)
    public ResponseEntity<ErrorResponse> excelUploadExceptionHandler(ExcelUploadException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        String message = ex.getErrorMessage();
        error.setMessage(message);
        log.error(message, ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(ExcelFormatException.class)
    public ResponseEntity<ErrorResponse> excelFormatExceptionHandler(ExcelFormatException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        String message = ex.getErrorMessage();
        error.setMessage(message);
        log.error(message, ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ExcelNotFoundException.class)
    public ResponseEntity<ErrorResponse> excelDeleteExceptionHandler(ExcelNotFoundException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        String message = ex.getErrorMessage();
        error.setMessage(message);
        log.error(message, ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
