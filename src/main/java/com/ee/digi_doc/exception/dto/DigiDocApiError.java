package com.ee.digi_doc.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DigiDocApiError {

    private List<Error> errors = new ArrayList<>();

    public DigiDocApiError addError(String message) {
        errors.add(new Error(null, message, null));
        return this;
    }

    public void addError(String error, String message, String field) {
        errors.add(new Error(error, message, field));
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Error {
        private final String error;
        private final String message;
        private final String field;
    }

}
