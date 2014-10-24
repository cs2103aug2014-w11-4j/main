import java.io.IOException;


public class SearchCommand extends Command {
	private static final String MESSAGE_SEARCH_RESULT = "%s task with \"%s\" has been found.";

	public SearchCommand(CommandType type, String desc) {
		this.type = type;
		if (type == CommandType.SEARCH)
			this.keyword = desc;
		else if (type == CommandType.INVALID)
			this.description = desc;
	}

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
