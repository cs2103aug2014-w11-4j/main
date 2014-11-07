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
 * Concrete Command Class that can be executed to confirm the a tentative task
 * given a task id displayed on the screen to the user.
 */
//@author A0119504L
public class ConfirmCommand extends Command {

    private static final String MESSAGE_CONFIRM =
        "\"%s\" has been confirmed from %s.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID =
        "You have input an invalid task ID.";
    private static final String MESSAGE_ERROR_NOT_TENTATIVE =
        "\"%s\" is not tentative and does not need confirmation.";
    private static final String MESSAGE_ERROR_WRONG_DATE_ID =
        "You have input an invalid date ID.";
    private static final String MESSAGE_SCHEDULE_CONFLICT =
        "Please note that there are conflicting schedule(s). Plan well!";
    private static final String JOURNAL_MESSAGE_CONFIRM =
        "Confirmed task \"%s\"";

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
     * Confirm the date of the tentative task to the database.
     *
     * @return Response object with appropriate feedback to the user
     */
    //@author A0119504L
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        if (!isValidDisplayedId(taskId)) {
            String errorMessage = ColorFormatter.
                format(MESSAGE_ERROR_WRONG_TASK_ID, Color.RED);
            return new Response(errorMessage, false);
        }

        long databaseId = getDisplayedTasksList().get(taskId - 1);

        Task task = getDbManager().getInstance(databaseId);
        String description = Formatter.limitDescription(task.getDescription());
        ArrayList<DatePair> dateList = task.getDateList();

        if (!task.isTentative()) {
            String errorMessage = ColorFormatter.format(
                String.format(MESSAGE_ERROR_NOT_TENTATIVE, description),
                Color.RED);
            return new Response(errorMessage, false);
        }

        if (dateList.size() < dateId) {
            String errorMessage = ColorFormatter.
                format(MESSAGE_ERROR_WRONG_DATE_ID, Color.RED);
            return new Response(errorMessage, false);
        }

        DatePair date = dateList.get(dateId - 1);
        ArrayList<DatePair> newDateList = new ArrayList<DatePair>();
        newDateList.add(date);
        task.setDateList(newDateList);

        long newDatabaseId = getDbManager().
            modify(databaseId, task, String.format(JOURNAL_MESSAGE_CONFIRM,
                                                   description));
        boolean hasConflict =
            task.checkConflictWithDB(getDbManager(), newDatabaseId);
        getDisplayedTasksList().set(taskId - 1, newDatabaseId);

        StringBuilder messages = new StringBuilder();
        messages.append(ColorFormatter.format(
            String.format(MESSAGE_CONFIRM, description,
                          task.getDateString()), Color.YELLOW));
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
