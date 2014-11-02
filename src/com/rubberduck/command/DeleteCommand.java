package com.rubberduck.command;

import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to delete the task object from
 * database given the task id displayed on screen to the user.
 *
 */
// @author A0119504L
public class DeleteCommand extends Command {

    private static final String JOURNAL_MESSAGE_DELETE =
        "Deleted task \"%s\"";
    private static final String MESSAGE_DELETE =
        "\"%s\" has been successfully deleted.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "This is not a valid task ID to delete.";

    private int taskId;

    /**
     * Getter method for taskId.
     *
     * @return taskId as int
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Public Constructor of DeleteCommand that accepts a task ID.
     *
     * @param taskId task id of the task displayed on screen
     */
    public DeleteCommand(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Delete given task from database if it exist.
     *
     * @return success message and previous view list or error if invalid id
     * @throws IOException DBManager has encountered an IO Error
     * 
     */
    // @author A0111736M
    // @author A0119504L
    @Override
    public String execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            return ColorFormatter.
                format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
        }
        long databaseId = getDisplayedTasksList().get(taskId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        getDbManager().modify(databaseId, null,
                              String.format(JOURNAL_MESSAGE_DELETE,
                                            oldDescription));
        getDisplayedTasksList().set(taskId - 1, (long) -1);

        StringBuilder response = new StringBuilder();
        response.append(ColorFormatter.format(
            String.format(MESSAGE_DELETE, oldDescription), Color.YELLOW));
        response.append(System.lineSeparator());
        response.append(ColorFormatter.format(
            String.format(oldTask.formatOutput("-")), Color.RED));
        response.append(System.lineSeparator());
        response.append(getPreviousDisplayCommand().execute());

        return response.toString();
    }
}
