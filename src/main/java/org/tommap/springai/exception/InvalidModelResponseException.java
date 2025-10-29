package org.tommap.springai.exception;

public class InvalidModelResponseException extends RuntimeException {
    public InvalidModelResponseException(String prompt, String response) {
        super(String.format("the response: %s is not correct for the question: %s -> evaluator check failed", response, prompt));
    }
}
