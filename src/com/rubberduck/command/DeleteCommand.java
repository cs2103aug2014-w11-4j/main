package com.rubberduck.command;

import java.io.IOException;

import com.rubberduck.Task;

public class DeleteCommand extends Command {
    private static final String JOURNAL_MESSAGE_DELETE = "Deleted task \"%s\"";
    private static final String MESSAGE_DELETE = "\"%s\" has been successfully deleted.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";

    private int taskId;

    public int getTaskId() {
        return taskId;
    }

    /**
     * @param taskId displayed id of the task
     */
    public DeleteCommand(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Delete Task of Database.
     *
     * @return delete message including the task description
     * @throws IOException
     */
    @Override
    public String execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        long databaseId = getDisplayedTasksList().get(taskId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        getDbManager().markAsInvalid(databaseId);
        getDisplayedTasksList().set(taskId - 1, (long) -1);
        getDbManager().recordAction(databaseId, null,
                String.format(JOURNAL_MESSAGE_DELETE, oldDescription));
        return String.format(MESSAGE_DELETE, oldDescription);
    }
}
