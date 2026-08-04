package com.olek.banking.shared.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Base exception for business rule violations produced by the domain.
 */
public abstract class DomainException extends RuntimeException {

    private final DomainErrorCode code;
    private final Map<String, Object> context;

    /**
     * Creates a domain exception without additional context.
     *
     * @param code    stable domain error code
     * @param message human-readable error description
     */
    protected DomainException(
            DomainErrorCode code,
            String message
    ) {
        this(code, message, Map.of());
    }

    /**
     * Creates a domain exception with safe diagnostic context.
     *
     * @param code    stable domain error code
     * @param message human-readable error description
     * @param context safe contextual information related to the error
     */
    protected DomainException(
            DomainErrorCode code,
            String message,
            Map<String, Object> context
    ) {
        super(Objects.requireNonNull(
                message,
                "message must not be null"
        ));

        this.code = Objects.requireNonNull(
                code,
                "code must not be null"
        );

        this.context = Map.copyOf(
                Objects.requireNonNull(
                        context,
                        "context must not be null"
                )
        );
    }

    /**
     * Returns the stable domain error code.
     *
     * @return domain error code
     */
    public DomainErrorCode code() {
        return code;
    }

    /**
     * Returns immutable and safe error context.
     *
     * @return immutable error context
     */
    public Map<String, Object> context() {
        return context;
    }
}
