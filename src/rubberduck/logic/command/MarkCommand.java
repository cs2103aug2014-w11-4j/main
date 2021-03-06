package rubberduck.logic.command;

import java.io.IOException;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.datatransfer.Task;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.common.formatter.Formatter;

//@author A0119504L
/**
 * Concrete Command Class that can be executed to mark as completed/incomplete
 * the task object from database given the task id displayed on screen to the
 * user.
 */
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
     * Public constructor of MarkCommand that accepts task id to determine what
     * the command should mark when executed.
     *
     * @param taskId displayed id of the task
     */
    public MarkCommand(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Getter method of taskId.
     *
     * @return taskId as int
     */
    protected int getTaskId() {
        return taskId;
    }

    /**
     * Mark a task (completed to uncompleted and vice versa).
     *
     * @return message of mark
     * @throws IOException that might occur
     */
    @Override
    public Response execute() throws IOException {
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
    private Response markTaskCompleted(int displayedId) throws IOException {
        long databaseId = getDisplayedTasksList().get(displayedId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        assert !oldTask.getIsDone() : "Should be incomplete task.";
        oldTask.setIsDone(true);

        String desc = Formatter.limitDescription(oldTask.getDescription());

        long newTaskId = getDbManager().modify(databaseId, oldTask, String
            .format(JOURNAL_MESSAGE_MARK_AS_COMPLETED, desc));
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
    private Response markTaskIncomplete(int displayedId) throws IOException {
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
