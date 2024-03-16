package io.github.sbaeumlisberger.simpleproperties;

public class MappingException extends RuntimeException {
    public MappingException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public MappingException(String msg) {
        super(msg);
    }
}
