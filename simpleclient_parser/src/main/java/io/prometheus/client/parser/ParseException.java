package io.prometheus.client.parser;

public class ParseException extends RuntimeException {
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
