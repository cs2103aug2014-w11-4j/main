package com.rubberduck.command;

import java.io.IOException;

public class InvalidCommand extends Command {

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param description of the invalid command
     */
    public InvalidCommand(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String execute() throws IOException {
        return errorMessage;
    }

}
