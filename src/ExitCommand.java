import java.io.IOException;

public class ExitCommand extends Command{
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
