import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
	protected ArrayList<Long> displayedTasksList = new ArrayList<Long>();
	protected DatabaseManager<Task> dbManager;
	private static final String MESSAGE_ERROR_DATABASE_IOEXCEPTION = "Exception has occured when accessing local storage.";
	
    /* Constructor for view command */
    public Command(CommandType type, boolean viewAll, boolean completed,
            DatePair viewRange) {
        this.type = type;
        this.viewAll = viewAll;
        this.viewRange = viewRange;
        this.completed = completed;
    }

    /* Constructor for update command */
    public Command(CommandType type, int taskId, String desc,
            ArrayList<DatePair> datePairs) {
        this.type = type;
        this.taskId = taskId;
        this.description = desc;
        this.datePairs = datePairs;
    }

    /* Constructor for confirm command */
    public Command(CommandType type, int taskId, int dateId) {
        this.type = type;
        this.taskId = taskId;
        this.dateId = dateId;
    }

    /* Constructor for search & invalid command */
    public Command(CommandType type, String desc) {
        this.type = type;
        if (type == CommandType.SEARCH)
            this.keyword = desc;
        else if (type == CommandType.INVALID)
            this.description = desc;
    }

    /* Constructor for delete, mark command */
    public Command(CommandType type, int taskId) {
        this.type = type;
        this.taskId = taskId;
    }

    /* Constructor for exit, undo, redo, help command */
    public Command(CommandType type) {
        this.type = type;
    }
    
    public Command() {
    	
    }

    /**
     * Start the database, if not found new database will be created.
     *
     * @return states if the database has been started successfully
     */
    public boolean startDatabase() {
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
     * Clear the screen of the current interface.
     *
     * @throws IOException
     */
    protected String clearScreen() throws IOException {
        final String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            Runtime.getRuntime().exec("cls");
        } else {
            Runtime.getRuntime().exec("clear");
        }

        return "";
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

    public DatabaseManager<Task> getDB() {
        return dbManager;
    }

    public abstract String execute() throws IOException;
    
    
    
}
