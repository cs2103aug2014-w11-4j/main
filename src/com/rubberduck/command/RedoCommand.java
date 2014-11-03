package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

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
     * Redo previous action that was undone in the journal by the user. Will
     * return error message if there is nothing to redo.
     *
     * @throws IOException occurs when DatabaseManager has I/O issues
     */
    @Override
    public Response execute() throws IOException {
        try {
            return new Response(String.format(
                ColorFormatter.format(JOURNAL_MESSAGE_REDONE, Color.YELLOW),
                getDbManager().redo()), false);
        } catch (UnsupportedOperationException e) { /* Nothing to redo */
            return new Response(e.getMessage(), false);
        }
    }
}
