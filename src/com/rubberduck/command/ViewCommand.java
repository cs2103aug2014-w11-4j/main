package com.rubberduck.command;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Formatter;
import com.rubberduck.menu.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Concrete Command Class that can be executed to return related tasks as a
 * formatted String based on various input upon creation.
 */
//@author A0111794E
public class ViewCommand extends Command {

    public enum ViewType {
        TASK, DEADLINE, SCHEDULE
    }

    /* Global logger to log information and exception. */
    private static final Logger LOGGER =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String MESSAGE_VIEWALL_RESULT =
        "You have %s incomplete task(s) in total.";
    private static final String MESSAGE_VIEWDATE_RESULT =
        "You have %s incomplete task(s) %s.";
    private static final String MESSAGE_VIEWALL_CRESULT =
        "You have %s completed task(s) in total.";
    private static final String MESSAGE_VIEWDATE_CRESULT =
        "You have %s completed task(s) %s.";
    private static final String MESSAGE_VIEWOVERDUE_RESULT =
        "You have %s overdue tasks(s) in total.";
    private static final String MESSAGE_DATE_RANGE =
        "from %s to %s";
    private static final String MESSAGE_ONE_DAY =
        "on %s";
    private static final String SCHEDULE_SEPERATOR =
        "--------------------------------[  SCHEDULES  ]---------------------------------";
    private static final String FLOATING_SEPERATOR =
        "--------------------------------[    TASKS    ]---------------------------------";
    private static final String DEADLINE_SEPERATOR =
        "--------------------------------[  DEADLINES  ]---------------------------------";

    private static final ArrayList<ViewType> VIEW_SELECTION_ALL =
        new ArrayList<ViewType>(
            Arrays.asList(ViewType.DEADLINE, ViewType.SCHEDULE, ViewType.TASK));

    private static final int FLOATING_TASK = 0;
    private static final int DEADLINE_TASK = 1;
    private static final int TIMED_TASK = 2;

