package com.rubberduck.command;

import java.io.IOException;
import java.util.Collections;

import com.rubberduck.logic.Task;

public class SearchCommand extends Command {
    private static final String MESSAGE_SEARCH_RESULT = "%s task with \"%s\" has been found.";
    protected static final int CONSOLE_MAX_WIDTH = 80;

    /* Information required for search */
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword that is used to search for the task
     */
    public SearchCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Search for task based on description.
     */
    @Override
    public String execute() throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        getDisplayedTasksList().clear();
        for (Long databaseId : getDbManager().getValidIdList()) {
            String taskInDb = getDbManager().getInstance(databaseId)
                    .getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keyword.toLowerCase())) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        responseBuilder.append(String.format(MESSAGE_SEARCH_RESULT,
                getDisplayedTasksList().size(), keyword));

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
