package com.rubberduck.command;

import java.io.IOException;

public class RedoCommand extends Command {
    private static final String JOURNAL_MESSAGE_REDONE = "Redone operation \"%s\".";

    /**
     * Method that redo previous (undone) action in the journal.
     */
    @Override
    public String execute() throws IOException {
        try {
            return String.format(JOURNAL_MESSAGE_REDONE, getDbManager().redo());
        } catch (UnsupportedOperationException e) { // Nothing to redo
            return e.getMessage();
        }
    }
}
