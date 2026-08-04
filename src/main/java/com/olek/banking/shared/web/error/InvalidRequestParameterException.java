package com.olek.banking.shared.web.error;

import java.util.Objects;

/**
 * Indicates that an HTTP request parameter has an invalid representation.
 */
public final class InvalidRequestParameterException extends RuntimeException {

    private final String parameter;
    private final String value;

    /**
     * Creates the exception.
     *
     * @param parameter invalid parameter name
     * @param value     invalid parameter value
     */
    public InvalidRequestParameterException(
            String parameter,
            String value
    ) {
        super("request parameter has an invalid format");

        this.parameter = Objects.requireNonNull(
                parameter,
                "parameter must not be null"
        );

        this.value = Objects.requireNonNull(
                value,
                "value must not be null"
        );
    }

    /**
     * Returns the invalid parameter name.
     *
     * @return parameter name
     */
    public String parameter() {
        return parameter;
    }

    /**
     * Returns the rejected parameter value.
     *
     * @return rejected value
     */
    public String value() {
        return value;
    }
}