import java.io.IOException;

public class DeleteCommand extends Command {
	private static final String JOURNAL_MESSAGE_DELETE = "Deleted task \"%s\"";
	private static final String MESSAGE_DELETE = "\"%s\" has been successfully deleted.";
	private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";

	/**
	 *
	 * @param taskId displayed id of the task
	 */
	public DeleteCommand(int taskId) {
		this.taskId = taskId;
	}

    /**
     * Delete Task of Database.
     *
     * @return delete message including the task description
     *
     * @throws IOException
     */
	@Override
	public String execute() throws IOException {
		if (!isValidDisplayedId(taskId)) {
			return MESSAGE_ERROR_WRONG_TASK_ID;
		}
		long databaseId = displayedTasksList.get(taskId - 1);
		Task oldTask = dbManager.getInstance(databaseId);
		String oldDescription = oldTask.getDescription();
		dbManager.markAsInvalid(databaseId);
		displayedTasksList.set(taskId - 1, (long) -1);
		dbManager.recordAction(databaseId, null,
				String.format(JOURNAL_MESSAGE_DELETE, oldDescription));
		return String.format(MESSAGE_DELETE, oldDescription);

	}
}
