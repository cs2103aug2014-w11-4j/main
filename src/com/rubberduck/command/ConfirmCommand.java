package com.rubberduck.command;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Concrete Command Class that can be executed to confirm the a tentative task
 * given a task id displayed on the screen to the user.
 */
//@author A0119504L
public class ConfirmCommand extends Command {

    private static final String JOURNAL_MESSAGE_CONFIRM =
        "Confirm task \"%s\"";
    private static final String MESSAGE_CONFIRM =
        "\"%s\" has been confirmed.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "You have input an invalid task ID.";
    private static final String MESSAGE_ERROR_NOT_TENTATIVE =
        "\"%s\" is not tentative and does not need confirmation.";
    private static final String MESSAGE_ERROR_WRONG_DATE_ID =
        "You have input an invalid date ID.";

    private int taskId;
    private int dateId;

    /**
     * Getter method for taskId.
     *
     * @return taskId as int
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Getter method for dateId.
     *
     * @return dateId as int
     */
    public int getDateId() {
        return dateId;
    }

    /**
     * Public constructor for ConfirmCommand.
     *
     * @param taskId id of the task as displayed in the last view command
     * @param dateId id to be confirmed
     */
    public ConfirmCommand(int taskId, int dateId) {
        this.taskId = taskId;
        this.dateId = dateId;
    }

    /**
     * Confirm the date of task to the database.
     *
     * @return confirm message with the displayed id
     */
    //@author A0119504L
    @Override
    public String execute() throws IOException {
        StringBuilder response = new StringBuilder();

        if (!isValidDisplayedId(taskId)) {
            response.append(ColorFormatter.
                format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED));
            response.append(System.lineSeparator());
            response.append(getPreviousDisplayCommand().execute());
            return response.toString();
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);

        Task task = getDbManager().getInstance(databaseId);
        String oldTaskFormattedString = ColorFormatter.format(
            String.format(task.formatOutput("-")), Color.RED);
        String oldDescription = task.getDescription();

        ArrayList<DatePair> dateList = task.getDateList();

        if (dateList.size() <= 1) {
            response.append(ColorFormatter.format(
                String.format(MESSAGE_ERROR_NOT_TENTATIVE, oldDescription),
                Color.RED));
            response.append(System.lineSeparator());
            response.append(getPreviousDisplayCommand().execute());
            return response.toString();
        }

        if (dateList.size() < dateId) {
            response.append(ColorFormatter.
                format(MESSAGE_ERROR_WRONG_DATE_ID, Color.RED));
            response.append(System.lineSeparator());
            response.append(getPreviousDisplayCommand().execute());
            return response.toString();
        }

        DatePair date = dateList.get(dateId - 1);
        ArrayList<DatePair> newDateList = new ArrayList<DatePair>();
        newDateList.add(date);
        task.setDateList(newDateList);

        long newDatabaseId = getDbManager().
            modify(databaseId, task, String.format(JOURNAL_MESSAGE_CONFIRM,
                                                   oldDescription));

        getDisplayedTasksList().set(taskId - 1, newDatabaseId);

        response.append(ColorFormatter.format(
            String.format(MESSAGE_CONFIRM, oldDescription), Color.YELLOW));
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
