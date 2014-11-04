package com.rubberduck.command;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Formatter;
import com.rubberduck.menu.Response;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Concrete Command Class that can be executed to update the task object from
 * database given the task id displayed on screen to the user.
 */
//@author A0119504L
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

    private int taskId;
    private String description;
    private ArrayList<DatePair> datePairs;

    /**
     * Getter method for taskId.
     *
     * @return taskId as int
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Getter method for description.
     *
     * @return description as String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method for datePairs.
     *
     * @return datePairs as ArrayList<DatePair>
     */
    public ArrayList<DatePair> getDatePairs() {
        return datePairs;
    }

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
     * Update the task with provided arguments to the database.
     *
     * @return Response containing updated message with the displayed id
     */
    // @author A0119504L
    @Override
    public Response execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            String errorMessage =
                ColorFormatter.format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
            return new Response(errorMessage, false);
        }

        if (DatePair.isDateBeforeNow(datePairs)) {
            String errorMessage =
                ColorFormatter.format(MESSAGE_UPDATE_PAST, Color.RED);
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
            String errorMessage =
                ColorFormatter.format(MESSAGE_ERROR_WRONG_TASK_TYPE, Color.RED);
            return new Response(errorMessage, false);
        }

        long newDatabaseId = getDbManager().
            modify(databaseId, task, String.format(JOURNAL_MESSAGE_UPDATE,
                                                   oldDesc));

        getDisplayedTasksList().set(taskId - 1, newDatabaseId);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter.format(
            String.format(MESSAGE_UPDATE, oldDesc), Color.YELLOW));
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
}
