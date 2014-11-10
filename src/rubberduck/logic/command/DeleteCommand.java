package rubberduck.logic.command;

import java.io.IOException;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.datatransfer.Task;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.common.formatter.Formatter;

//@author A0119504L
/**
 * Concrete Command Class that can be executed to delete the task object from
 * database given the task id displayed on screen to the user.
 */
public class DeleteCommand extends Command {

    private static final String MESSAGE_DELETE =
        "\"%s\" has been successfully deleted from RubberDuck.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "This is not a valid task ID to delete.";
    private static final String JOURNAL_MESSAGE_DELETE =
        "Deleted task \"%s\"";

    private int taskId;

    /**
     * Public Constructor of DeleteCommand that accepts a task ID.
     *
     * @param taskId task id of the task displayed on screen
     */
    public DeleteCommand(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Getter method for taskId.
     *
     * @return taskId as int
     */
    protected int getTaskId() {
        return taskId;
    }

    /**
     * Delete given task from database if it exist.
     *
     * @return Response with success message and updated table if delete is
     * successful. else Response with error message.
     * @throws IOException occurs when DBManager has encountered an I/O Error
     */
    @Override
    public Response execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            String errorMessage = ColorFormatter.
                format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
            return new Response(errorMessage, false);
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);
        Task oldTask = getDbManager().getInstance(databaseId);
        String oldDesc = Formatter.limitDescription(oldTask.getDescription());
        getDbManager().modify(databaseId, null,
                              String.format(JOURNAL_MESSAGE_DELETE,
                                            oldDesc));
        getDisplayedTasksList().set(taskId - 1, (long) -1);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter.format(
            String.format(MESSAGE_DELETE, oldDesc), Color.YELLOW));
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
}
