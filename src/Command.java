/**
 * Class that represents a command object where it stores the type of command it
 * is and all its arguments.
 *
 * @author hooitong
 *
 */
public class Command {
    private CommandType type;
    private String[] arguments;

    public Command(CommandType type, String[] args) {
        this.type = type;
        arguments = args;
    }

    public Command(CommandType type) {
        this(type, null);
    }

    public CommandType getType() {
        return type;
    }

    public String[] getArguments() {
        return arguments;
    }
}
