package com.olek.banking.shared.web.error;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the uniform error response returned by the HTTP API.
 *
 * @param code      stable error code
 * @param message   human-readable error description
 * @param context   safe contextual information related to the error
 * @param path      HTTP request path
 * @param timestamp UTC timestamp when the error was produced
 */
public record ApiErrorResponse(
        String code,
        String message,
        Map<String, Object> context,
        String path,
        Instant timestamp
) {

    /**
     * Creates and validates an API error response.
     *
     * @throws NullPointerException if any required value is {@code null}
     */
    public ApiErrorResponse {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");

        context = Map.copyOf(context);
    }
}