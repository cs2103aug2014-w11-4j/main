package com.rubberduck.command;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to redo the previous undone operation.
 *
 * @author hooitong
 */
public class RedoCommand extends Command {

    private static final String JOURNAL_MESSAGE_REDONE = "Redone action \"%s\".";

    /**
     * Redo previous action that was undone in the journal by the user. Will return error message if
     * there is nothing to redo.
     *
     * @throws IOException occurs when DatabaseManager has I/O issues
     */
    @Override
    public String execute() throws IOException {
        try {
            return String.format(
                ColorFormatter.format(JOURNAL_MESSAGE_REDONE, Color.YELLOW),
                getDbManager().redo());
        } catch (UnsupportedOperationException e) { /* Nothing to redo */
            return e.getMessage();
        }
    }
}
