package rife.bld.commands.exceptions;

import java.io.Serial;

public class CommandCreationException extends RuntimeException {
    @Serial private static final long serialVersionUID = 5577728010329494164L;

    private final String name_;

    public CommandCreationException(String name, String message) {
        super(message);

        name_ = name;
    }

    public String getName() {
        return name_;
    }
}