import java.io.IOException;

public class RedoCommand extends Command {
	private static final String JOURNAL_MESSAGE_REDONE = "Redone operation \"%s\".";

	/**
	 *
	 */
	public RedoCommand() {
		this.type = CommandType.REDO;
	}
	
	 /**
     * Method that redo previous (undone) action in the journal.
     */
	@Override
	public String execute() throws IOException {
		try {
			return String.format(JOURNAL_MESSAGE_REDONE, dbManager.redo());
		} catch (UnsupportedOperationException e) { // Nothing to redo
			return e.getMessage();
		}
	}
}
