package com.smartcampus.exception;

/**
 * Thrown when a new Sensor references a roomId that does not exist in the system.
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String missingId;

    public LinkedResourceNotFoundException(String missingId) {
        super("Referenced room '" + missingId + "' does not exist in the system.");
        this.missingId = missingId;
    }

    public String getMissingId() {
        return missingId;
    }
}
