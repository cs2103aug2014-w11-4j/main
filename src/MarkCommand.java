import java.io.IOException;


public class MarkCommand extends Command{
	private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";
	private static final String JOURNAL_MESSAGE_MARK_AS_COMPLETED = "Mark task \"%s\" as completed";
	private static final String JOURNAL_MESSAGE_MARK_AS_UNCOMPLETED = "Mark task \"%s\" as uncompleted";
	private static final String MESSAGE_MARK_COMPLETED = "\"%s\" has been marked to completed.";
	private static final String MESSAGE_MARK_UNCOMPLETED = "\"%s\" has been marked to uncompleted.";
	/**
	 *
	 * @param taskId displayed id of the task
	 */
	public MarkCommand(int taskId) {
        this.type = CommandType.MARK;
        this.taskId = taskId;
    }
	
	/**
     * Mark a task (completed to uncompleted and vice versa)
     * @return message of mark
     *
     * @throws IOException
     */
	@Override
	public String execute() throws IOException {
		if (!isValidDisplayedId(taskId)) {
			return MESSAGE_ERROR_WRONG_TASK_ID;
		}
		if (isCompletedTask(taskId)) {
			return markTaskUncompleted(taskId);
		} else {
			return markTaskCompleted(taskId);
		}
	}
	
    /**
     * Check whether the task is completed.
     *
     * @param displayedId of the task
     * @return true if the task is completed
     *
     * @throws IOException
     */
    public boolean isCompletedTask(int displayedId) throws IOException {
        long databaseId = displayedTasksList.get(displayedId - 1);
        Task oldTask = dbManager.getInstance(databaseId);
        return oldTask.getIsDone();
    }
    
	  /**
     * Mark a task as completed.
     *
     * @param displayedId displayed id of the task
     * @return message of mark task to completed
     *
     * @throws IOException
     */
    public String markTaskCompleted(int displayedId) throws IOException {
        long databaseId = displayedTasksList.get(displayedId - 1);
        Task oldTask = dbManager.getInstance(databaseId);
        assert !oldTask.getIsDone();
        oldTask.setIsDone(true);
        long newTaskId = dbManager.putInstance(oldTask);
        displayedTasksList.set(displayedId - 1, newTaskId);
        dbManager.markAsInvalid(databaseId);
        dbManager.recordAction(
                databaseId,
                newTaskId,
                String.format(JOURNAL_MESSAGE_MARK_AS_COMPLETED,
                        oldTask.getDescription()));
        return String.format(MESSAGE_MARK_COMPLETED, oldTask.getDescription());
    }

    /**
     * Mark a task as Uncompleted.
     *
     * @param displayedId displayed id of the task
     * @return message of mark task to uncompleted
     *
     * @throws IOException
     */
    public String markTaskUncompleted(int displayedId) throws IOException {
        long databaseId = displayedTasksList.get(displayedId - 1);
        Task oldTask = dbManager.getInstance(databaseId);
        assert oldTask.getIsDone();
        oldTask.setIsDone(false);
        long newTaskId = dbManager.putInstance(oldTask);
        displayedTasksList.set(displayedId - 1, newTaskId);
        dbManager.markAsInvalid(databaseId);
        dbManager.recordAction(
                databaseId,
                newTaskId,
                String.format(JOURNAL_MESSAGE_MARK_AS_UNCOMPLETED,
                        oldTask.getDescription()));
        return String.format(MESSAGE_MARK_UNCOMPLETED, oldTask.getDescription());
    }
}
