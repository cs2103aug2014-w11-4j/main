import java.io.IOException;

public class InvalidCommand extends Command{
	/**
	 *
	 * @param description of the invalid command
	 */
    public InvalidCommand(String description) {
        this.type = CommandType.INVALID;
        this.description = description;
    }

	@Override
	public String execute() throws IOException {
		return getDescription();
	}

}
