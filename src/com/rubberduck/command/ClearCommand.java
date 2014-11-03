package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.MenuInterface;
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
     * Clear the screen of the current terminal by calling the clearScreen
     * method in the current ConsoleReader instance. Return response message
     * when cleared.
     */
    @Override
    public Response execute() throws IOException {
        MenuInterface.getInstance().clearScreen();
        return new Response(ColorFormatter.format(MESSAGE_CLEAR, Color.YELLOW),
                            false);
    }
}
