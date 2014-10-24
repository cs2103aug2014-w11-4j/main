import java.io.IOException;


public class SearchCommand extends Command {
	private static final String MESSAGE_SEARCH_RESULT = "%s task with \"%s\" has been found.";

	/**
	 *
	 * @param keyword that is used to search for the task
	 */
	public SearchCommand(String keyword) {
		this.type = CommandType.SEARCH;
		this.keyword = keyword;
	}
	
	/**
     * Search for task based on description.
     *
     * 
     */
	@Override
	public String execute() throws IOException {
		StringBuilder responseBuilder = new StringBuilder();

		displayedTasksList.clear();
		for (Long databaseId : dbManager.getValidIdList()) {
			String taskInDb = dbManager.getInstance(databaseId)
					.getDescription();
			taskInDb = taskInDb.toLowerCase();
			if (taskInDb.contains(keyword.toLowerCase())) {
				displayedTasksList.add(databaseId);
			}
		}

		responseBuilder.append(String.format(MESSAGE_SEARCH_RESULT,
				displayedTasksList.size(), keyword));

		if (!displayedTasksList.isEmpty()) {
			responseBuilder.append(System.lineSeparator());
			responseBuilder.append(formatTaskListOutput());
		}

		return responseBuilder.toString();

	}

}
