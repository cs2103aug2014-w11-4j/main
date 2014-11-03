package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to undo the previous operation.
 */
//@author A0111736M
public class UndoCommand extends Command {

    private static final String JOURNAL_MESSAGE_UNDONE =
        "Undone previous action \"%s\".";

    /**
     * Undo the operation done by the user. Will return error message when there
     * is nothing to undo.
     *
     * @throws IOException occurs when DatabaseManager has I/O issues
     */
    @Override
    public Response execute() throws IOException {
        try {
            return new Response(String.format(
                ColorFormatter.format(JOURNAL_MESSAGE_UNDONE, Color.YELLOW),
                getDbManager().undo()), false);
        } catch (UnsupportedOperationException e) { /* Nothing to undo */
            return new Response(e.getMessage(), false);
        }

    }
}