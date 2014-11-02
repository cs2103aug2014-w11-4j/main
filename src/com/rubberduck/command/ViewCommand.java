package com.rubberduck.command;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Concrete Command Class that can be executed to return related tasks as a
 * formatted String based on various input upon creation.
 *
*/
// @author A0111794E

public class ViewCommand extends Command {

    public enum ViewType {
        TASK, DEADLINE, SCHEDULE
    }

    /* Global logger to log information and exception. */
    private static final Logger LOGGER = Logger
            .getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String MESSAGE_VIEWALL_RESULT = "You have %s uncompleted task(s).";
    private static final String MESSAGE_VIEWDATE_RESULT = "You have %s uncompleted task(s) %s.";
    private static final String MESSAGE_VIEWALL_CRESULT = "You have %s completed task(s).";
    private static final String MESSAGE_VIEWDATE_CRESULT = "You have %s completed task(s) %s.";
    private static final String SCHEDULE_SEPERATOR = "--------------------------------[  SCHEDULES  ]---------------------------------";
    private static final String FLOATING_SEPERATOR = "--------------------------------[    TASKS    ]---------------------------------";
    private static final String DEADLINE_SEPERATOR = "--------------------------------[  DEADLINES  ]---------------------------------";

    private static final ArrayList<ViewType> VIEW_SELECTION_ALL = new ArrayList<ViewType>(
            Arrays.asList(ViewType.DEADLINE, ViewType.SCHEDULE, ViewType.TASK));

    private static final int FLOATING_TASK = 0;
    private static final int DEADLINE_TASK = 1;
    private static final int TIMED_TASK = 2;

    private static final int CONSOLE_MAX_WIDTH = 80;

    private DatePair viewRange;
    private boolean viewAll;
    private boolean completed;
    private ArrayList<ViewType> viewSelection;

    /**
     * Getter method for viewRange.
     *
     * @return viewRange as DatePair
     */
    public DatePair getViewRange() {
        return viewRange;
    }

    /**
     * Getter method for viewAll.
     *
     * @return viewAll as boolean
     */
    public boolean isViewAll() {
        return viewAll;
    }

    /**
     * Getter method for completed.
     *
     * @return completed as boolean
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Public constructor for ViewCommand.
     *
     * @param viewAll       true if all tasks should be returned
     * @param completed     true if all completed tasks should be returned
     *                      instead
     * @param viewRange     date range to view tasks in
     * @param viewSelection specified view scope from user
     */
    public ViewCommand(boolean viewAll, boolean completed, DatePair viewRange,
            ArrayList<ViewType> viewSelection) {
        this.viewAll = viewAll;
        this.viewRange = viewRange;
        this.completed = completed;
        if (viewSelection.isEmpty()) {
            this.viewSelection = VIEW_SELECTION_ALL;
        } else {
            this.viewSelection = viewSelection;
        }

    }

    /**
     * Check the type of view method requested by user.
     *
     * @return the result of the view option
     */
    @Override
    public String execute() throws IOException {
        setPreviousDisplayCommand(this);
        if (isViewAll()) {
            return viewAll(isCompleted(), viewSelection);
        } else {
            return viewByPeriod(getViewRange(), isCompleted(), viewSelection);
        }
    }

