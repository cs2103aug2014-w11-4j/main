package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Concrete Command Class that can be executed to clear the screen of the
 * terminal.
 */
//@author A0111736M
public class ClearCommand extends Command {

    /* Global logger to log information and exception. */
    private static final Logger LOGGER =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String MESSAGE_CLEAR =
        "Screen cleared.";

    /**
     * Clear the screen of the current terminal by returning an appropriate
     * response back to the caller.
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        String response = ColorFormatter.format(MESSAGE_CLEAR, Color.YELLOW);
        LOGGER.info(response);
        return new Response(response, true);
    }
}
