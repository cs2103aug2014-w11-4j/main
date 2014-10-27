package com.rubberduck.command;

import java.io.IOException;
import java.util.ArrayList;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;

public class UpdateCommand extends Command {
    private static final String JOURNAL_MESSAGE_UPDATE = "Updated task \"%s\"";
    private static final String MESSAGE_UPDATE = "\"%s\" has been successfully updated.";
    private static final String MESSAGE_UPDATE_PAST = "You cannot update the end date thats already passed.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";
    private static final String MESSAGE_ERROR_WRONG_TASK_TYPE = "You have input an invalid task type.";

    private int taskId;
    private String description;
    private ArrayList<DatePair> datePairs;

    public int getTaskId() {
        return taskId;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<DatePair> getDatePairs() {
        return datePairs;
    }

    /**
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
     * Update the task to the database.
     *
     * @return updated message with the displayed id
     */
    @Override
    public String execute() throws IOException {
        if (!isValidDisplayedId(taskId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }

        if (DatePair.isDateBeforeNow(datePairs)) {
            return MESSAGE_UPDATE_PAST;
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);

        Task task = getDbManager().getInstance(databaseId);
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
            return MESSAGE_ERROR_WRONG_TASK_TYPE;
        }

        long newDatabaseId = getDbManager().modify(databaseId, task,
                String.format(JOURNAL_MESSAGE_UPDATE, oldDescription));

        getDisplayedTasksList().set(taskId - 1, newDatabaseId);

        return String.format(MESSAGE_UPDATE, oldDescription);
    }
}
