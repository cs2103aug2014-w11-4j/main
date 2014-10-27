package com.rubberduck.command;

import java.io.IOException;
import java.util.ArrayList;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

/**
 * Concrete Command Class that can be executed to update the task object from
 * database given the task id displayed on screen to the user.
 *
 * @author Zhao Hang
 */
public class UpdateCommand extends Command {
    private static final String JOURNAL_MESSAGE_UPDATE = "Updated task \"%s\"";
    private static final String MESSAGE_UPDATE = "\"%s\" has been successfully updated.";
    private static final String MESSAGE_UPDATE_PAST = "You cannot update the end date thats already passed.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";
    private static final String MESSAGE_ERROR_WRONG_TASK_TYPE = "You have input an invalid task type.";

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
     * @param taskId id of the task as displayed in the last view command
     * @param description updated description, if not changed will be null
     * @param datePairs updated date list, if not changed will be null
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
     * @return updated message with the displayed id
     * @throws IOException
     * @author Zhao Hang
     * @author Hooi Tong ANSI & Response
     */
    @Override
    public String execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            return ColorFormatter.format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
        }

        if (DatePair.isDateBeforeNow(datePairs)) {
            return ColorFormatter.format(MESSAGE_UPDATE_PAST, Color.RED);
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);

        Task task = getDbManager().getInstance(databaseId);
        String oldTaskFormattedString = ColorFormatter.format(
                String.format(task.formatOutput("-")), Color.RED);
        String oldDescription = task.getDescription();

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
            return ColorFormatter.format(MESSAGE_ERROR_WRONG_TASK_TYPE,
                    Color.RED);
        }

        long newDatabaseId = getDbManager().modify(databaseId, task,
                String.format(JOURNAL_MESSAGE_UPDATE, oldDescription));

        getDisplayedTasksList().set(taskId - 1, newDatabaseId);

        StringBuilder response = new StringBuilder();
        response.append(ColorFormatter.format(
                String.format(MESSAGE_UPDATE, oldDescription), Color.YELLOW));
        response.append(System.lineSeparator());
        response.append(oldTaskFormattedString);
        response.append(System.lineSeparator());
        response.append(ColorFormatter.format(task.formatOutput("+"),
                Color.GREEN));
        response.append(System.lineSeparator());
        response.append(getPreviousDisplayCommand().execute());

        return response.toString();
    }
}
