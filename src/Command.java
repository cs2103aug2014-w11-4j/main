import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that represents a command object where it stores the type of command it
 * is and all its arguments.
 *
 * @author hooitong
 *
 */
public class Command {
    /* Enum type to store all types of command and their possible variations */
    enum CommandType {
        /*
         * use var args to populate all possible variations for each command
         * type
         */
        VIEW("view", "display"), SEARCH("find", "lookup", "search"), ADD("add",
                "insert", "ins", "new"), DELETE("delete", "remove"), UPDATE(
                "change", "update", "edit"), UNDO("undo", "ud"), REDO("redo",
                "rd"), MARK("mark", "completed", "done"), EXIT("exit", "quit"), INVALID;

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
    private CommandType type;

    /* Information required for add, update */
    private String description;

    /* Information required for add, update */
    private ArrayList<DatePair> datePairs;

    /* Information required for view */
    private DatePair viewRange;
    private boolean viewAll;
    private boolean completed;

    /* Information required for delete & update */
    private int taskId;

    /* Information required for search */
    private String keyword;

    /* Constructor for view command */
    public Command(CommandType type, boolean viewAll, boolean completed,
            DatePair viewRange) {
        this.type = type;
        this.viewAll = viewAll;
        this.viewRange = viewRange;
        this.completed = completed;
    }

    /* Constructor for search & invalid command */
    public Command(CommandType type, String desc) {
        this.type = type;
        if (type == CommandType.SEARCH)
            this.keyword = desc;
        else if (type == CommandType.INVALID)
            this.description = desc;
    }

    /* Constructor for add command */
    public Command(CommandType type, String desc, ArrayList<DatePair> datePairs) {
        this.type = type;
        this.description = desc;
        this.datePairs = datePairs;
    }

    /* Constructor for delete, mark command */
    public Command(CommandType type, int taskId) {
        this.type = type;
        this.taskId = taskId;
    }

    /* Constructor for update command */
    public Command(CommandType type, int taskId, String desc,
            ArrayList<DatePair> datePairs) {
        this.type = type;
        this.taskId = taskId;
        this.description = desc;
        this.datePairs = datePairs;
    }

    /* Constructor for exit, undo, redo command */
    public Command(CommandType type) {
        this.type = type;
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
}
