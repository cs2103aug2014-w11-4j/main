import java.io.IOException;
import java.util.ArrayList;


public class ConfirmCommand extends Command{
	private static final String JOURNAL_MESSAGE_CONFIRM = "Confirm task \"%s\"";
	private static final String MESSAGE_CONFIRM = "\"%s\" has been confirmed.";
	private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";
	private static final String MESSAGE_ERROR_NOT_TENTATIVE = "\"%s\" is not tentative and does not need confirmation.";
	private static final String MESSAGE_ERROR_WRONG_DATE_ID = "You have input an invalid date ID.";

    /* Information required for confirm */
    private int dateId;

    public int getDateId() {
        return dateId;
    }

	/**
	 * @param taskId id of the task as displayed in the last view command
     * @param dateId id to be confirmed
	 */
    public ConfirmCommand(int taskId, int dateId) {
        this.type = CommandType.CONFIRM;
        this.taskId = taskId;
        this.dateId = dateId;
    }

    /**
     * Confirm the date of task to the database.
     *
     * @return confirm message with the displayed id
     */
	@Override
	public String execute() throws IOException {
		 if (!isValidDisplayedId(taskId)) {
	            return MESSAGE_ERROR_WRONG_TASK_ID;
	        }
	        long databaseId = displayedTasksList.get(taskId - 1);

	        Task task = dbManager.getInstance(databaseId);
	        String oldDescription = task.getDescription();

	        ArrayList<DatePair> dateList = task.getDateList();

	        if (dateList.size() <= 1) {
	            return String.format(MESSAGE_ERROR_NOT_TENTATIVE, oldDescription);
	        }

	        if (dateList.size() < dateId) {
	            return MESSAGE_ERROR_WRONG_DATE_ID;
	        }

	        DatePair date = dateList.get(dateId - 1);
	        ArrayList<DatePair> newDateList = new ArrayList<DatePair>();
	        newDateList.add(date);
	        task.setDateList(newDateList);

	        long newDatabaseId = dbManager.putInstance(task);
	        dbManager.markAsInvalid(databaseId);

	        displayedTasksList.set(taskId - 1, newDatabaseId);
	        dbManager.recordAction(databaseId, newDatabaseId,
	                String.format(JOURNAL_MESSAGE_CONFIRM, oldDescription));

	        return String.format(MESSAGE_CONFIRM, oldDescription);

	}
}
