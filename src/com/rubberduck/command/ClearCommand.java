package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to clear the screen of the
 * terminal.
 */
//@author A0111736M
public class ClearCommand extends Command {

    private static final String MESSAGE_CLEAR =
        "Screen cleared.";

    /**
     * Clear the screen of the current terminal by returning an appropriate
     * response back to the caller.
     */
    @Override
    public Response execute() throws IOException {
        String response = ColorFormatter.format(MESSAGE_CLEAR, Color.YELLOW);
        return new Response(response, true);
    }
}
