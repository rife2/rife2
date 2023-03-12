package rife.bld.operations.exceptions;

import java.io.Serial;

public class OperationOptionException extends RuntimeException {
    @Serial private static final long serialVersionUID = 5577728010329494164L;

    public OperationOptionException(String message) {
        super(message);
    }
}