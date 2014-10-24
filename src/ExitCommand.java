import java.io.IOException;

public class ExitCommand extends Command{
	/**
	 *
	 */
    public ExitCommand() {
        this.type = CommandType.EXIT;
    }

	@Override
	public String execute() throws IOException {
		dbManager.closeFile();
        System.exit(0);
		return null;
	}
}
