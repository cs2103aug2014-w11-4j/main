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

public class Logic {

    private static final String databaseName = "database.xml";
    private static DatabaseManager<Task> dbManager = null;
    private static String currentDirectory = System.getProperty("user.dir");
    private static JournalController<Task> journal = null;

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
                    + File.separator + databaseName);
            journal = new JournalController<Task>(dbManager);

        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;

    }

    /**
     *
     */

    public static String executeCommand(Command cmd) {
        CommandType cmdType = cmd.getType();

        if (cmdType.equals(CommandType.ADD)) {
            ArrayList<DatePair> dateRangeList = new ArrayList<DatePair>();
            dateRangeList.add(cmd.getDateRange());
            addTask(cmd.getDescription(), dateRangeList);
        } else if (cmdType.equals(CommandType.VIEW)) {
            viewAll();
        } else if (cmdType.equals(CommandType.SEARCH)) {
            // yet to determine search type
            // using search by keywords
            String result = searchWithKeyword(cmd.getDescription());
            System.out.println(result);
        } else if (cmdType.equals(CommandType.DELETE)) {
            // yet to implement
        } else if (cmdType.equals(CommandType.UPDATE)) {
            // yet to implement
        } else if (cmdType.equals(CommandType.INVALID)) {
            System.out.println("Invalid Command");
        } else if (cmdType.equals(CommandType.EXIT)) {
            System.exit(0);
        }

        //TODO: For each function, return output (String) back to interface.
        return null;
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
     * update the task to the database
     * 
     * @param id of the task
     * @param updated description
     * @param updated date list
     * 
     * @return new id of the task, if id == 0, task failed to update
     */
    public static long updateTask(long id, String description, ArrayList<DatePair> dateList) {
  
        long newTaskId = 0;
        newTaskId  = addTask(description, dateList);
		delete(id);
		journal.recordAction(id, newTaskId, description);
        return newTaskId;
        
    }
    /**
     * Return all the valid task stored in the database
     *
     * @return list of tasks and their information in the database which are valid
     */
    public static String viewAll() {
        String taskString = "";
        for (Task task : dbManager) {
            taskString = taskString + task.toString() + "\n";
        }
        return taskString;
    }

    /**
     * Non Official Method
     * Added quickly to assist testing
     *
     * @return if the task has been mark invalid
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

    public static String searchWithKeyword(String keywords) {
        String result = "";
        ArrayList<Task> relatedTasks = new ArrayList<Task>();
        for (Task task : dbManager) {
            String taskInDb = task.getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keywords.toLowerCase())) {
                relatedTasks.add(task);
            }
        }

        for (int i = 0; i < relatedTasks.size(); i++) {
            result = result + relatedTasks.get(i).toString();
        }

        return result;
    }

    /**
     *
     * @param dateRange DatePair object containing the start date and end date
     * @return result of all the tasks that are within the period as queried
     */

    public static String searchWithPeriod(DatePair dateRange) {
        String result = "";
        for (Task task : dbManager) {
            boolean inPeriod = isWithinPeriod(task, dateRange);
            if (inPeriod) {
                result = result + task.toString();
                break;
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