    /**
     * Return all the valid task stored in the database.
     *
     * @param isCompleted   true if completed tasks should be displayed
     * @param viewSelection specified view scope from user
     * @return list of tasks and their information in the database
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private String viewAll(boolean isCompleted,
            ArrayList<ViewType> viewSelection) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        getDisplayedTasksList().clear();

        for (int i = 0; i < getDbManager().getValidIdList().size(); i++) {
            Long databaseId = getDbManager().getValidIdList().get(i);
            Task task = getDbManager().getInstance(databaseId);
            if (isCompleted == task.getIsDone()
                    && viewSelection.contains(getTaskType(task))) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.GREEN
                : Color.YELLOW;

        if (isCompleted) {
            String formattedString = String.format(MESSAGE_VIEWALL_CRESULT,
                    getDisplayedTasksList().size());
            responseBuilder.append(ColorFormatter.format(formattedString,
                    headerColor));
        } else {
            String formattedString = String.format(MESSAGE_VIEWALL_RESULT,
                    getDisplayedTasksList().size());
            responseBuilder.append(ColorFormatter.format(formattedString,
                    headerColor));
        }

        if (!getDisplayedTasksList().isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    /**
     * Searches the Database for a related task that coincides with the
     * dateRange requested.
     *
     * @param dateRange   DatePair object containing the start date and end
     *                    date
     * @param isCompleted true if completed tasks should be displayed
     * @return result of all the tasks that are within the period as queried
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private String viewByPeriod(DatePair dateRange, boolean isCompleted,
            ArrayList<ViewType> viewSelection) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        getDisplayedTasksList().clear();
        for (Long databaseId : getDbManager().getValidIdList()) {
            Task task = getDbManager().getInstance(databaseId);
            if (isCompleted == task.getIsDone() && task.hasDate()
                    && viewSelection.contains(getTaskType(task))) {
                if (task.isWithinPeriod(dateRange)) {
                    getDisplayedTasksList().add(databaseId);
                }

            }
        }

        String range = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.US);
        if (dateRange.hasDateRange()) {
            range = "from "
                    + dateFormat.format(dateRange.getStartDate().getTime())
                    + " to "
                    + dateFormat.format(dateRange.getEndDate().getTime());
        } else if (dateRange.hasEndDate()) {
            range = "on " + dateFormat.format(dateRange.getEndDate().getTime());
        } else {
            assert false : "This should not occur as there must be a date.";
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.GREEN
                : Color.YELLOW;

        if (isCompleted) {
            responseBuilder.append(ColorFormatter.format(String.format(
                    MESSAGE_VIEWDATE_CRESULT, getDisplayedTasksList().size(),
                    range), headerColor));
        } else {
            responseBuilder.append(ColorFormatter.format(String.format(
                    MESSAGE_VIEWDATE_RESULT, getDisplayedTasksList().size(),
                    range), headerColor));
        }

        if (!getDisplayedTasksList().isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    /**
     * Format the list of tasks into a String output and return.
     *
     * @return the formatted string of all tasks involved
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    // @author A0111736M
    // @author A0111794E
    private String formatTaskListOutput() throws IOException {
        Collections.sort(getDisplayedTasksList(), getDbManager()
                .getInstanceIdComparator());

        StringBuilder stringBuilder = new StringBuilder();
        String header = String.format("%-7s%-6s%-43s%-24s", "ID", "Done",
                "Task", "Date");
        String border = "";
        for (int i = 0; i < CONSOLE_MAX_WIDTH; i++) {
            border += "-";
        }

        stringBuilder.append(border + System.lineSeparator() + header
                + System.lineSeparator() + border + System.lineSeparator());

        int prevType = -1;
        for (int i = 0; i < getDisplayedTasksList().size(); i++) {
            int currentType = getTaskType(i);
            if (currentType != prevType) {
                if (currentType == FLOATING_TASK) {
                    stringBuilder.append(FLOATING_SEPERATOR);
                    stringBuilder.append(System.lineSeparator());
                } else if (currentType == DEADLINE_TASK) {
                    stringBuilder.append(DEADLINE_SEPERATOR);
                    stringBuilder.append(System.lineSeparator());
                } else if (currentType == TIMED_TASK) {
                    stringBuilder.append(SCHEDULE_SEPERATOR);
                    stringBuilder.append(System.lineSeparator());
                }
            }
            prevType = currentType;
            stringBuilder.append(formatTaskOutput(i));
            stringBuilder.append(System.lineSeparator());
        }
        stringBuilder.append(border);
        return stringBuilder.toString();
    }

    /**
     * Helper method that formats the output of an individual task.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    // @author A0111736M

    private String formatTaskOutput(int displayingId) throws IOException {
        Task task = getDbManager().getInstance(
                getDisplayedTasksList().get(displayingId));
        return task.formatOutput(displayingId + 1 + "");
    }

    /**
     * Retrieve the task type from the database given the ID displayed.
     *
     * @param displayingId the id of the task
     * @return enum which specifies what type of task it is
     * @throws IOException occurs when dbManager encounters a problem with file
     */

    // @author A0111794E
    private int getTaskType(int displayingId) throws IOException {
        Task t = getDbManager().getInstance(
                getDisplayedTasksList().get(displayingId));
        if (t.isFloatingTask()) {
            return FLOATING_TASK;
        } else if (t.isDeadline()) {
            return DEADLINE_TASK;
        } else {
            return TIMED_TASK;
        }
    }

    /**
     * Retrieve the viewType based on the task provided.
     *
     * @param task object to get type
     * @return ViewType of the object
     */
    // @author A0111794E
    private ViewType getTaskType(Task task) {
        if (task.isFloatingTask()) {
            return ViewType.TASK;
        } else if (task.isDeadline()) {
            return ViewType.DEADLINE;
        } else {
            return ViewType.SCHEDULE;
        }
    }

}
