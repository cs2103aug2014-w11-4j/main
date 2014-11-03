package com.rubberduck.command;

import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to delete the task object from
 * database given the task id displayed on screen to the user.
 */
//@author A0119504L
public class DeleteCommand extends Command {

    private static final String JOURNAL_MESSAGE_DELETE =
        "Deleted task \"%s\"";
    private static final String MESSAGE_DELETE =
        "\"%s\" has been successfully deleted from RubberDuck.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "This is not a valid task ID to delete.";

    private int taskId;

    /**
     * Getter method for taskId.
     *
     * @return taskId as int
     */
    //@author A0119504L-reused
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
     */
    // @author A0119504L
    @Override
    public Response execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            String errorMessage = ColorFormatter.
                format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
            return new Response(errorMessage, false);
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        getDbManager().modify(databaseId, null,
                              String.format(JOURNAL_MESSAGE_DELETE,
                                            oldDescription));
        getDisplayedTasksList().set(taskId - 1, (long) -1);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter.format(
            String.format(MESSAGE_DELETE, oldDescription), Color.YELLOW));
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
}
