package com.jobsearch.exception;

public class JobAlertNotFoundException extends RuntimeException {
    public JobAlertNotFoundException(String message) {
        super(message);
    }
}