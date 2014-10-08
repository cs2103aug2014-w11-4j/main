/**
 * The logic center of the task manager which handles
 * all the logic behind functionalities.
 *
 * Interacts with UI and DatabaseManager.
 *
 */

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class Logic {
    private static final String DATABASE_NAME = "database.xml";

    private static DatabaseManager<Task> dbManager = null;
    private static String currentDirectory = System.getProperty("user.dir");

    private static HashMap<Long, Long> displayedTasksMap = new HashMap<Long, Long>();

    private static JournalController<Task> journal = null;
    private static final String JOURNAL_MESSAGE_UNDONE = "Undone operation \"%s\".";
    private static final String JOURNAL_MESSAGE_REDONE = "Redone operation \"%s\".";
    private static final String JOURNAL_MESSAGE_ADD = "Added task \"%s\"";
    private static final String JOURNAL_MESSAGE_MARK_AS_COMPLETED = "Mark task \"%s\" as completed";
    private static final String JOURNAL_MESSAGE_MARK_AS_UNCOMPLETED = "Mark task \"%s\" as uncompleted";
    private static final String JOURNAL_MESSAGE_UPDATE = "Updated task \"%s\"";
    private static final String JOURNAL_MESSAGE_DELETE = "Deleted task \"%s\"";

    private static final String MESSAGE_ADD = "\"%s\" has been successfully added.";
    private static final String MESSAGE_DELETE = "\"%s\" has been successfully deleted.";
    private static final String MESSAGE_UPDATE = "\"%s\" has been successfully updated.";
    private static final String MESSAGE_MARK_COMPLETED = "\"%s\" has been marked to completed.";
    private static final String MESSAGE_MARK_UNCOMPLETED = "\"%s\" has been marked to uncompleted.";
    private static final String MESSAGE_SEARCH_RESULT = "%s task with \"%s\" has been found.";

    private static final String MESSAGE_ERROR_WRONG_TASK_ID = "Wrong task ID!";
    private static final String MESSAGE_VIEWALL_RESULT = "You have %s uncompleted task(s).";
    private static final String MESSAGE_VIEWDATE_RESULT = "You have %s uncompleted task(s) %s.";
    private static final String MESSAGE_VIEWALL_CRESULT = "You have %s completed task(s).";
    private static final String MESSAGE_VIEWDATE_CRESULT = "You have %s completed task(s) %s.";

    private static final int CONSOLE_MAX_WIDTH = 80;

    /**
     * Start the database, if not found new database will be created.
     *
     * Create a new journal using the database that is created.
     *
     * @return states if the database has been started successfully
     */
    public static boolean startDatabase() {
        try {
            dbManager = new DatabaseManager<Task>(currentDirectory
                    + File.separator + DATABASE_NAME);
            journal = new JournalController<Task>(dbManager);

        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    /**
     * Executes command provided by user in the interface.
     *
     * @param command the command object that holds instruction from user
     * @return result of the command instruction
     */
    public static String executeCommand(Command command) {
        try {
            switch (command.getType()) {
                case ADD:
                    addTask(command.getDescription(), command.getDatePairs());
                    return String.format(MESSAGE_ADD, command.getDescription());

                case VIEW:
                    if (command.isViewAll()) {
                        return viewAll(command.isCompleted());
                    } else {
                        return viewByPeriod(command.getViewRange(),
                                command.isCompleted());
                    }

                case SEARCH:
                    return searchWithKeyword(command.getKeyword());

                case MARK:
                    return markTask(command.getTaskId());

                case DELETE:
                    return deleteTask(command.getTaskId());

                case UPDATE:
                    return updateTask(command.getTaskId(),
                            command.getDescription(), command.getDatePairs());

                case UNDO:
                    return undo();

                case REDO:
                    return redo();

                case INVALID:
                    return command.getDescription();

                case EXIT:
                    dbManager.closeFile();
                    System.exit(0);

                default: /* should never reach default */
                    throw new AssertionError(command.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
            // TODO: Need to discuss proper exception handling & logging
        }
    }

    /**
     * Create and add the task to the database.
     *
     * @param description of the task
     * @param dateList of possible DatePair
     * @return id of the task
     *
     * @throws IOException
     */
    public static long addTask(String description, ArrayList<DatePair> dateList)
            throws IOException {
        Task task = new Task(description, dateList);
        long id = dbManager.putInstance(task);
        journal.recordAction(null, id,
                String.format(JOURNAL_MESSAGE_ADD, task.getDescription()));
        return id;
    }

    /**
     * Check whether the task is completed.
     *
     * @param displayedId of the task
     * @return true if the task is completed
     *
     * @throws IOException
     */
    public static boolean isCompletedTask(long displayedId) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        return oldTask.isDone();
    }

    /**
     * Mark a task (completed to uncompleted and vice versa)
     * @param displayedId displayed id of the task
     * @return message of mark
     * @throws IOException
     */
    public static String markTask(long displayedId) throws IOException {
        if (!displayedTasksMap.containsKey(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        if (isCompletedTask(displayedId)) {
            return markTaskUncompleted(displayedId);
        } else {
            return markTaskCompleted(displayedId);
        }
    }

    /**
     * Mark a task as completed.
     *
     * @param displayedId displayed id of the task
     * @return message of mark task to completed
     *
     * @throws IOException
     */
    public static String markTaskCompleted(long displayedId) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        oldTask.setIsDone(true);
        long newTaskId = dbManager.putInstance(oldTask);
        displayedTasksMap.put(displayedId, newTaskId);
        dbManager.markAsInvalid(databaseId);
        journal.recordAction(
                databaseId,
                newTaskId,
                String.format(JOURNAL_MESSAGE_MARK_AS_COMPLETED,
                        oldTask.getDescription()));
        return String.format(MESSAGE_MARK_COMPLETED, oldTask.getDescription());
    }

    /**
     * Mark a task as Uncompleted.
     *
     * @param displayedId displayed id of the task
     * @return message of mark task to uncompleted
     *
     * @throws IOException
     */
    public static String markTaskUncompleted(long displayedId)
            throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        oldTask.setIsDone(false);
        long newTaskId = dbManager.putInstance(oldTask);
        displayedTasksMap.put(displayedId, newTaskId);
        dbManager.markAsInvalid(databaseId);
        journal.recordAction(
                databaseId,
                newTaskId,
                String.format(JOURNAL_MESSAGE_MARK_AS_UNCOMPLETED,
                        oldTask.getDescription()));
        return String
                .format(MESSAGE_MARK_UNCOMPLETED, oldTask.getDescription());
    }

    /**
     * Update the task to the database.
     *
     * @param displayedId id of the task as displayed in the last view command
     * @param description updated description, if not changed will be null
     * @param dateList updated date list, if not changed will be null
     * @return updated message with the displayed id
     */
    public static String updateTask(long displayedId, String description,
            ArrayList<DatePair> dateList) throws IOException {
        if (!displayedTasksMap.containsKey(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }

        long oldDatabaseId = displayedTasksMap.get(displayedId);
        Task task = dbManager.getInstance(oldDatabaseId);
        String oldDescription = task.getDescription();

        if (!description.isEmpty()) {
            task.setDescription(description);
        }
        if (!dateList.isEmpty()) {
            task.setDateList(dateList);
        }

        long newDatabaseId = dbManager.putInstance(task);
        dbManager.markAsInvalid(oldDatabaseId);

        displayedTasksMap.put(displayedId, newDatabaseId);
        journal.recordAction(oldDatabaseId, newDatabaseId,
                String.format(JOURNAL_MESSAGE_UPDATE, oldDescription));

        return String.format(MESSAGE_UPDATE, oldDescription);
    }

    /**
     * Return all the valid task stored in the database
     *
     * @return list of tasks and their information in the database
     */
    public static String viewAll(boolean isCompleted) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        Long displayingId = (long) 1;
        displayedTasksMap.clear();
        for (int i = 0; i < dbManager.getValidIdList().size(); i++) {
            Long databaseId = dbManager.getValidIdList().get(i);
            Task task = dbManager.getInstance(databaseId);
            if (isCompleted == task.isDone()) {
                displayedTasksMap.put(displayingId, databaseId);
                displayingId++;
            }
        }

        if (isCompleted) {
            responseBuilder.append(String.format(MESSAGE_VIEWALL_CRESULT,
                    displayedTasksMap.size()));
        } else {
            responseBuilder.append(String.format(MESSAGE_VIEWALL_RESULT,
                    displayedTasksMap.size()));
        }

        if (!displayedTasksMap.isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    /**
     * Delete Task of Database.
     *
     * @param displayedId displayed id of the task
     * @return delete message including the task description
     *
     * @throws IOException
     */
    public static String deleteTask(long displayedId) throws IOException {
        if (!displayedTasksMap.containsKey(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        dbManager.markAsInvalid(databaseId);
        displayedTasksMap.remove(displayedId);
        journal.recordAction(databaseId, null,
                String.format(JOURNAL_MESSAGE_DELETE, oldDescription));
        return String.format(MESSAGE_DELETE, oldDescription);
    }

    /**
     * Search for task based on description.
     *
     * @param keyword the keyword that is used to search for the task
     */

    public static String searchWithKeyword(String keyword) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        displayedTasksMap.clear();
        Long displayingId = (long) 1;
        for (Long databaseId : dbManager.getValidIdList()) {
            String taskInDb = dbManager.getInstance(databaseId)
                    .getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keyword.toLowerCase())) {
                displayedTasksMap.put(displayingId, databaseId);
                displayingId++;
            }
        }

        responseBuilder.append(String.format(MESSAGE_SEARCH_RESULT,
                displayedTasksMap.size(), keyword));

        if (!displayedTasksMap.isEmpty()) {
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

    public static String viewByPeriod(DatePair dateRange, boolean isCompleted)
            throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        displayedTasksMap.clear();
        Long displayingId = (long) 1;
        for (Long databaseId : dbManager.getValidIdList()) {
            Task task = dbManager.getInstance(databaseId);
            if (isCompleted == task.isDone() && task.hasDate()) {
                boolean inPeriod = dbManager.getInstance(databaseId)
                        .isWithinPeriod(dateRange);
                if (inPeriod) {
                    displayedTasksMap.put(displayingId, databaseId);
                    displayingId++;
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
                    displayedTasksMap.size(), range));
        } else {
            responseBuilder.append(String.format(MESSAGE_VIEWDATE_RESULT,
                    displayedTasksMap.size(), range));
        }

        if (!displayedTasksMap.isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    /**
     * Method that undo previous action in the journal.
     *
     */
    public static String undo() throws IOException {
        try {
            return String.format(JOURNAL_MESSAGE_UNDONE, journal.undo());
        } catch (UnsupportedOperationException e) { // Nothing to undo
            return e.getMessage();
        }
    }

    /**
     * Method that redo previous (undone) action in the journal.
     *
     */
    public static String redo() throws IOException {
        try {
            return String.format(JOURNAL_MESSAGE_REDONE, journal.redo());
        } catch (UnsupportedOperationException e) { // Nothing to redo
            return e.getMessage();
        }
    }

    /**
     * Helper method that formats the output of tasks.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     *
     * @throws IOException
     */
    private static String formatTaskOutput(Long displayingId)
            throws IOException {
        Task task = dbManager.getInstance(displayedTasksMap.get(displayingId));
        return task.formatOutput(displayingId);
    }

    private static String formatTaskListOutput() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String header = String.format("%-7s%-6s%-43s%-23s", "ID", "Done",
                "Task", "Date");
        String border = "";
        for (int i = 0; i < CONSOLE_MAX_WIDTH; i++) {
            border += "-";
        }

        stringBuilder.append(border + System.lineSeparator() + header
                + System.lineSeparator() + border + System.lineSeparator());

        for (long displayingId : displayedTasksMap.keySet()) {
            stringBuilder.append(formatTaskOutput(displayingId));
            stringBuilder.append(System.lineSeparator());
        }
        stringBuilder.append(border);

        return stringBuilder.toString();
    }

    /**
     * Non-official methods added quickly to assist testing.
     *
     * @return details of the task requested
     *
     * @throws IOException
     */
    public static String viewTask(long id) throws IOException {
        return dbManager.getInstance(id).toString();
    }

    public static DatabaseManager<Task> getDB() {
        return dbManager;
    }

}
