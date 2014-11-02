package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to return an invalid message back
 * to the user.
 */
//@author A0111736M
public class InvalidCommand extends Command {

    private String errorMessage;

    /**
     * Public constructor of InvalidCommand that accepts an error message.
     *
     * @param errorMessage that is to be displayed to the user
     */
    public InvalidCommand(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns error message provided by application back to user.
     *
     * @return error message as String
     */
    @Override
    public String execute() throws IOException {
        return ColorFormatter.format(errorMessage, Color.RED);
    }
}
