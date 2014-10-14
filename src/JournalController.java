//@author A0119416H

import java.io.IOException;
import java.io.Serializable;
import java.util.Stack;

/**
 * This class records all the actions done and controls undo/redo operations.
 */
public class JournalController<T extends Serializable & Comparable<T>> {

    private class IDPair {
        private Long previousId;
        private Long newId;
        private String description;

        public IDPair(Long previousId, Long newId, String description) {
            this.previousId = previousId;
            this.newId = newId;
            this.description = description;
        }

        public Long getPreviousId() {
            return previousId;
        }

        public Long getNewId() {
            return newId;
        }

        public String getDescription() {
            return description;
        }
    }

    private Stack<IDPair> undoStack = new Stack<IDPair>();
    private Stack<IDPair> redoStack = new Stack<IDPair>();

    private DatabaseManager<T> dbManager;

    /**
     * Initialize the JournalController with the given dbManager.
     *
     * @param dbManager an initialized instance of DatabaseManager
     */
    public JournalController(DatabaseManager<T> dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Record an action taken.
     *
     * @param previousId the ID of the old instance, can be null if the action
     *            is add
     * @param newId the ID of the new instance, can be null if the action is
     *            delete
     * @param description the description of the recorded action
     */
    public void recordAction(Long previousId, Long newId, String description) {
        redoStack.clear();
        undoStack.add(new IDPair(previousId, newId, description));
    }

    /**
     * Undo the last action
     *
     * @return the description of the undone action
     * @throws IOException if file IO failed in dbManager
     * @throws UnsupportedOperationException if there is no action to undo
     */
    public String undo() throws IOException, UnsupportedOperationException {
        if (undoStack.size() == 0) {
            throw new UnsupportedOperationException("Nothing to undo.");
        }
        IDPair lastAction = undoStack.pop();
        if (lastAction.getNewId() != null) {
            dbManager.markAsInvalid(lastAction.getNewId());
        }
        if (lastAction.getPreviousId() != null) {
            dbManager.markAsValid(lastAction.getPreviousId());
        }
        redoStack.push(lastAction);
        return lastAction.getDescription();
    }

    /**
     * Redo the last undo action
     *
     * @return the description of the redone action
     * @throws IOException if file IO failed in dbManager
     * @throws UnsupportedOperationException if there is no action to redo
     */
    public String redo() throws IOException, UnsupportedOperationException {
        if (redoStack.size() == 0) {
            throw new UnsupportedOperationException("Nothing to redo.");
        }
        IDPair lastAction = redoStack.pop();
        if (lastAction.getPreviousId() != null) {
            dbManager.markAsInvalid(lastAction.getPreviousId());
        }
        if (lastAction.getNewId() != null) {
            dbManager.markAsValid(lastAction.getNewId());
        }
        undoStack.push(lastAction);
        return lastAction.getDescription();
    }

    public int getUndoStackSize() {
        return undoStack.size();
    }

    public int getRedoStackSize() {
        return redoStack.size();
    }

}
