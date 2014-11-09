package rubberduck.logic.command;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

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
     *
     * @throws IOException from logger
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        String response = ColorFormatter.format(MESSAGE_CLEAR, Color.YELLOW);
        LOGGER.info(response);
        return new Response(response, true);
    }
}
