package com.file.sorter.exception;

public class FileNotFoundCustomException extends RuntimeException {
    public FileNotFoundCustomException(String message) {
        super(message);
    }
}