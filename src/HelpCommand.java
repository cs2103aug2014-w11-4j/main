import java.io.IOException;


public class HelpCommand extends Command{

	/**
	 *
	 */
	public HelpCommand() {
		this.type = CommandType.HELP;
	}
	
    /**
     * Shows the available command for the end user in the system. TODO: May
     * need refactoring as currently it is hardcoded.
     *
     * @return a String object containing all the commands available
     */
	@Override
	public String execute() throws IOException {

		StringBuilder sb = new StringBuilder();
		sb.append("Here are for the available commands in RubberDuck.");
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "view",
				"View your agenda given a date range or \"all\"."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "search",
				"Search for tasks related to the given keyword."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "add",
				"Add a new task of provided description with optional date."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "delete",
				"Delete a task from the system given task ID."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "update",
				"Update task given task ID and new information."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "undo",
				"Undo your previous action."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "redo",
				"Redo your undone action."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "mark",
				"Mark any task to complete/incomplete given task ID."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "confirm",
				"Confirm any tentative task given task ID and date ID."));
		sb.append(System.lineSeparator());
		sb.append(String.format("%-15s%-65s", "exit", "Exit from RubberDuck."));
		return sb.toString();
	}

}
