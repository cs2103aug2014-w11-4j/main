/**
 * Logic Class: Logic Component
 * Interacts with UI and DatabaseManager:
 * Act as middleman that communicates between userinput and database
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Logic {

    private static final String DATABASE_NAME = "database.xml";
    private static final String INVALID_COMMAND_MESSAGE = "Invalid Command";
    
    private static DatabaseManager<Task> dbManager = null;
    private static String currentDirectory = System.getProperty("user.dir");
    private static JournalController<Task> journal = null;

    private static HashMap<Long, Long> displayedTasksMap = new HashMap<Long, Long>();

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
        String result= "";
        try {
            if (cmdType.equals(CommandType.ADD)) {
                ArrayList<DatePair> dateRangeList = new ArrayList<DatePair>();
                dateRangeList.add(command.getDateRange());
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
                // yet to implement
            } else if (cmdType.equals(CommandType.UPDATE)) {
                // yet to implement
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
            journal.recordAction(null, id, description);
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
            journal.recordAction(null, id, description);
        } catch (IOException e) {
            return id;
        }
        return id;
    }

    /**
     * Mark a task as completed
     *
     * @param displayed id of the task
     *
     * @return database id of the task, if id == 0, task failed to be marked
     * @throws IOException 
     */
    public static long markTaskcompleted(long displayedId) throws IOException {
        long databaseId = displayedTasksMap.get(displayedId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        ArrayList<DatePair> oldDateList = oldTask.getDateList();
        long newTaskId = addCompletedTask(oldDescription, oldDateList);
        delete(databaseId);
        journal.recordAction(databaseId, newTaskId, oldDescription);
        return newTaskId;
    }

    /**
     * Update the task to the database
     *
     * @param id of the task
     * @param updated description
     * @param updated date list
     *
     * @return new id of the task, if id == 0, task failed to update
     */
    public static long updateTask(long displayedTaskId, String description,
            ArrayList<DatePair> dateList) throws IOException {
        long databaseId = displayedTasksMap.get(displayedTaskId);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        ArrayList<DatePair> oldDateList = oldTask.getDateList();
        long newTaskId;
        if (description == null && dateList != null) {
            newTaskId = addTask(oldDescription, dateList);
        } else if (description == null && dateList != null) {
            newTaskId = addTask(description, oldDateList);
        } else if (description != null && dateList != null) {
            newTaskId = addTask(description, dateList);
        } else {
            // magic string here, will change
            return databaseId;
        }
        delete(databaseId);
        journal.recordAction(databaseId, newTaskId, description);
        return newTaskId;

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
        for (Long databaseId : dbManager.getValidIdList()) {
            displayedTasksMap.put(displayingId, databaseId);
            taskString = taskString
                    + dbManager.getInstance(databaseId).toString() + "\n";
            displayingId++;
        }
        return taskString;
    }

    /**
     * Delete Task of Database
     *
     * @return if the task has been deleted from database
     */
    public static boolean delete(long id) {
        try {
            dbManager.markAsInvalid(id);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Non Official Method
     * Added quickly to assist testing
     * @return details of the task requested
     * @throws IOException
     *
     */
    public static String viewTask(long id) {
        try {
            return dbManager.getInstance(id).toString();
        } catch (IOException io) {
            return "error";
        }
    }

    /**
     * Search for task based on description
     *
     * @param the keyword that is used to search for the task
     */

    public static String searchWithKeyword(String keywords) throws IOException {
        String result = "";
        ArrayList<Task> relatedTasks = new ArrayList<Task>();

        displayedTasksMap.clear();
        Long displayingId = (long) 1;
        for (Long databaseId : dbManager.getValidIdList()) {
            String taskInDb = dbManager.getInstance(databaseId)
                    .getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keywords.toLowerCase())) {
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
            boolean inPeriod = isWithinPeriod(
                    dbManager.getInstance(databaseId), dateRange);
            if (inPeriod) {
                displayedTasksMap.put(displayingId, databaseId);
                result = result + dbManager.getInstance(databaseId).toString();
                displayingId++;
            }
        }

        return result;
    }

    /**
     * Check if the task is within the given dateRange
     *
     * List of allowed task within the range includes:
     *
     * Task start date end date falling within requested date range,
     * Task with start date falling after the requested date range's start date and no end date
     * Task with end date falling before the requested date range's end date and no start date
     * Task with start date before requested start date but with end date in between requested date range
     * Task with end date after requested end date but with start date in between requested date range
     * Task with date range overlapping the entire date requested date range
     *
     *
     *
     * @param task the task that is being used to check if it falls within the dateRange
     * @param dateRange DatePair object containing the start date and end date as queried
     * @return boolean to state if the task being compared falls within the range
     */

    private static boolean isWithinPeriod(Task task, DatePair dateRange) {
        boolean inPeriod = false;
        Calendar startDateCriteria = dateRange.getStartDate();
        Calendar endDateCriteria = dateRange.getEndDate();
        ArrayList<DatePair> taskDateList = task.getDateList();

        for (int i = 0; i < taskDateList.size(); i++) {
            DatePair taskDatePair = task.getDateList().get(i);
            Calendar taskStartDate = taskDatePair.getStartDate();
            Calendar taskEndDate = taskDatePair.getEndDate();

            if (taskStartDate == null && taskEndDate == null) {
                inPeriod = true;
                break;
            }

            if (taskEndDate == null) {
                if (taskStartDate.after(startDateCriteria)) {
                    inPeriod = true;
                    break;
                }
            } else if (taskStartDate == null) {
                if (taskEndDate.before(endDateCriteria)) {
                    inPeriod = true;
                    break;
                }
            }

            if (taskStartDate.after(startDateCriteria)
                    && taskEndDate.before(endDateCriteria)) {
                inPeriod = true;
                break;
            }

            if (taskStartDate.before(startDateCriteria)
                    && taskEndDate.after(endDateCriteria)) {
                inPeriod = true;
                break;
            }

            if (taskStartDate.before(startDateCriteria)
                    && taskEndDate.after(startDateCriteria)) {
                inPeriod = true;
                break;
            }

            if (taskStartDate.before(startDateCriteria)
                    && taskEndDate.after(endDateCriteria)) {
                inPeriod = true;
                break;

            }

        }

        return inPeriod;
    }

    /**
     * Undo previous action
     *
     */
    public static void undo() {
        try {
            journal.undo();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Redo previous action
     *
     */
    public static void redo() {
        try {
            journal.redo();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     *
     */
    public static DatabaseManager<Task> getDB() {
        return dbManager;
    }
}
