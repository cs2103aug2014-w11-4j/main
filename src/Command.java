import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents a command object where it stores the type of command it
 * is and all its arguments.
 *
 * @author hooitong
 *
 */
public abstract class Command {
    /* Enum type to store all types of command and their possible variations */
    enum CommandType {
        /*
         * use var args to populate all possible variations for each command
         * type
         */
        VIEW("view", "display"), SEARCH("find", "lookup", "search"), ADD("add",
                "insert", "ins", "new"), DELETE("delete", "remove"), UPDATE(
                "change", "update", "edit"), UNDO("undo", "ud"), REDO("redo",
                "rd"), MARK("mark", "completed", "done"), CONFIRM("confirm"), HELP(
                "?", "help"), CLEAR("cls", "clear"), EXIT("exit", "quit"), INVALID;

        private List<String> tags;
        private static final Map<String, CommandType> tagMap = new HashMap<String, CommandType>();

        private CommandType(String... tags) {
            this.tags = Arrays.asList(tags);
        }

        /**
         * Method to initialize/populate the tagMap for other methods.
         */
        static {
            for (CommandType command : CommandType.values()) {
                for (String tag : command.tags) {
                    tagMap.put(tag, command);
                }
            }
        }

        /**
         * Method used to return the command type based on user input
         *
         * @param input the input to retrieve command
         * @return the correct command type based on input
         */
        public static CommandType getCommandType(String input) {
            if (input == null || input.isEmpty()) {
                return CommandType.INVALID;
            }

            CommandType cmd = tagMap.get(input.toLowerCase());

            if (cmd == null) {
                return CommandType.INVALID;
            } else {
                return cmd;
            }
        }

    }

    /* Must-have var for all types of command */
    protected CommandType type;

    /* Information required for add, update */
    protected String description;

    /* Information required for add, update */
    protected ArrayList<DatePair> datePairs;

    /* Information required for view */
    protected DatePair viewRange;
    protected boolean viewAll;
    protected boolean completed;

    /* Information required for delete & update */
    protected int taskId;

    /* Information required for search */
    protected String keyword;

    /* Information required for confirm */
    protected int dateId;
    
	protected static final int CONSOLE_MAX_WIDTH = 80;

	protected static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	protected static final String DATABASE_NAME = "database.xml";
	protected static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
	protected static ArrayList<Long> displayedTasksList = new ArrayList<Long>();
	protected static DatabaseManager<Task> dbManager;
	private static final String MESSAGE_ERROR_DATABASE_IOEXCEPTION = "Exception has occured when accessing local storage.";
	
    public Command() {
    	
    }

    /**
     * Start the database, if not found new database will be created.
     *
     * @return states if the database has been started successfully
     */
    public static boolean startDatabase() {
        try {
            dbManager = new DatabaseManager<Task>(CURRENT_DIRECTORY
                    + File.separator + DATABASE_NAME);
        } catch (IOException e) {
            logger.log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return false;
        }
        return true;
    }
    /* Getters methods for variables in the class */

    public CommandType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public DatePair getViewRange() {
        return viewRange;
    }

    public ArrayList<DatePair> getDatePairs() {
        return datePairs;
    }

    public boolean isViewAll() {
        return viewAll;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getDateId() {
        return dateId;
    }
    
    /**
     * Helper method that formats the output of tasks.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     *
     * @throws IOException
     */
    protected String formatTaskOutput(int displayingId) throws IOException {
        Task task = dbManager.getInstance(displayedTasksList.get(displayingId));
        return task.formatOutput(displayingId + 1);
    }

    protected String formatTaskListOutput() throws IOException {
        Collections.sort(displayedTasksList, dbManager.getInstanceComparator());

        StringBuilder stringBuilder = new StringBuilder();
        String header = String.format("%-7s%-6s%-43s%-24s", "ID", "Done",
                "Task", "Date");
        String border = "";
        for (int i = 0; i < CONSOLE_MAX_WIDTH; i++) {
            border += "-";
        }

        stringBuilder.append(border + System.lineSeparator() + header
                + System.lineSeparator() + border + System.lineSeparator());

        for (int i = 0; i < displayedTasksList.size(); i++) {
            stringBuilder.append(formatTaskOutput(i));
            stringBuilder.append(System.lineSeparator());
        }
        stringBuilder.append(border);

        return stringBuilder.toString();
    }

    protected boolean isValidDisplayedId(int displayedId) {
        return !(displayedId > displayedTasksList.size() || displayedId <= 0 || displayedTasksList.get(displayedId - 1) == -1);
    }


    /**
     * Method used to check whether a task has any potential conflict in current
     * database.
     *
     * @param t the Task object
     * @return true if there is a conflict else false
     *
     * @throws IOException
     */
    public boolean checkConflictWithDB(Task t) throws IOException {
        boolean isConflict = false;
        if (t.isFloatingTask()) {
            return isConflict;
        }
        ArrayList<Long> validIDList = dbManager.getValidIdList();
        for (int i = 0; i < validIDList.size(); i++) {
            Task storedTask = dbManager.getInstance(validIDList.get(i));
            if (!storedTask.getIsDone() && !storedTask.isFloatingTask()) {
                isConflict = t.hasConflictWith(storedTask);
            }
        }

        return isConflict;
    }

    /**
     * Check if any end date in the DateList has already past the current date
     * and time during execution.
     *
     * @param dateList the ArrayList of DatePair
     * @return true if there is a date that has already past else false
     */
    public boolean isDateBeforeNow(ArrayList<DatePair> dateList) {
        if (dateList.size() > 0) {
            for (DatePair dp : dateList) {
                if (dp.getEndDate().before(Calendar.getInstance())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Non-official methods added quickly to assist testing.
     *
     * @return details of the task requested
     *
     * @throws IOException
     */
    public String viewTask(long id) throws IOException {
        return dbManager.getInstance(id).toString();
    }

    public static DatabaseManager<Task> getDB() {
        return dbManager;
    }

    public abstract String execute() throws IOException;
    
    public String safeExecute() {
    	try {
    		return execute();
    	} catch (IOException e) {
            logger.log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return MESSAGE_ERROR_DATABASE_IOEXCEPTION;
        }
    }
    
}
