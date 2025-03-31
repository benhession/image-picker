package com.benhession.imagepicker.testutil.exception;

public class TestDatabaseException extends RuntimeException {

    public TestDatabaseException(String message) {
        super(message);
    }

    public TestDatabaseException(Throwable cause) {
        super(cause);
    }
}
