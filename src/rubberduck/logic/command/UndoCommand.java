package rubberduck.logic.command;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to undo the previous operation.
 */
//@author A0111736M
public class UndoCommand extends Command {

    private static final String JOURNAL_MESSAGE_UNDONE =
        "Undone previous action \"%s\".";

    /**
     * Undo the operation done by the user. Returns a response back with the
     * appropriate response message and updated view data of previously executed
     * view/search.
     *
     * @return Response object with appropriate messages
     * @throws IOException occurs when DatabaseManager has I/O issues
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        try {
            String undoMessage = getDbManager().undo();
            Response res = getPreviousDisplayCommand().execute();
            res.setMessages(String.format(
                ColorFormatter.format(JOURNAL_MESSAGE_UNDONE, Color.YELLOW),
                undoMessage));
            return res;
        } catch (UnsupportedOperationException e) { /* Nothing to undo */
            LOGGER.info(MESSAGE_EXECUTE_INFO);
            return new Response(e.getMessage(), false);
        }

    }
}
