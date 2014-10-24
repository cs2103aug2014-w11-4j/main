import java.io.IOException;

public class ExitCommand extends Command{
	/**
	 * 
	 * @param type
	 */
    public ExitCommand(CommandType type) {
        this.type = type;
    }

	@Override
	public String execute() throws IOException {
		dbManager.closeFile();
        System.exit(0);
		return null;
	}
}
