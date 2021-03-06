package rubberduck.logic.command;

import java.io.IOException;
import java.util.ArrayList;

import rubberduck.common.datatransfer.DatePair;
import rubberduck.common.datatransfer.Response;
import rubberduck.common.datatransfer.Task;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.common.formatter.Formatter;

//@author A0119504L
/**
 * Concrete Command Class that can be executed to update the task object from
 * database given the task id displayed on screen to the user.
 */
public class UpdateCommand extends Command {

    private static final String JOURNAL_MESSAGE_UPDATE =
        "Updated task \"%s\"";
    private static final String MESSAGE_UPDATE =
        "\"%s\" has been successfully updated.";
    private static final String MESSAGE_UPDATE_PAST =
        "You cannot update the end date that has already passed.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "You have input an invalid ID.";
    private static final String MESSAGE_ERROR_WRONG_TASK_TYPE =
        "You have input an invalid task type.";
    private static final String MESSAGE_SCHEDULE_CONFLICT =
        "Please note that there are conflicting schedule(s). Plan well!";

    private int taskId;
    private String description;
    private ArrayList<DatePair> datePairs;

    /**
     * Public constructor for UpdateCommand.
     *
     * @param taskId      id of the task as displayed in the last view command
     * @param description updated description, if not changed will be null
     * @param datePairs   updated date list, if not changed will be null
     */
    public UpdateCommand(int taskId, String description,
                         ArrayList<DatePair> datePairs) {
        this.taskId = taskId;
        this.description = description;
        this.datePairs = datePairs;
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
     * Getter method for description.
     *
     * @return description as String
     */
    protected String getDescription() {
        return description;
    }

    /**
     * Getter method for datePairs.
     *
     * @return datePairs as ArrayList<DatePair>
     */
    protected ArrayList<DatePair> getDatePairs() {
        return datePairs;
    }

    /**
     * Update the task with provided arguments to the database.
     *
     * @return Response containing updated message with the displayed id
     * @throws IOException that might occur
     */
    @Override
    public Response execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            String errorMessage = ColorFormatter
                .format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
            return new Response(errorMessage, false);
        }

        if (DatePair.isDateBeforeNow(datePairs)) {
            String errorMessage = ColorFormatter
                .format(MESSAGE_UPDATE_PAST, Color.RED);
            return new Response(errorMessage, false);
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);

        Task task = getDbManager().getInstance(databaseId);
        String oldDesc = Formatter.limitDescription(task.getDescription());

        if (!description.isEmpty()) {
            task.setDescription(description);
        }

        if (!datePairs.isEmpty()) {
            if (task.isFloatingTask() || task.isDeadline()) {
                task.setDateList(datePairs);
                if (!task.isFloatingTask() && !task.isDeadline()) {
                    task.resetUuid();
                }
            } else {
                task.setDateList(datePairs);
                if (task.isFloatingTask() || task.isDeadline()) {
                    task.resetUuid();
                }
            }
        }

        if (!task.checkValidity()) {
            String errorMessage = ColorFormatter
                .format(MESSAGE_ERROR_WRONG_TASK_TYPE, Color.RED);
            return new Response(errorMessage, false);
        }

        long newDatabaseId = getDbManager()
            .modify(databaseId, task, String.format(JOURNAL_MESSAGE_UPDATE,
                                                    oldDesc));
        boolean hasConflict = task.checkConflictWithDB(getDbManager(),
                                                       newDatabaseId);

        getDisplayedTasksList().set(taskId - 1, newDatabaseId);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter
                            .format(String.format(MESSAGE_UPDATE, oldDesc),
                                    Color.YELLOW));
        if (hasConflict) {
            messages.append(System.lineSeparator());
            messages.append(ColorFormatter.format(MESSAGE_SCHEDULE_CONFLICT,
                                                  Color.RED));
        }
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
}
