import java.io.IOException;

public class ExitCommand extends Command {

    @Override
    public String execute() throws IOException {
        getDbManager().closeFile();
        System.exit(0);
        return "Closing RubberDuck.";
    }
}
