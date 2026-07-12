package com.oprica.tmsapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";
    private static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
    private static final String INVALID_TRANSACTION_DATA = "INVALID_TRANSACTION_DATA";
    private static final String TRANSACTION_STORAGE_FAILURE = "TRANSACTION_STORAGE_FAILURE";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ProblemDetail problem = createProblem(
                status,
                "Validation failed",
                "One or more request fields are invalid.",
                VALIDATION_FAILED
        );

        problem.setProperty("errors", extractFieldErrors(exception));

        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException exception,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        ProblemDetail problem = createProblem(
                status,
                "Invalid request body",
                "The request body contains malformed JSON "
                        + "or an invalid value.",
                MALFORMED_REQUEST
        );

        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @ExceptionHandler(InvalidTransactionCsvException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTransactionCsv(InvalidTransactionCsvException exception) {
        log.error("The configured transaction CSV contains invalid data", exception);

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Invalid transaction data",
                "The stored transaction data is invalid.",
                INVALID_TRANSACTION_DATA
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    @ExceptionHandler(TransactionRepositoryException.class)
    public ResponseEntity<ProblemDetail> handleRepositoryFailure(TransactionRepositoryException exception) {
        log.error("Transaction storage operation failed", exception);

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Transaction storage failure",
                "The transaction storage operation "
                        + "could not be completed.",
                TRANSACTION_STORAGE_FAILURE
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
        log.error("Unexpected application error", exception);

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred.",
                INTERNAL_ERROR
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem);
    }

    private static ProblemDetail createProblem(HttpStatusCode status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setProperty("code", code);

        return problem;
    }

    private static Map<String, List<String>> extractFieldErrors(MethodArgumentNotValidException exception) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            String message = Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Invalid value");
            errors.computeIfAbsent(fieldError.getField(), ignored -> new ArrayList<>()).add(message);
        }

        return errors;
    }
}
