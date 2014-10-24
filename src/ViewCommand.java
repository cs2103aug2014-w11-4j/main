import java.io.IOException;
import java.text.SimpleDateFormat;

public class ViewCommand extends Command {
	private static final String MESSAGE_VIEWALL_RESULT = "You have %s uncompleted task(s).";
	private static final String MESSAGE_VIEWDATE_RESULT = "You have %s uncompleted task(s) %s.";
	private static final String MESSAGE_VIEWALL_CRESULT = "You have %s completed task(s).";
	private static final String MESSAGE_VIEWDATE_CRESULT = "You have %s completed task(s) %s.";

	/**
	 *
	 * @param viewAll
	 * @param completed
	 * @param viewRange
	 */
	public ViewCommand(boolean viewAll, boolean completed,
			DatePair viewRange) {
		this.type = CommandType.VIEW;
		this.viewAll = viewAll;
		this.viewRange = viewRange;
		this.completed = completed;
	}
	
	/**
     * Check the type of view requested by Command
     *
     * @return the result of the view option
     *
     * @throws IOException
     *
     */
	@Override
	public String execute() throws IOException {
		if (isViewAll()) {
			return viewAll(isCompleted());
		} else {
			return viewByPeriod(getViewRange(), isCompleted());
		}

	}

	/**
	 * Return all the valid task stored in the database
	 * 
	 * @return list of tasks and their information in the database
	 */
	public String viewAll(boolean isCompleted) throws IOException {
		StringBuilder responseBuilder = new StringBuilder();
		displayedTasksList.clear();
		for (int i = 0; i < dbManager.getValidIdList().size(); i++) {
			Long databaseId = dbManager.getValidIdList().get(i);
			Task task = dbManager.getInstance(databaseId);
			if (isCompleted == task.getIsDone()) {
				displayedTasksList.add(databaseId);
			}
		}

		if (isCompleted) {
			responseBuilder.append(String.format(MESSAGE_VIEWALL_CRESULT,
					displayedTasksList.size()));
		} else {
			responseBuilder.append(String.format(MESSAGE_VIEWALL_RESULT,
					displayedTasksList.size()));
		}

		if (!displayedTasksList.isEmpty()) {
			responseBuilder.append(System.lineSeparator());
			responseBuilder.append(formatTaskListOutput());
		}

		return responseBuilder.toString();
	}

	/**
	 * Searches the Database for a related task that coincides with the
	 * dateRange requested.
	 * 
	 * @param dateRange
	 *            DatePair object containing the start date and end date
	 * @return result of all the tasks that are within the period as queried
	 */

	public String viewByPeriod(DatePair dateRange, boolean isCompleted)
			throws IOException {
		StringBuilder responseBuilder = new StringBuilder();
		displayedTasksList.clear();
		for (Long databaseId : dbManager.getValidIdList()) {
			Task task = dbManager.getInstance(databaseId);
			if (isCompleted == task.getIsDone() && task.hasDate()) {
				if (task.isWithinPeriod(dateRange)) {
					displayedTasksList.add(databaseId);
				}
			}
		}

		String range = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
		if (dateRange.hasDateRange()) {
			range = "from "
					+ dateFormat.format(dateRange.getStartDate().getTime())
					+ " to "
					+ dateFormat.format(dateRange.getEndDate().getTime());
		} else if (dateRange.hasEndDate()) {
			range = "on " + dateFormat.format(dateRange.getEndDate().getTime());
		} else {
			assert false : "This should not occur as there must be a date.";
		}

		if (isCompleted) {
			responseBuilder.append(String.format(MESSAGE_VIEWDATE_CRESULT,
					displayedTasksList.size(), range));
		} else {
			responseBuilder.append(String.format(MESSAGE_VIEWDATE_RESULT,
					displayedTasksList.size(), range));
		}

		if (!displayedTasksList.isEmpty()) {
			responseBuilder.append(System.lineSeparator());
			responseBuilder.append(formatTaskListOutput());
		}

		return responseBuilder.toString();
	}

}
