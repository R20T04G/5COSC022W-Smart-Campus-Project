package com.ramirucompany.cosc022w.smart.campus.project.errors;

public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
