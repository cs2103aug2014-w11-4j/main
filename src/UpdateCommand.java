import java.io.IOException;
import java.util.ArrayList;

public class UpdateCommand extends Command {
	private static final String JOURNAL_MESSAGE_UPDATE = "Updated task \"%s\"";
	private static final String MESSAGE_UPDATE = "\"%s\" has been successfully updated.";
	private static final String MESSAGE_UPDATE_PAST = "You cannot update the end date thats already passed.";
	private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";

	public UpdateCommand(CommandType type, int taskId, String desc,
			ArrayList<DatePair> datePairs) {
		this.type = type;
		this.taskId = taskId;
		this.description = desc;
		this.datePairs = datePairs;
	}

	@Override
	public String execute() throws IOException {
		if (!isValidDisplayedId(taskId)) {
			return MESSAGE_ERROR_WRONG_TASK_ID;
		}

		if (isDateBeforeNow(datePairs)) {
			return MESSAGE_UPDATE_PAST;
		}

		long databaseId = displayedTasksList.get(taskId - 1);

		Task task = dbManager.getInstance(databaseId);
		String oldDescription = task.getDescription();

		if (!description.isEmpty()) {
			task.setDescription(description);
		}

		if (!datePairs.isEmpty()) {
			if (task.isFloatingTask() || task.isDeadline()) {
				task.setDateList(datePairs);
				if (!task.isFloatingTask() && !task.isDeadline()) {
					task.generateUuid();
				}
			} else {
				task.setDateList(datePairs);
				if (task.isFloatingTask() || task.isDeadline()) {
					task.generateUuid();
				}
			}
		}

		long newDatabaseId = dbManager.putInstance(task);
		dbManager.markAsInvalid(databaseId);

		displayedTasksList.set(taskId - 1, newDatabaseId);
		dbManager.recordAction(databaseId, newDatabaseId,
				String.format(JOURNAL_MESSAGE_UPDATE, oldDescription));

		return String.format(MESSAGE_UPDATE, oldDescription);
	}
}
