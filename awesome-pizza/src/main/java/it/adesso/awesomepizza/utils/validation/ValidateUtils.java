package it.adesso.awesomepizza.utils.validation;

import it.adesso.awesomepizza.exception.InvalidOrderStateException;

public final class ValidateUtils {

    private ValidateUtils() {
    }

    public static void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new InvalidOrderStateException(message);
        }
    }

    public static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderStateException(message);
        }
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new InvalidOrderStateException(message);
        }
    }

    public static void requireMax(int value, int max, String message) {
        if (value > max) {
            throw new InvalidOrderStateException(message);
        }
    }
}
