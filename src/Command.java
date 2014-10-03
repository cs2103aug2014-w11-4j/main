/**
 * Class that represents a command object where it stores the type of command it
 * is and all its arguments.
 *
 * @author hooitong
 *
 */
public class Command {
    /* Must-have for all types of command */
    private CommandType type;

    /* Information required for add, update */
    private String description;

    /* Information required for add, update, view */
    private DatePair dateRange;

    /* Information required for view */
    private boolean viewAll;

    /* Information required for delete & update */
    private int taskId;

    /* Information required for search */
    private String keyword;

    /* Constructor for view command */
    public Command(CommandType type, boolean viewAll, DatePair dateRange) {
        this.type = type;
        this.viewAll = viewAll;
        this.dateRange = dateRange;
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
    public Command(CommandType type, String desc, DatePair dateRange) {
        this.type = type;
        this.description = desc;
        this.dateRange = dateRange;
    }

    /* Constructor for delete, mark command */
    public Command(CommandType type, int taskId) {
        this.type = type;
        this.taskId = taskId;
    }

    /* Constructor for update command */
    public Command(CommandType type, int taskId, String desc, DatePair dateRange) {
        this.type = type;
        this.taskId = taskId;
        this.description = desc;
        this.dateRange = dateRange;
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

    public DatePair getDateRange() {
        return dateRange;
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
