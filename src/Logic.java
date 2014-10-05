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
import java.util.Iterator;
import java.util.Set;

public class Logic {

    private static final String DATABASE_NAME = "database.xml";
    private static final String INVALID_COMMAND_MESSAGE = "Invalid Command";

    private static DatabaseManager<Task> dbManager = null;
    private static String currentDirectory = System.getProperty("user.dir");

    private static HashMap<Long, Long> displayedTasksMap = new HashMap<Long, Long>();

    private static JournalController<Task> journal = null;
    private static final String JOURNAL_MESSAGE_UNDONE = "Undone operation \"%s\"";
    private static final String JOURNAL_MESSAGE_REDONE = "Redone operation \"%s\"";
    private static final String JOURNAL_MESSAGE_ADD = "Add task %s";
    private static final String JOURNAL_MESSAGE_MARK_AS_COMPLETED = "Mark task %s as completed";
    private static final String JOURNAL_MESSAGE_UPDATE = "Update task %s";
    private static final String JOURNAL_MESSAGE_DELETE = "Delete task %s";

    /**
     * Start the database,
     * if not found new database will be created
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
            } else if (cmdType.equals(CommandType.VIEW)) {
                if (command.isViewAll()) {
                    result = viewAll();
                } else {
                    result = viewByPeriod(command.getDateRange());
                }
            } else if (cmdType.equals(CommandType.SEARCH)) {
                result = searchWithKeyword(command.getKeyword());
            } else if (cmdType.equals(CommandType.DELETE)) {
                delete(command.getTaskId());
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
     * @return id of the task, if id == 0, task failed to create
     */
    public static long addTask(String description, ArrayList<DatePair> dateList) {
        long id = 0;
        Task task = new Task(description, dateList);
        try {
            id = dbManager.putInstance(task);
            journal.recordAction(null, id,
                    String.format(JOURNAL_MESSAGE_ADD, task.getDescription()));
        } catch (IOException e) {
            return id;
        }
        return id;
    }

    /**
     * Create and add the completed task to the database
     *
     * @param description of the task
     * @param dateList of possible DatePair
     *
     * @return id of the task, if id == 0, task failed to create
     */
    public static long addCompletedTask(String description,
            ArrayList<DatePair> dateList) {
        long id = 0;
        Task task = new Task(description, dateList);
        task.setIsDone(true);
        try {
            id = dbManager.putInstance(task);
        } catch (IOException e) {
            return id;
        }
        return id;
    }

    /**
     * Mark a task as completed
     *
     * @param displayed id of the task
     * @return new id of the task
     *
     * @throws IOException 
     */
    public static long markTaskcompleted(long displayedId) throws IOException { 
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        ArrayList<DatePair> oldDateList = oldTask.getDateList();
        long newTaskId = addCompletedTask(oldDescription, oldDateList);
        dbManager.markAsInvalid(databaseId);
        journal.recordAction(
                databaseId,
                newTaskId,
                String.format(JOURNAL_MESSAGE_MARK_AS_COMPLETED,
                        oldTask.getDescription()));
        return newTaskId;
    }

    /**
     * Update the task to the database
     *
     * @param displayedId id of the task as displayed in the last view command
     * @param description updated description, if not changed will be null
     * @param dateList updated date list, if not changed will be null
     *
     * @return new id of the task, if id == 0, task failed to update
     */
    public static String updateTask(long displayedId, String description, 
            ArrayList<DatePair> dateList) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        ArrayList<DatePair> oldDateList = oldTask.getDateList();
        String result = "";
        long newTaskId;
        
        if (description == null && dateList != null) {
            newTaskId = addTask(oldDescription, dateList);
        } else if (description == null && dateList != null) {
            newTaskId = addTask(description, oldDateList);
        } else {
            newTaskId = addTask(description, dateList);
        }
        dbManager.markAsInvalid(databaseId);
        result = String.format(JOURNAL_MESSAGE_UPDATE, displayedId); 
        journal.recordAction(databaseId, newTaskId,
                String.format(JOURNAL_MESSAGE_UPDATE, oldTask.getDescription())); // TODO:
                                                                                  // Shall
                                                                                  // the
                                                                                  // description
                                                                                  // contain
                                                                                  // updated
                                                                                  // task
                                                                                  // info?
        return result;

    }

    /**
     * Return all the valid task stored in the database
     *
     * @return list of tasks and their information in the database
     */
    public static String viewAll() throws IOException {
        String taskString = "";
        Long displayingId = (long) 1;
        displayedTasksMap.clear();

        for (int i = 0; i < dbManager.getValidIdList().size(); i++) {
            Long databaseId = dbManager.getValidIdList().get(i);
            displayedTasksMap.put(displayingId, databaseId);
            if (displayingId == displayedTasksMap.size() - 1) {
                taskString = taskString + displayingId + ": "
                        + dbManager.getInstance(databaseId).toString();
                displayingId++;
            } else {
                taskString = taskString + displayingId + ": "
                        + dbManager.getInstance(databaseId).toString() + "\n";
                displayingId++;
            }

        }
        return taskString;
    }

    /**
     * Delete Task of Database
     *
     * @return if the task has been deleted from database
     */
    public static String delete(long id) {
        try {
            dbManager.markAsInvalid(id);
            journal.recordAction(id, null, "delete task"); // TODO
            return String.format(JOURNAL_MESSAGE_DELETE, id);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * Non Official Method
     * Added quickly to assist testing
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
        String result = "";
        ArrayList<Task> relatedTasks = new ArrayList<Task>();

        displayedTasksMap.clear();
        Long displayingId = (long) 1;
        for (Long databaseId : dbManager.getValidIdList()) {
            String taskInDb = dbManager.getInstance(databaseId)
                    .getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keyword.toLowerCase())) {
                displayedTasksMap.put(displayingId, databaseId);
                relatedTasks.add(dbManager.getInstance(databaseId));
                displayingId++;
            }
        }

        for (int i = 0; i < relatedTasks.size(); i++) {
            result = result + relatedTasks.get(i).toString();
        }

        return result;
    }

    /**
     * Searches the Database for a related task that coincides with the dateRange requested 
     *
     * @param dateRange DatePair object containing the start date and end date
     * @return result of all the tasks that are within the period as queried
     */

    public static String viewByPeriod(DatePair dateRange) throws IOException {
        String result = "";

        displayedTasksMap.clear();
        Long displayingId = (long) 1;
        for (Long databaseId : dbManager.getValidIdList()) {
            boolean inPeriod = dbManager.getInstance(databaseId)
                    .isWithinPeriod(dateRange);
            if (inPeriod) {
                displayedTasksMap.put(displayingId, databaseId);
                // result = result +
                // dbManager.getInstance(databaseId).toString();
                displayingId++;
            }
        }

        Set<Long> idSet = displayedTasksMap.keySet();
        Iterator<Long> idSetIterator = idSet.iterator();
        while (idSetIterator.hasNext()) {
            Long key = idSetIterator.next();
            if (key == displayedTasksMap.size()) {
                result = result
                        + key.toString()
                        + ": "
                        + dbManager.getInstance(displayedTasksMap.get(key))
                                .toString();
            } else {
                result = result
                        + key.toString()
                        + ": "
                        + dbManager.getInstance(displayedTasksMap.get(key))
                                .toString() + "\n";
            }

        }

        return result;
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
