import java.io.IOException;

public class ClearCommand extends Command {

    /**
     *
     */
    public ClearCommand() {
        this.type = CommandType.CLEAR;
    }

    /**
     * Clear the screen of the current interface.
     *
     * @throws IOException
     */
    @Override
    public String execute() throws IOException {
        final String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            Runtime.getRuntime().exec("cls");
        } else {
            Runtime.getRuntime().exec("clear");
        }

        return "";
    }
}
