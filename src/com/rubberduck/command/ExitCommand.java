package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to close the DatabaseManager and
 * exit the application.
 */
//@author A0111736M
public class ExitCommand extends Command {

    private static final String MESSAGE_EXIT =
        "Closing Rubberduck.";

    /**
     * Close the DatabaseManager and related I/O files and exit the
     * application.
     */
    @Override
    public Response execute() throws IOException {
        getDbManager().closeFile();
        System.exit(0);
        return new Response(ColorFormatter.format(MESSAGE_EXIT, Color.RED),
                            true);
    }
}
