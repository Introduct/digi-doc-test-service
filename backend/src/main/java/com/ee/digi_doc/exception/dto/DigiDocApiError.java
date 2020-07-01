package com.ee.digi_doc.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DigiDocApiError {

    private List<Error> errors = new ArrayList<>();

    public void addError(String error, String message, String field) {
        errors.add(new Error(error, message, field));
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private class Error {
        @NonNull
        private final String error;
        private final String message;
        private final String field;
    }

}
