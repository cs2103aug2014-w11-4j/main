package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to close the DatabaseManager and
 * exit the application.
 *
 * @author hooitong
 */
public class ExitCommand extends Command {

    private static final String MESSAGE_EXIT =
        "Closing Rubberduck.";

    /**
     * Close the DatabaseManager and related I/O files and exit the
     * application.
     */
    @Override
    public String execute() throws IOException {
        getDbManager().closeFile();
        System.exit(0);
        return ColorFormatter.format(MESSAGE_EXIT, Color.RED);
    }
}
