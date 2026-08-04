package com.olek.banking.shared.web.error;

/**
 * Identifies errors produced by the HTTP boundary.
 */
public enum ApiErrorCode {

    /**
     * The HTTP request contains invalid fields.
     */
    VALIDATION_ERROR,

    /**
     * The HTTP request body is missing or cannot be parsed.
     */
    MALFORMED_REQUEST,

    /**
     * A path or query parameter has an invalid format.
     */
    INVALID_PARAMETER,

    /**
     * A required HTTP header is missing.
     */
    MISSING_REQUIRED_HEADER,

    /**
     * The server encountered an unexpected error.
     */
    INTERNAL_SERVER_ERROR
}