import java.util.ArrayList;

/**
 * Class that represents a command object where it stores the type of command it
 * is and all its arguments.
 *
 * @author hooitong
 *
 */
public class Command {
    /* Must-have var for all types of command */
    private CommandType type;

    /* Information required for add, update */
    private String description;

    /* Information required for add, update */
    private ArrayList<DatePair> datePairs;

    /* Information required for view */
    private DatePair viewRange;
    private boolean viewAll;

    /* Information required for delete & update */
    private int taskId;

    /* Information required for search */
    private String keyword;

    /* Constructor for view command */
    public Command(CommandType type, boolean viewAll, DatePair viewRange) {
        this.type = type;
        this.viewAll = viewAll;
        this.viewRange = viewRange;
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

    public int getTaskId() {
        return taskId;
    }

    public String getKeyword() {
        return keyword;
    }
}
