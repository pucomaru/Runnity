package com.runnity.websocket.exception;

/**
 * 유효하지 않은 티켓 예외
 */
public class InvalidTicketException extends RuntimeException {
    public InvalidTicketException(String message) {
        super(message);
    }

    public InvalidTicketException(String message, Throwable cause) {
        super(message, cause);
    }
}

