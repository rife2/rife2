package rife.bld.operations.exceptions;

import java.io.Serial;

public class CommandCreationException extends RuntimeException {
    @Serial private static final long serialVersionUID = 5577728010329494164L;

    public CommandCreationException(String message) {
        super(message);
    }
}