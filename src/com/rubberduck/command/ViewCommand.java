package com.rubberduck.command;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;

public class ViewCommand extends Command {
    private static final String MESSAGE_VIEWALL_RESULT = "You have %s uncompleted task(s).";
    private static final String MESSAGE_VIEWDATE_RESULT = "You have %s uncompleted task(s) %s.";
    private static final String MESSAGE_VIEWALL_CRESULT = "You have %s completed task(s).";
    private static final String MESSAGE_VIEWDATE_CRESULT = "You have %s completed task(s) %s.";
    protected static final int CONSOLE_MAX_WIDTH = 80;

    /* Information required for view */
    private DatePair viewRange;
    private boolean viewAll;
    private boolean completed;

    public DatePair getViewRange() {
        return viewRange;
    }

    public boolean isViewAll() {
        return viewAll;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * @param viewAll
     * @param completed
     * @param viewRange
     */
    public ViewCommand(boolean viewAll, boolean completed, DatePair viewRange) {
        this.viewAll = viewAll;
        this.viewRange = viewRange;
        this.completed = completed;
    }

    /**
     * Check the type of view requested by Command
     *
     * @return the result of the view option
     * @throws IOException
     */
    @Override
    public String execute() throws IOException {
        if (isViewAll()) {
            return viewAll(isCompleted());
        } else {
            return viewByPeriod(getViewRange(), isCompleted());
        }

    }

    /**
     * Return all the valid task stored in the database
     *
     * @return list of tasks and their information in the database
     */
    public String viewAll(boolean isCompleted) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        getDisplayedTasksList().clear();
        for (int i = 0; i < getDbManager().getValidIdList().size(); i++) {
            Long databaseId = getDbManager().getValidIdList().get(i);
            Task task = getDbManager().getInstance(databaseId);
            if (isCompleted == task.getIsDone()) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        if (isCompleted) {
            responseBuilder.append(String.format(MESSAGE_VIEWALL_CRESULT,
                    getDisplayedTasksList().size()));
        } else {
            responseBuilder.append(String.format(MESSAGE_VIEWALL_RESULT,
                    getDisplayedTasksList().size()));
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
     * @param dateRange DatePair object containing the start date and end date
     * @return result of all the tasks that are within the period as queried
     */

    public String viewByPeriod(DatePair dateRange, boolean isCompleted)
            throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        getDisplayedTasksList().clear();
        for (Long databaseId : getDbManager().getValidIdList()) {
            Task task = getDbManager().getInstance(databaseId);
            if (isCompleted == task.getIsDone() && task.hasDate()) {
                if (task.isWithinPeriod(dateRange)) {
                    getDisplayedTasksList().add(databaseId);
                }
            }
        }

        String range = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
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

        if (isCompleted) {
            responseBuilder.append(String.format(MESSAGE_VIEWDATE_CRESULT,
                    getDisplayedTasksList().size(), range));
        } else {
            responseBuilder.append(String.format(MESSAGE_VIEWDATE_RESULT,
                    getDisplayedTasksList().size(), range));
        }

        if (!getDisplayedTasksList().isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    private String formatTaskListOutput() throws IOException {
        Collections.sort(getDisplayedTasksList(),
                getDbManager().getInstanceIdComparator());

        StringBuilder stringBuilder = new StringBuilder();
        String header = String.format("%-7s%-6s%-43s%-24s", "ID", "Done",
                "Task", "Date");
        String border = "";
        for (int i = 0; i < CONSOLE_MAX_WIDTH; i++) {
            border += "-";
        }

        stringBuilder.append(border + System.lineSeparator() + header
                + System.lineSeparator() + border + System.lineSeparator());

        for (int i = 0; i < getDisplayedTasksList().size(); i++) {
            stringBuilder.append(formatTaskOutput(i));
            stringBuilder.append(System.lineSeparator());
        }
        stringBuilder.append(border);

        return stringBuilder.toString();
    }

    /**
     * Helper method that formats the output of tasks.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     * @throws IOException
     */
    protected String formatTaskOutput(int displayingId) throws IOException {
        Task task = getDbManager().getInstance(
                getDisplayedTasksList().get(displayingId));
        return task.formatOutput(displayingId + 1);
    }

}
