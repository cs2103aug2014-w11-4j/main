import java.io.IOException;

public class UndoCommand extends Command {
	private static final String JOURNAL_MESSAGE_UNDONE = "Undone operation \"%s\".";

	/**
	 *
	 */
	public UndoCommand() {
		this.type = CommandType.UNDO;
	}
	
	/**
     * Method that undo previous action in the journal.
     */
	@Override
	public String execute() throws IOException {
		try {
			return String.format(JOURNAL_MESSAGE_UNDONE, dbManager.undo());
		} catch (UnsupportedOperationException e) { // Nothing to undo
			return e.getMessage();
		}

	}
}
