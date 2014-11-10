package rubberduck.logic.command;

import java.io.IOException;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

//@author A0111736M
/**
 * Concrete Command Class that can be executed to redo the previous undone
 * operation.
 */
public class RedoCommand extends Command {

    private static final String JOURNAL_MESSAGE_REDONE =
        "Redone action \"%s\".";

    /**
     * Redo previous action that was undone in the journal by the user. Returns
     * a response back with the appropriate response message and updated view
     * data of previously executed view/search.
     *
     * @return Response object with appropriate messages
     * @throws IOException occurs when DatabaseManager encounters I/O issues
     */
    @Override
    public Response execute() throws IOException {
        try {
            String redoMessage = getDbManager().redo();
            Response res = getPreviousDisplayCommand().execute();
            res.setMessages(String.format(
                ColorFormatter.format(JOURNAL_MESSAGE_REDONE, Color.YELLOW),
                redoMessage));
            LOGGER.info(JOURNAL_MESSAGE_REDONE);
            return res;
        } catch (UnsupportedOperationException e) { /* Nothing to redo */
            LOGGER.info(e.getMessage());
            return new Response(e.getMessage(), false);
        }
    }
}
