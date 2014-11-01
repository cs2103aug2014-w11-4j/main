package com.rubberduck.command;

import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;
import java.util.Collections;

/**
 * Concrete Command Class that can be executed to search the data store for
 * tasks containing the provided keyword and returns back the task details.
 *
 * @author Jason Sia
 */
public class SearchCommand extends Command {

    private static final String MESSAGE_SEARCH_RESULT =
        "%s task with \"%s\" has been found.";
    private static final String SCHEDULE_SEPERATOR =
        "--------------------------------[  SCHEDULES  ]---------------------------------";
    private static final String FLOATING_SEPERATOR =
        "--------------------------------[    TASKS    ]---------------------------------";
    private static final String DEADLINE_SEPERATOR =
        "--------------------------------[  DEADLINES  ]---------------------------------";

    private static final int CONSOLE_MAX_WIDTH = 80;

    private static final int FLOATING_TASK = 0;
    private static final int DEADLINE_TASK = 1;
    private static final int TIMED_TASK = 2;

    /* Information required for search */
    private String keyword;

    /**
     * Getter method for keyword.
     *
     * @return String object
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Public constructor of SearchCommand.
     *
     * @param keyword that is used to search for the task
     */
    public SearchCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Search for task based on description and return formatted string of tasks
     * back to parent.
     *
     * @return formatted string back to parent
     */
    @Override
    public String execute() throws IOException {
        setPreviousDisplayCommand(this);

        StringBuilder responseBuilder = new StringBuilder();

        getDisplayedTasksList().clear();

        for (Long databaseId : getDbManager().getValidIdList()) {
            String taskInDb =
                getDbManager().getInstance(databaseId).getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keyword.toLowerCase())) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.RED
                                                              : Color.GREEN;

        responseBuilder.append(ColorFormatter.format(
            String.format(MESSAGE_SEARCH_RESULT, getDisplayedTasksList().size(),
                          keyword), headerColor));

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
     * @author hooitong
     */
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

        stringBuilder.append(border + System.lineSeparator() + header +
                             System.lineSeparator() + border +
                             System.lineSeparator());

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
     * @author hooitong
     */
    private String formatTaskOutput(int displayingId) throws IOException {
        Task task = getDbManager().
            getInstance(getDisplayedTasksList().get(displayingId));
        return task.formatOutput(displayingId + 1 + "");
    }

    /**
     * Retrieve the task type from the database given the ID displayed.
     *
     * @param displayingId the id of the task
     * @return enum which specifies what type of task it is
     * @throws IOException occurs when dbManager encounters a problem with file
     */
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
}
