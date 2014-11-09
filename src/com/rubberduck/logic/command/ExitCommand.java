package com.rubberduck.logic.command;

import com.rubberduck.common.datatransfer.Response;
import com.rubberduck.common.formatter.ColorFormatter;
import com.rubberduck.common.formatter.ColorFormatter.Color;

import java.io.IOException;

//@author A0111736M

/**
 * Concrete Command Class that can be executed to close the DatabaseManager and
 * exit the application.
 */
public class ExitCommand extends Command {

    private static final String MESSAGE_EXIT =
        "Closing RubberDuck...";
    private static final String MESSAGE_CLOSING_DB =
        "Closing database...";

    /**
     * Close the DatabaseManager and related I/O files and terminate the
     * application and its JVM with a code indicating normal termination.
     *
     * @throws IOException occurs when there is problem closing databaseManager
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        LOGGER.info(MESSAGE_CLOSING_DB);
        getDbManager().closeFile();

        LOGGER.info(MESSAGE_EXIT);
        System.exit(0);

        return new Response(ColorFormatter.format(MESSAGE_EXIT, Color.RED),
                            true);
    }
}
