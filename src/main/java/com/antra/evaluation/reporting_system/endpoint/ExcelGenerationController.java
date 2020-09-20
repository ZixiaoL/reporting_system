package com.antra.evaluation.reporting_system.endpoint;

import com.antra.evaluation.reporting_system.exception.*;
import com.antra.evaluation.reporting_system.pojo.api.*;
import com.antra.evaluation.reporting_system.pojo.report.ExcelFile;
import com.antra.evaluation.reporting_system.service.ExcelService;
import com.antra.evaluation.reporting_system.validation.MultiSheetGroupSequences;
import com.antra.evaluation.reporting_system.validation.SingleSheetGroupSequences;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.compress.utils.IOUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    public ResponseEntity<ExcelResponse> createExcel(@RequestBody @Validated({SingleSheetGroupSequences.class}) ExcelRequest request) {
        log.info("Create Single Sheet Excel, Description {}", request.getDescription());
        ExcelFile excelFile;
        try {
            excelFile = excelService.saveRequest(request);
        } catch (IOException e) {
            throw new ExcelTransferException("file save failed");
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
    public ResponseEntity<ExcelResponse> createMultiSheetExcel(@RequestBody @Validated({MultiSheetGroupSequences.class}) ExcelRequest request) {
        log.info("Create Multi Sheet Excel, Description {}", request.getDescription());
        ExcelFile excelFile;
        try {
            excelFile = excelService.saveMultiSheetRequest(request);
        } catch (IOException e) {
            throw new ExcelTransferException("file save failed");
        }
        ExcelResponse response = new ExcelResponse();
        response.setFileId(excelFile.getId());
        response.setDescription(excelFile.getDescription());
        response.setGeneratedTime(excelFile.getGeneratedTime());
        response.setFileSize(excelFile.getFileSize());
        response.setDownloadLink(excelFile.getDownloadLink());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/excel/batch")
    @ApiOperation("Generate multiple Excel files in one request")
    public ResponseEntity<List<Response>> createBatchExcel(@RequestBody @Validated({SingleSheetGroupSequences.class}) BatchExcelRequest batchExcelRequest) {
        boolean ioFail = false;
        boolean formatFail = false;
        List<ExcelRequest> requests = batchExcelRequest.getRequests();
        log.info("Create Batch Excel Files, Description {}", requests.stream().map(ExcelRequest::getDescription)
                .collect(Collectors.toList()));
        List<Response> responses = new ArrayList<>();
        for(ExcelRequest excelRequest : requests) {
            ExcelFile excelFile;
            try {
                if(excelRequest.getSplitBy() != null && excelRequest.getSplitBy().length() != 0) {
                    excelFile = excelService.saveMultiSheetRequest(excelRequest);
                } else {
                    excelFile = excelService.saveRequest(excelRequest);
                }
                ExcelResponse response = new ExcelResponse();
                response.setFileId(excelFile.getId());
                response.setDescription(excelFile.getDescription());
                response.setGeneratedTime(excelFile.getGeneratedTime());
                response.setFileSize(excelFile.getFileSize());
                response.setDownloadLink(excelFile.getDownloadLink());
                responses.add(response);
            } catch (IOException ioe) {
                ExcelTransferException ete = new ExcelTransferException("file save failed");
                responses.add(excelTransferExceptionHandler(ete).getBody());
                log.error(ete.getErrorMessage(), ete);
                ioFail = true;
            } catch (ExcelFormatException efe) {
                responses.add(excelFormatExceptionHandler(efe).getBody());
                log.error(efe.getErrorMessage(), efe);
                formatFail = true;
            }
        }
        return new ResponseEntity<>(responses, ioFail ? HttpStatus.INTERNAL_SERVER_ERROR :
                formatFail ? HttpStatus.BAD_REQUEST : HttpStatus.OK);
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

    @GetMapping("/excel/content/batch")
    @ApiOperation("Download Excel")
    public void downloadBatchExcel(HttpServletResponse response, @RequestParam String... fileId) {
        log.info("Download Excel, id {}", Arrays.stream(fileId).collect(Collectors.toList()));
        response.setHeader("Content-Type","application/zip");
        response.setHeader("Content-Disposition","attachment; filename=\""+Arrays.stream(fileId).collect(Collectors.toList()).toString()+".zip\"");
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for(String id : fileId) {
                InputStream fis = excelService.getExcelBodyById(id);
                if(fis == null) {
                    response.setStatus(400);
                    ExcelNotFoundException enfe = new ExcelNotFoundException("file not exists");
                    log.error(enfe.getErrorMessage(), enfe);
                    continue;
                }
                ZipEntry zipEntry = new ZipEntry(id+".xlsx");
                zos.putNextEntry(zipEntry);
                try {
                    zos.write(IOUtils.toByteArray(fis));
                } catch (IOException e) {
                    response.setStatus(500);
                    ExcelTransferException ete = new ExcelTransferException("file download failed");
                    log.error(ete.getErrorMessage(), ete);
                    continue;
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            response.setStatus(500);
            ExcelTransferException ete = new ExcelTransferException("file download failed");
            log.error(ete.getErrorMessage(), ete);
        }
    }

    @DeleteMapping("/excel/{id}")
    @ApiOperation("Delete Excel")
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

    @ExceptionHandler(ExcelTransferException.class)
    public ResponseEntity<ErrorResponse> excelTransferExceptionHandler(ExcelTransferException ex) {
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
    public ResponseEntity<ErrorResponse> excelNotFoundExceptionHandler(ExcelNotFoundException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        String message = ex.getErrorMessage();
        error.setMessage(message);
        log.error(message, ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
