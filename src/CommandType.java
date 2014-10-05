import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enum type to store all types of command and their possible variations.
 *
 * @author hooitong
 *
 */
public enum CommandType {
    /* use var args to populate all possible variations for each command type */
    VIEW("view", "display"), SEARCH("find", "lookup", "search"), ADD("add",
            "insert", "ins", "new"), DELETE("delete", "remove"), UPDATE(
            "change", "update", "edit"), UNDO("undo", "ud"), REDO("redo", "rd"), MARK(
            "mark", "completed", "done"), EXIT("exit", "quit"), INVALID("invalid");

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
