/**
 * Logic Class: Logic Component
 * Interacts with UI and DatabaseManager: 
 * Act as middleman that communicates between userinput and database 
 * 
 */

import java.io.IOException;
import java.util.ArrayList;

public class Logic {

    private static final String databaseName = "database.xml";
    private static DatabaseManager<Task> dbManager = null;
    private static String currentDirectory = System.getProperty("user.dir");
    private static ArrayList<Long> log = new ArrayList<Long>();

    /**
     * Start the database,
     * if not found new database will be created
     * 
     * @return states if the database has been started successfully
     */
    public static boolean startDatabase() {
        try {
            dbManager = new DatabaseManager<Task>(currentDirectory + "\\"
                    + databaseName);
        } catch (IOException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;

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
            log.add(id);
        } catch (IOException e) {
            return id;
        }
        return id;
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

}