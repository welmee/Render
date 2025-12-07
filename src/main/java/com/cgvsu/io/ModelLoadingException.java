package com.cgvsu.io;

public class ModelLoadingException extends Exception {

    public ModelLoadingException(String message) {
        super(message);
    }

    public ModelLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelLoadingException(String message, int lineNumber) {
        super(String.format("Ошибка в строке %d: %s", lineNumber, message));
    }

    public ModelLoadingException(String message, int lineNumber, Throwable cause) {
        super(String.format("Ошибка в строке %d: %s", lineNumber, message), cause);
    }
}
