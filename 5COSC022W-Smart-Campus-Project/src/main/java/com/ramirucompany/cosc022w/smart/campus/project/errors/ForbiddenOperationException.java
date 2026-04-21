package com.ramirucompany.cosc022w.smart.campus.project.errors;

public class ForbiddenOperationException extends RuntimeException {

    public ForbiddenOperationException(String message) {
        super(message);
    }
}