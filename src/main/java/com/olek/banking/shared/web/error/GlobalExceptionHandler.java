package com.olek.banking.shared.web.error;

import com.olek.banking.shared.domain.DomainErrorCode;
import com.olek.banking.shared.domain.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Translates application failures into uniform HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final HttpStatusCode UNPROCESSABLE_CONTENT =
            HttpStatusCode.valueOf(422);

    private final Clock clock;

    /**
     * Creates the global exception handler.
     *
     * @param clock source of the current time
     */
    public GlobalExceptionHandler(Clock clock) {
        this.clock = Objects.requireNonNull(
                clock,
                "clock must not be null"
        );
    }

    /**
     * Handles business rule violations produced by the domain.
     *
     * @param exception domain exception
     * @param request   current HTTP request
     * @return mapped HTTP error response
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainException(
            DomainException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode status = statusFor(exception.code());

        ApiErrorResponse response = new ApiErrorResponse(
                exception.code().name(),
                exception.getMessage(),
                exception.context(),
                request.getRequestURI(),
                Instant.now(clock)
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Handles bean validation failures in HTTP request models.
     *
     * @param exception validation exception
     * @param request   current HTTP request
     * @return bad-request error response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, Object> context = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            context.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ApiErrorResponse response = new ApiErrorResponse(
                ApiErrorCode.VALIDATION_ERROR.name(),
                "request validation failed",
                context,
                request.getRequestURI(),
                Instant.now(clock)
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    /**
     * Handles malformed or unreadable JSON request bodies.
     *
     * @param exception body parsing exception
     * @param request   current HTTP request
     * @return bad-request error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                ApiErrorCode.MALFORMED_REQUEST.name(),
                "request body is missing or malformed",
                Map.of(),
                request.getRequestURI(),
                Instant.now(clock)
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    /**
     * Handles unexpected failures without exposing internal implementation
     * details.
     *
     * @param exception unexpected exception
     * @param request   current HTTP request
     * @return internal-server-error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                ApiErrorCode.INTERNAL_SERVER_ERROR.name(),
                "an unexpected error occurred",
                Map.of(),
                request.getRequestURI(),
                Instant.now(clock)
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Handles invalid path or query parameter representations.
     *
     * @param exception invalid parameter exception
     * @param request   current HTTP request
     * @return bad-request error response
     */
    @ExceptionHandler(InvalidRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(
            InvalidRequestParameterException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                ApiErrorCode.INVALID_PARAMETER.name(),
                exception.getMessage(),
                Map.of(
                        "parameter", exception.parameter(),
                        "value", exception.value()
                ),
                request.getRequestURI(),
                Instant.now(clock)
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    private HttpStatusCode statusFor(DomainErrorCode code) {
        return switch (code) {
            case ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case ACCOUNT_NUMBER_ALREADY_EXISTS,
                 ACCOUNT_NOT_ACTIVE,
                 ACCOUNT_ALREADY_CLOSED,
                 ACCOUNT_HAS_REMAINING_BALANCE -> HttpStatus.CONFLICT;

            case INSUFFICIENT_BALANCE,
                 CURRENCY_MISMATCH,
                 INVALID_AMOUNT -> UNPROCESSABLE_CONTENT;
        };
    }
}