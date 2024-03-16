package io.github.sbaeumlisberger.simpleproperties;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

public class PropertyMappers {

    public static Function<String, Boolean> asBoolean() {
        return (String rawValue) -> {
            if (rawValue.equals("true")) {
                return true;
            } else if (rawValue.equals("false")) {
                return false;
            } else {
                throw new MappingException(rawValue + " is not a boolean.");
            }
        };
    }

    public static Function<String, Integer> asInteger() {
        return (String rawValue) -> {
            try {
                return Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                throw new MappingException(rawValue + " is not a integer.");
            }
        };
    }

    public static Function<String, Long> asLong() {
        return (String rawValue) -> {
            try {
                return Long.parseLong(rawValue);
            } catch (NumberFormatException e) {
                throw new MappingException(rawValue + " is not a long.");
            }
        };
    }


    public static Function<String, Float> asFloat() {
        return (String rawValue) -> {
            try {
                return Float.parseFloat(rawValue);
            } catch (NumberFormatException e) {
                throw new MappingException(rawValue + " is not a float.");
            }
        };
    }

    public static Function<String, Double> asDouble() {
        return (String rawValue) -> {
            try {
                return Double.parseDouble(rawValue);
            } catch (NumberFormatException e) {
                throw new MappingException(rawValue + " is not a double.");
            }
        };
    }

    public static <T extends Enum<T>> Function<String, T> asEnum(Class<T> enumClass) {
        return asEnum(enumClass, false);
    }

    public static <T extends Enum<T>> Function<String, T> asEnum(Class<T> enumClass, boolean ignoreCase) {
        return (String rawValue) -> Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> ignoreCase ? e.name().equalsIgnoreCase(rawValue) : e.name().equals(rawValue))
                .findFirst()
                .orElseThrow(() -> new MappingException(rawValue + " is not a value of the enum " + enumClass.getName() + "."));
    }

    public static Function<String, Path> asPath() {
        return (String rawValue) -> {
            try {
                return Path.of(rawValue);
            } catch (InvalidPathException e) {
                throw new MappingException(rawValue + " is not a valid path.", e);
            }
        };
    }

}
