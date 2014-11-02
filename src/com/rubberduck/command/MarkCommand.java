package com.rubberduck.command;

import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;

/**
 * Concrete Command Class that can be executed to mark as completed/incomplete
 * the task object from database given the task id displayed on screen to the
 * user.
 */
//@author A0119504L
public class MarkCommand extends Command {

    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "You have input an invalid ID.";
    private static final String JOURNAL_MESSAGE_MARK_AS_COMPLETED =
        "Mark task \"%s\" as completed";
    private static final String JOURNAL_MESSAGE_MARK_AS_INCOMPLETE =
        "Mark task \"%s\" as incomplete";
    private static final String MESSAGE_MARK_COMPLETED =
        "\"%s\" has been marked to completed.";
    private static final String MESSAGE_MARK_INCOMPLETE =
        "\"%s\" has been marked to incomplete.";

    private int taskId;

    /**
     * Getter method of taskId.
     *
     * @return taskId as int
     */
    //@author A0119504L-reused
    public int getTaskId() {
        return taskId;
    }

    /**
     * Public constructor of MarkCommand that accepts task id to determine what
     * the command should mark when executed.
     *
     * @param taskId displayed id of the task
     */
    public MarkCommand(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Mark a task (completed to uncompleted and vice versa).
     *
     * @return message of mark
     */
    // @author A0119504L
    @Override
    public String execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            StringBuilder errorResponse = new StringBuilder();
            errorResponse.append(ColorFormatter.
                format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED));
            errorResponse.append(System.lineSeparator());
            errorResponse.append(getPreviousDisplayCommand().execute());
            return errorResponse.toString();
        }

        if (isCompletedTask(taskId)) {
            return markTaskIncomplete(taskId);
        } else {
            return markTaskCompleted(taskId);
        }
    }

    /**
     * Check whether the task is completed.
     *
     * @param displayedId of the task
     * @return true if the task is completed
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0119504L
    private boolean isCompletedTask(int displayedId) throws IOException {
        long databaseId = getDisplayedTasksList().get(displayedId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        return oldTask.getIsDone();
    }

    /**
     * Mark a task as completed.
     *
     * @param displayedId displayed id of the task
     * @return message of mark task to completed
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0119504L
    public String markTaskCompleted(int displayedId) throws IOException {
        long databaseId = getDisplayedTasksList().get(displayedId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        assert !oldTask.getIsDone();
        oldTask.setIsDone(true);
        long newTaskId =
            getDbManager().modify(databaseId, oldTask, String.format(
                JOURNAL_MESSAGE_MARK_AS_COMPLETED,
                oldTask.getDescription()));
        getDisplayedTasksList().set(displayedId - 1, newTaskId);
        StringBuilder response = new StringBuilder();
        response.append(ColorFormatter.format(
            String.format(MESSAGE_MARK_COMPLETED, oldTask.getDescription()),
            Color.GREEN));
        response.append(System.lineSeparator());
        response.append(getPreviousDisplayCommand().execute());
        return response.toString();

    }

    /**
     * Mark a completed task as incomplete.
     *
     * @param displayedId displayed id of the task
     * @return message of mark task to uncompleted
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0119504L
    public String markTaskIncomplete(int displayedId) throws IOException {
        long databaseId = getDisplayedTasksList().get(displayedId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        assert oldTask.getIsDone();
        oldTask.setIsDone(false);
        long newTaskId =
            getDbManager().modify(databaseId, oldTask, String.format(
                JOURNAL_MESSAGE_MARK_AS_INCOMPLETE,
                oldTask.getDescription()));
        getDisplayedTasksList().set(displayedId - 1, newTaskId);
        StringBuilder response = new StringBuilder();
        response.append(ColorFormatter.format(
            String.format(MESSAGE_MARK_INCOMPLETE, oldTask.getDescription()),
            Color.RED));
        response.append(System.lineSeparator());
        response.append(getPreviousDisplayCommand().execute());
        return response.toString();
    }
}