    private DatePair viewRange;
    private boolean viewAll;
    private boolean completed;
    private boolean overdue;
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
     * Getter method for overdue
     *
     * @return overdue as boolean
     */
    public boolean isOverdue() {
        return overdue;
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
    public ViewCommand(boolean viewAll, boolean completed,
                       DatePair viewRange, ArrayList<ViewType> viewSelection) {
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
     * Public constructor for ViewCommand. Overloaded for constructing
     * ViewCommand that can be executed to view overdue tasks with filter.
     *
     * @param overdue       true if to show all overdue tasks
     * @param viewSelection specified view scope from user
     */
    public ViewCommand(boolean overdue, ArrayList<ViewType> viewSelection) {
        this.viewAll = false;
        this.completed = false;
        this.viewRange = null;
        this.overdue = overdue;
        if (viewSelection.isEmpty()) {
            this.viewSelection = VIEW_SELECTION_ALL;
        } else {
            this.viewSelection = viewSelection;
        }
    }

    /**
     * Check the type of view method requested by user.
     *
     * @return Response object containing the result of the view option
     */
    @Override
    public Response execute() throws IOException {
        LOGGER.info(MESSAGE_EXECUTE_INFO);

        setPreviousDisplayCommand(this);
        if (isOverdue()) {
            return viewOverdue(viewSelection);
        } else if (isViewAll()) {
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
     * @return Response object containing result of all tasks
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewAll(boolean isCompleted,
                             ArrayList<ViewType> viewSelection)
        throws IOException {

        getDisplayedTasksList().clear();

        for (int i = 0; i < getDbManager().getValidIdList().size(); i++) {
            Long databaseId = getDbManager().getValidIdList().get(i);
            Task task = getDbManager().getInstance(databaseId);
            if (isCompleted == task.getIsDone() && viewSelection.
                contains(getTaskType(task))) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.GREEN
                                                              : Color.YELLOW;

        Color cHeaderColor = getDisplayedTasksList().isEmpty() ? Color.YELLOW
                                                               : Color.GREEN;
        StringBuilder viewCount = new StringBuilder();
        if (isCompleted) {
            String formattedString = String.format(MESSAGE_VIEWALL_CRESULT,
                                                   getDisplayedTasksList().
                                                       size());
            viewCount.append(ColorFormatter.format(formattedString,
                                                   cHeaderColor));
        } else {
            String formattedString = String.format(MESSAGE_VIEWALL_RESULT,
                                                   getDisplayedTasksList()
                                                       .size());
            viewCount.append(ColorFormatter.format(formattedString,
                                                   headerColor));
        }

        return new Response("", viewCount.toString(), formatTaskListOutput());
    }

    /**
     * Return overdue tasks in database, based on user selections
     *
     * @param viewSelection specified view scope from user
     * @return Response object containing result of all tasks
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewOverdue(ArrayList<ViewType> viewSelection)
        throws IOException {

        getDisplayedTasksList().clear();

        for (int i = 0; i < getDbManager().getValidIdList().size(); i++) {
            Long databaseId = getDbManager().getValidIdList().get(i);
            Task task = getDbManager().getInstance(databaseId);
            if (taskOverdueValidity(task)) {
                getDisplayedTasksList().add(databaseId);
            }
        }
        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.GREEN
                                                              : Color.YELLOW;
        StringBuilder viewCount = new StringBuilder();
        String formattedString = String.format(MESSAGE_VIEWOVERDUE_RESULT,
                                               getDisplayedTasksList().size());
        viewCount.append(ColorFormatter.format(formattedString, headerColor));
        return new Response("", viewCount.toString(), formatTaskListOutput());
    }

    /**
     * Checks if the task has a deadline, if it is overdue and if it fits user
     * filter criteria
     *
     * @param task the task to check
     * @return if it fits search criteria
     */
    private boolean taskOverdueValidity(Task task) {
        return (task.isDeadline() || task.isSchedule()) &&
               !task.getIsDone() && viewSelection.contains(getTaskType(task)) &&
               task.getDateList().get(0).getEndDate()
                   .before(Calendar.getInstance());
    }

    /**
     * Searches the Database for a related task that coincides with the
     * dateRange requested.
     *
     * @param dateRange     DatePair object containing the start date and end
     *                      date
     * @param isCompleted   true if completed tasks should be displayed
     * @param viewSelection specified view scope from user
     * @return Response object containing result of all tasks within range
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewByPeriod(DatePair dateRange, boolean isCompleted,
                                  ArrayList<ViewType> viewSelection)
        throws IOException {
        getDisplayedTasksList().clear();
        for (Long databaseId : getDbManager().getValidIdList()) {
            Task task = getDbManager().getInstance(databaseId);
            if (isCompleted == task.getIsDone() && task.hasDate() &&
                viewSelection.contains(getTaskType(task))) {
                if (task.isWithinPeriod(dateRange)) {
                    getDisplayedTasksList().add(databaseId);
                }
            }
        }

        String range = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.US);
        if (dateRange.hasDateRange()) {
            String startDate =
                dateFormat.format(dateRange.getStartDate().getTime());
            String endDate =
                dateFormat.format(dateRange.getEndDate().getTime());

            if (!startDate.equals(endDate)) {
                range = String.format(MESSAGE_DATE_RANGE, startDate, endDate);
            } else {
                range = String.format(MESSAGE_ONE_DAY, endDate);
            }
        } else {
            assert false : "This should not occur as there must be a date.";
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.GREEN
                                                              : Color.YELLOW;
        StringBuilder viewCount = new StringBuilder();
        if (isCompleted) {
            viewCount.append(ColorFormatter.format(String.format(
                MESSAGE_VIEWDATE_CRESULT, getDisplayedTasksList().size(),
                range), headerColor));
        } else {
            viewCount.append(ColorFormatter.format(String.format(
                MESSAGE_VIEWDATE_RESULT, getDisplayedTasksList().size(),
                range), headerColor));
        }

        return new Response("", viewCount.toString(), formatTaskListOutput());
    }

    /**
     * Format the list of tasks into a String output and return.
     *
     * @return the formatted string of all tasks involved
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0111736M
    private String formatTaskListOutput() throws IOException {
        Collections.sort(getDisplayedTasksList(),
                         getDbManager().getInstanceIdComparator());
        StringBuilder taskData = new StringBuilder();

        int prevType = -1;
        for (int i = 0; i < getDisplayedTasksList().size(); i++) {
            if (taskData.length() > 0) {
                taskData.append(System.lineSeparator());
            }

            int currentType = getTaskType(i);
            if (currentType != prevType) {
                if (currentType == FLOATING_TASK) {
                    taskData.append(FLOATING_SEPERATOR);
                } else if (currentType == DEADLINE_TASK) {
                    taskData.append(DEADLINE_SEPERATOR);
                } else if (currentType == TIMED_TASK) {
                    taskData.append(SCHEDULE_SEPERATOR);
                }
                taskData.append(System.lineSeparator());
            }
            prevType = currentType;
            taskData.append(formatTaskOutput(i));
        }
        return taskData.toString();
    }

    /**
     * Helper method that formats the output of an individual task.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private String formatTaskOutput(int displayingId) throws IOException {
        Task task = getDbManager()
            .getInstance(getDisplayedTasksList().get(displayingId));
        return Formatter.formatTask(task, displayingId + 1 + "");
    }

    /**
     * Retrieve the task type from the database given the ID displayed.
     *
     * @param displayingId the id of the task
     * @return enum which specifies what type of task it is
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0111794E
    private int getTaskType(int displayingId) throws IOException {
        Task t = getDbManager().
            getInstance(getDisplayedTasksList().get(displayingId));
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
