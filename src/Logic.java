/**
 * Logic Class: Logic Component
 * Interacts with UI and DatabaseManager:
 * Act as middleman that communicates between userinput and database
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Logic {

    private static final String DATABASE_NAME = "database.xml";
    private static final String INVALID_COMMAND_MESSAGE = "Invalid Command";

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
    private static final String VIEW_TASK_HEADER = String.format(
            "%-7s%-6s%-43s%-23s", "ID", "Done", "Task", "Date");
    private static final String VIEW_TASK_BORDER = "--------------------------------------------------------------------------------";
    private static final String VIEW_TASK_MESSAGE = "You have %s uncompleted task(s).";

    /**
     * Start the database, if not found new database will be created
     *
     * Create a new journal using the database that is created
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
     * Executes command provided by user in the interface
     *
     * @param command the command object that holds instruction from user
     * @return result of the command instruction
     */

    public static String executeCommand(Command command) {
        CommandType cmdType = command.getType();
        String result = "";
        try {
            if (cmdType.equals(CommandType.ADD)) {
                ArrayList<DatePair> dateRangeList = new ArrayList<DatePair>();
                if (command.getDateRange() != null) { // TODO: Temporary fix by
                                                      // Huang Yue, please
                                                      // refactor this
                    dateRangeList.add(command.getDateRange());
                }
                addTask(command.getDescription(), dateRangeList);
                result = String.format(MESSAGE_ADD, command.getDescription());
            } else if (cmdType.equals(CommandType.VIEW)) {
                if (command.isViewAll()) {
                    result = viewAll();
                } else {
                    result = viewByPeriod(command.getDateRange());
                }
            } else if (cmdType.equals(CommandType.SEARCH)) {
                result = searchWithKeyword(command.getKeyword());
            } else if (cmdType.equals(CommandType.MARK)) {
                if (isCompletedTask(command.getTaskId())) {
                    result = markTaskUncompleted(command.getTaskId());
                } else {
                    result = markTaskCompleted(command.getTaskId());
                }
            } else if (cmdType.equals(CommandType.DELETE)) {
                result = delete(command.getTaskId());
            } else if (cmdType.equals(CommandType.UPDATE)) {
                String description = null;
                description = command.getDescription();
                ArrayList<DatePair> dateRangeList = new ArrayList<DatePair>();
                if (command.getDateRange() != null) { // TODO: Temporary fix by
                                                      // Huang Yue, please
                                                      // refactor this
                    dateRangeList.add(command.getDateRange());
                }
                updateTask(command.getTaskId(), description, dateRangeList);
            } else if (cmdType.equals(CommandType.UNDO)) {
                result = undo();
            } else if (cmdType.equals(CommandType.REDO)) {
                result = redo();
            } else if (cmdType.equals(CommandType.INVALID)) {
                result = INVALID_COMMAND_MESSAGE;
            } else if (cmdType.equals(CommandType.EXIT)) {
                dbManager.closeFile();
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO
        }

        // TODO: For each function, return output (String) back to interface.
        return result;
    }

    /**
     * Create and add the task to the database
     *
     * @param description of the task
     * @param dateList of possible DatePair
     *
     * @return id of the task
     *
     * @throws IOException
     */
    public static long addTask(String description, ArrayList<DatePair> dateList) throws IOException {
        Task task = new Task(description, dateList);
        long id = dbManager.putInstance(task);
        journal.recordAction(null, id,
                    String.format(JOURNAL_MESSAGE_ADD, task.getDescription()));
        return id;
    }

    /**
     * Check whether the task is completed
     *
     * @param displayedId of the task
     *
     * @return true if the task is completed
     * @throws IOException
     */
    public static boolean isCompletedTask(long displayedId) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        return oldTask.getIsDone();
    }
    /**
     * Mark a task as completed
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
     * Mark a task as Uncompleted
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
        return String.format(MESSAGE_MARK_UNCOMPLETED, oldTask.getDescription());
    }

    /**
     * Update the task to the database
     *
     * @param displayedId id of the task as displayed in the last view command
     * @param description updated description, if not changed will be null
     * @param dateList updated date list, if not changed will be null
     *
     * @return updated message with the displayed id
     */
    public static String updateTask(long displayedId, String description,
            ArrayList<DatePair> dateList) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        String result = "";
        long newTaskId;

        if (description == null && dateList != null) {
        	oldTask.setDateList(dateList);
        	newTaskId = dbManager.putInstance(oldTask);
        	displayedTasksMap.put(displayedId, newTaskId);
        } else if (description != null && dateList == null) {
        	oldTask.setDescription(description);
        	newTaskId = dbManager.putInstance(oldTask);
        	displayedTasksMap.put(displayedId, newTaskId);
        } else {
        	oldTask.setDescription(description);
        	oldTask.setDateList(dateList);
        	newTaskId = dbManager.putInstance(oldTask);
        	displayedTasksMap.put(displayedId, newTaskId);
        }
        dbManager.markAsInvalid(databaseId);
        result = String.format(MESSAGE_UPDATE, oldDescription);
        journal.recordAction(databaseId, newTaskId,
                String.format(JOURNAL_MESSAGE_UPDATE, oldDescription)); // TODO
        return result;

    }

    /**
     * Return all the valid task stored in the database
     *
     * @return list of tasks and their information in the database
     */
    public static String viewAll() throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        Long displayingId = (long) 1;
        displayedTasksMap.clear();
        for (int i = 0; i < dbManager.getValidIdList().size(); i++) {
            Long databaseId = dbManager.getValidIdList().get(i);
            displayedTasksMap.put(displayingId, databaseId);
            displayingId++;
        }

        responseBuilder.append(String.format(VIEW_TASK_MESSAGE, dbManager
                .getValidIdList().size()));

        if (!displayedTasksMap.isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    private static String formatTaskOutput(Long displayingId)
            throws IOException {
        Task task = dbManager.getInstance(displayedTasksMap.get(displayingId));
        return task.formatOutput(displayingId);
    }

    private static String formatTaskListOutput() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(VIEW_TASK_BORDER
                             + System.lineSeparator() + VIEW_TASK_HEADER
                             + System.lineSeparator() + VIEW_TASK_BORDER
                             + System.lineSeparator());

        for (long displayingId : displayedTasksMap.keySet()) {
            stringBuilder.append(formatTaskOutput(displayingId));
            stringBuilder.append(System.lineSeparator());
        }

        stringBuilder.append(VIEW_TASK_BORDER);

        return stringBuilder.toString();
    }

    /**
     * Delete Task of Database
     * @param displayedId displayed id of the task
     *
     * @return delete message including the task description
     * @throws IOException
     */
    public static String delete(long displayedId) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        dbManager.markAsInvalid(databaseId);
        journal.recordAction(databaseId, null,
                String.format(JOURNAL_MESSAGE_DELETE, oldDescription));
        return String.format(MESSAGE_DELETE, oldDescription);
    }

    /**
     * Non Official Method Added quickly to assist testing
     * @return details of the task requested
     * @throws IOException
     *
     */
    public static String viewTask(long id) throws IOException {
        return dbManager.getInstance(id).toString();
    }

    /**
     * Search for task based on description
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

        responseBuilder.append(String.format(MESSAGE_SEARCH_RESULT, displayedTasksMap.size(),
                keyword));

        if (!displayedTasksMap.isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    /**
     * Searches the Database for a related task that coincides with the
     * dateRange requested
     *
     * @param dateRange DatePair object containing the start date and end date
     * @return result of all the tasks that are within the period as queried
     */

    public static String viewByPeriod(DatePair dateRange) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();

        displayedTasksMap.clear();
        Long displayingId = (long) 1;
        for (Long databaseId : dbManager.getValidIdList()) {
            boolean inPeriod = dbManager.getInstance(databaseId)
                    .isWithinPeriod(dateRange);
            if (inPeriod) {
                displayedTasksMap.put(displayingId, databaseId);
                displayingId++;
            }
        }

        responseBuilder.append(String.format(VIEW_TASK_MESSAGE, dbManager
                .getValidIdList().size()));

        if (!displayedTasksMap.isEmpty()) {
            responseBuilder.append(System.lineSeparator());
            responseBuilder.append(formatTaskListOutput());
        }

        return responseBuilder.toString();
    }

    /**
     * Undo previous action
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
     * Redo previous action
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
     *
     */
    public static DatabaseManager<Task> getDB() {
        return dbManager;
    }

}
