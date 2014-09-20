//@author A0119416H

import java.io.IOException;
import java.util.Stack;

/**
 * This class records all the actions done and controls undo/redo operations.
 */
public class JournalController {

    private class IDPair {
        private Long previousId;
        private Long newId;
        public IDPair(Long previousId, Long newId) {
            this.previousId = previousId;
            this.newId = newId;
        }
        public Long getPreviousId() {
            return previousId;
        }
        public Long getNewId() {
            return newId;
        }
    }

    private Stack<IDPair> undoStack = new Stack<IDPair>();
    private Stack<IDPair> redoStack = new Stack<IDPair>();

    private DatabaseManager<Task> dbManager;

    /**
     * Initialize the JournalController with the given dbManager.
     *
     * @param dbManager an initialized instance of DatabaseManager
     */
    public JournalController(DatabaseManager<Task> dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Record an action taken.
     *
     * @param previousId the ID of the old instance, can be null if the action is add
     * @param newId the ID of the new instance, can be null if the action is delete
     */
    public void recordAction(Long previousId, Long newId) {
        redoStack.clear();
        undoStack.add(new IDPair(previousId, newId));
    }

    /**
     * Undo the last action
     *
     * @throws IOException if file IO failed in dbManager
     * @throws UnsupportedOperationException if there is no action to undo
     */
    public void undo() throws IOException, UnsupportedOperationException {
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
    }

    /**
     * Redo the last undo action
     *
     * @throws IOException if file IO failed in dbManager
     * @throws UnsupportedOperationException if there is no action to redo
     */
    public void redo() throws IOException, UnsupportedOperationException {
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
    }

}
