package com.rubberduck.logic.command;

import com.rubberduck.logic.formatter.ColorFormatter;
import com.rubberduck.logic.formatter.ColorFormatter.Color;
import com.rubberduck.logic.formatter.Formatter;
import com.rubberduck.storage.task.Task;

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
        "Marked task \"%s\" as completed";
    private static final String JOURNAL_MESSAGE_MARK_AS_INCOMPLETE =
        "Marked task \"%s\" as incomplete";
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
     * @throws IOException that might occur
     */
    // @author A0119504L
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        if (!isValidDisplayedId(taskId)) {
            String errorMessage = ColorFormatter.format(
                MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
            return new Response(errorMessage, false);
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
     * Mark a incomplete task as completed.
     *
     * @param displayedId displayed id of the task
     * @return Response object containing results of the operation
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0119504L
    public Response markTaskCompleted(int displayedId) throws IOException {
        long databaseId = getDisplayedTasksList().get(displayedId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        assert !oldTask.getIsDone() : "Should be incomplete task.";
        oldTask.setIsDone(true);

        String desc = Formatter.limitDescription(oldTask.getDescription());

        long newTaskId = getDbManager().modify(databaseId, oldTask, String.
            format(JOURNAL_MESSAGE_MARK_AS_COMPLETED, desc));
        getDisplayedTasksList().set(displayedId - 1, newTaskId);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter.format(
            String.format(MESSAGE_MARK_COMPLETED, desc), Color.GREEN));
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }

    /**
     * Mark a completed task as incomplete.
     *
     * @param displayedId displayed id of the task
     * @return Response object containing results of the operation
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0119504L
    public Response markTaskIncomplete(int displayedId) throws IOException {
        long databaseId = getDisplayedTasksList().get(displayedId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        assert oldTask.getIsDone();
        oldTask.setIsDone(false);

        String desc = Formatter.limitDescription(oldTask.getDescription());

        long newTaskId =
            getDbManager().modify(databaseId, oldTask, String.format(
                JOURNAL_MESSAGE_MARK_AS_INCOMPLETE, desc));
        getDisplayedTasksList().set(displayedId - 1, newTaskId);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter.format(
            String.format(MESSAGE_MARK_INCOMPLETE, desc), Color.RED));
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
}
