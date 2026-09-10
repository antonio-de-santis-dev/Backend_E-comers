package com.it.userservis.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException (String message) {
        super(message);
    }
}
