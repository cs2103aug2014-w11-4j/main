package com.rubberduck.command;

import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Formatter;
import com.rubberduck.menu.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Concrete Command Class that can be executed to search the data store for
 * tasks containing the provided keyword and returns back the task details.
 */
//@author A0111794E
public class SearchCommand extends Command {

    private static final String MESSAGE_SEARCH_RESULT =
        "%s task with \"%s\" has been found.";
    private static final String SCHEDULE_SEPERATOR =
        "--------------------------------[  SCHEDULES  ]---------------------------------";
    private static final String FLOATING_SEPERATOR =
        "--------------------------------[    TASKS    ]---------------------------------";
    private static final String DEADLINE_SEPERATOR =
        "--------------------------------[  DEADLINES  ]---------------------------------";

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
     * Search for task based on description and return a Response containing
     * formatted string of tasks back to parent.
     *
     * @return Response object containing formatted tasks
     */
    @Override
    public Response execute() throws IOException {
        setPreviousDisplayCommand(this);
        getDisplayedTasksList().clear();

        for (Long databaseId : getDbManager().getValidIdList()) {
            String taskInDb =
                getDbManager().getInstance(databaseId).getDescription();
            taskInDb = taskInDb.toLowerCase();
            StringTokenizer st = new StringTokenizer(taskInDb);
            while(st.hasMoreElements()){
                if(st.nextToken().contains(keyword.toLowerCase())){
                    getDisplayedTasksList().add(databaseId);
                    break;
                }
            }           
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.RED
                                                              : Color.GREEN;

        StringBuilder viewCount = new StringBuilder();
        viewCount.append(ColorFormatter.format(
            String.format(MESSAGE_SEARCH_RESULT, getDisplayedTasksList().size(),
                          keyword), headerColor));

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
        Task task = getDbManager().
            getInstance(getDisplayedTasksList().get(displayingId));
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
}
