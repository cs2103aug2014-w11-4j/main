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

    public enum ViewFilter {
        TASK, DEADLINE, SCHEDULE
    }

    public enum ViewType {
        ALL, DATE, PREV, OVERDUE
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

    private static final ArrayList<ViewFilter> VIEW_SELECTION_ALL =
        new ArrayList<ViewFilter>(
            Arrays.asList(ViewFilter.DEADLINE, ViewFilter.SCHEDULE,
                          ViewFilter.TASK));

    private static final int FLOATING_TASK = 0;
    private static final int DEADLINE_TASK = 1;
    private static final int SCHEDULE_TASK = 2;

    private DatePair viewRange;
    private boolean completed;
    private ViewType viewType;
    private ArrayList<ViewFilter> viewSelection;

    /**
     * Getter method for viewRange.
     *
     * @return viewRange as DatePair
     */
    public DatePair getViewRange() {
        return viewRange;
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
     * Getter method for viewType.
     *
     * @return viewType as ViewType
     */
    public ViewType getViewType() {
        return viewType;
    }

    /**
     * Getter method for viewSelection.
     *
     * @return viewSelection as ArrayList
     */
    public ArrayList<ViewFilter> getViewSelection() {
        return viewSelection;
    }

    /**
     * Public constructor for ViewCommand.
     *
     * @param viewType      the type of view to execute
     * @param completed     true if all completed tasks should be returned
     *                      instead
     * @param viewRange     date range to view tasks in
     * @param viewSelection specified view scope from user
     */
    public ViewCommand(ViewType viewType, boolean completed, DatePair viewRange,
                       ArrayList<ViewFilter> viewSelection) {
        this.viewType = viewType;
        this.completed = completed;
        this.viewRange = viewRange;
        if (viewSelection != null && viewSelection.isEmpty()) {
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

        switch (viewType) {
            case ALL:
                return viewAll();

            case DATE:
                return viewByPeriod();

            case OVERDUE:
                return viewOverdue();

            case PREV:
                return viewPrev();

            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Return all the valid task stored in the database.
     *
     * @return Response object containing result of all tasks
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewAll() throws IOException {
        assert viewSelection != null;

        getDisplayedTasksList().clear();

        for (int i = 0; i < getDbManager().getValidIdList().size(); i++) {
            Long databaseId = getDbManager().getValidIdList().get(i);
            Task task = getDbManager().getInstance(databaseId);
            if (completed == task.getIsDone() && viewSelection.
                contains(getTaskType(task))) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.GREEN
                                                              : Color.YELLOW;

        Color cHeaderColor = getDisplayedTasksList().isEmpty() ? Color.YELLOW
                                                               : Color.GREEN;
        StringBuilder viewCount = new StringBuilder();
        if (completed) {
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

        setPreviousDisplayCommand(this);
        return new Response("", viewCount.toString(), formatTaskListOutput());
    }

    /**
     * Return overdue tasks in database, based on user selections
     *
     * @return Response object containing result of all tasks
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewOverdue() throws IOException {
        assert viewSelection != null;

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

        setPreviousDisplayCommand(this);
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
     * @return Response object containing result of all tasks within range
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewByPeriod() throws IOException {
        assert viewSelection != null;
        assert viewRange != null;

        getDisplayedTasksList().clear();
        for (Long databaseId : getDbManager().getValidIdList()) {
            Task task = getDbManager().getInstance(databaseId);
            if (completed == task.getIsDone() && task.hasDate() &&
                viewSelection.contains(getTaskType(task))) {
                if (task.isWithinPeriod(viewRange)) {
                    getDisplayedTasksList().add(databaseId);
                }
            }
        }

        String range = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.US);
        if (viewRange.hasDateRange()) {
            String startDate =
                dateFormat.format(viewRange.getStartDate().getTime());
            String endDate =
                dateFormat.format(viewRange.getEndDate().getTime());

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
        if (completed) {
            viewCount.append(ColorFormatter.format(String.format(
                MESSAGE_VIEWDATE_CRESULT, getDisplayedTasksList().size(),
                range), headerColor));
        } else {
            viewCount.append(ColorFormatter.format(String.format(
                MESSAGE_VIEWDATE_RESULT, getDisplayedTasksList().size(),
                range), headerColor));
        }

        setPreviousDisplayCommand(this);
        return new Response("", viewCount.toString(), formatTaskListOutput());
    }

    /**
     * Execute the previous command with viewSelection and completed of the
     * current command.
     *
     * @return the formatted string of all tasks involved
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private Response viewPrev() throws IOException {
        if (viewSelection.size() == 1 &&
            viewSelection.contains(ViewFilter.TASK)) {
            this.viewType = ViewType.ALL;
            return execute();
        }

        if (getPreviousDisplayCommand() instanceof ViewCommand) {
            ViewCommand prev = (ViewCommand) getPreviousDisplayCommand();
            this.viewType = prev.getViewType();
            this.viewRange = prev.getViewRange();
            return execute();
        } else {
            return getPreviousDisplayCommand().execute();
        }
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
                } else if (currentType == SCHEDULE_TASK) {
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
            return SCHEDULE_TASK;
        }
    }

    /**
     * Retrieve the viewType based on the task provided.
     *
     * @param task object to get type
     * @return ViewFilter of the object
     */

    private ViewFilter getTaskType(Task task) {
        if (task.isFloatingTask()) {
            return ViewFilter.TASK;
        } else if (task.isDeadline()) {
            return ViewFilter.DEADLINE;
        } else {
            return ViewFilter.SCHEDULE;
        }
    }

}
