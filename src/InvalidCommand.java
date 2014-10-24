import java.io.IOException;

public class InvalidCommand extends Command{
	/**
	 * 
	 * @param type
	 * @param desc
	 */
    public InvalidCommand(CommandType type, String desc) {
        this.type = type;
        if (type == CommandType.SEARCH)
            this.keyword = desc;
        else if (type == CommandType.INVALID)
            this.description = desc;
    }

	@Override
	public String execute() throws IOException {
		return getDescription();
	}

}
