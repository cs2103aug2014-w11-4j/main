package rubberduck.logic.command;

import java.io.IOException;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

/**
 * Concrete Command Class that can be executed to close the DatabaseManager and
 * exit the application.
 */
//@author A0111736M
public class ExitCommand extends Command {

    private static final String MESSAGE_EXIT =
        "Closing RubberDuck...";
    private static final String MESSAGE_CLOSING_DB =
        "Closing database...";

    /**
     * Close the DatabaseManager and related I/O files and terminate the
     * application and its JVM with a code indicating normal termination.
     *
     * @throws IOException occurs when DatabaseManager encounters closing error
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_CLOSING_DB);
        getDbManager().closeFile();
        LOGGER.info(MESSAGE_EXIT);
        System.exit(0);
        return new Response(ColorFormatter.format(MESSAGE_EXIT, Color.RED),
                            true);
    }
}
