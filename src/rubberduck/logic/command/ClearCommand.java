package rubberduck.logic.command;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

//@author A0111736M
/**
 * Concrete Command Class that can be executed to clear the screen of the
 * terminal.
 */
public class ClearCommand extends Command {

    private static final String MESSAGE_CLEAR =
        "Screen cleared.";

    /**
     * Clear the screen of the current terminal by returning an appropriate
     * Response object back to the caller.
     */
    @Override
    public Response execute() {
        String response = ColorFormatter.format(MESSAGE_CLEAR, Color.YELLOW);
        LOGGER.info(MESSAGE_CLEAR);
        return new Response(response, true);
    }
}
