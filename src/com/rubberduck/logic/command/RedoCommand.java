package com.rubberduck.logic.command;

import com.rubberduck.common.datatransfer.Response;
import com.rubberduck.common.formatter.ColorFormatter;
import com.rubberduck.common.formatter.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to redo the previous undone
 * operation.
 */
//@author A0111736M
public class RedoCommand extends Command {

    private static final String JOURNAL_MESSAGE_REDONE =
        "Redone action \"%s\".";

    /**
     * Redo previous action that was undone in the journal by the user. Returns
     * a response back with the appropriate response message and updated view
     * data of previously executed view/search.
     *
     * @return Response object with appropriate messages
     * @throws IOException occurs when DatabaseManager has I/O issues
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        try {
            String redoMessage = getDbManager().redo();
            Response res = getPreviousDisplayCommand().execute();
            res.setMessages(String.format(
                ColorFormatter.format(JOURNAL_MESSAGE_REDONE, Color.YELLOW),
                redoMessage));
            return res;
        } catch (UnsupportedOperationException e) { /* Nothing to redo */
            LOGGER.info(e.getMessage());
            return new Response(e.getMessage(), false);
        }
    }
}
