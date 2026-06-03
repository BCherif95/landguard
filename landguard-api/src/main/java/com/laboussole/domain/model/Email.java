package com.laboussole.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/** Validated, lowercase-normalised e-mail address. */
public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        Objects.requireNonNull(value, "Email must not be null");
        var normalised = value.trim().toLowerCase();
        if (!PATTERN.matcher(normalised).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        value = normalised;
    }

    public static Email of(String raw) {
        return new Email(raw);
    }
}
