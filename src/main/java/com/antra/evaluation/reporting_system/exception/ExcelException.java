package com.antra.evaluation.reporting_system.exception;

public abstract class ExcelException extends RuntimeException{
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public ExcelException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public ExcelException() {
        super();
    }
}
