package rubberduck.logic.command;

import java.io.IOException;
import java.util.StringTokenizer;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.common.formatter.Formatter;

//@author A0111794E
/**
 * Concrete Command Class that can be executed to search the data store for
 * tasks containing the provided keyword and returns back the task details.
 */
public class SearchCommand extends Command {

    private static final String MESSAGE_SEARCH_RESULT =
        "%s task with \"%s\" has been found.";

    private static final int FIRST_CHAR = 0;
    private static final int SECOND_CHAR = 1;
    private static final int CHAR_LENGTH_OFFSET = 1;
    private static final int EMPTY_KEYWORDS_LENGTH = 2;
    private static final int ONE_WORD = 1;

    /* Information required for search */
    private String keyword;

    /**
     * Public constructor of SearchCommand.
     *
     * @param keyword that is used to search for the task
     */
    public SearchCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Getter method for keyword.
     *
     * @return String object
     */
    protected String getKeyword() {
        return keyword;
    }

    /**
     * Search for task based on description and return a Response containing
     * formatted string of tasks back to parent.
     *
     * @return Response object containing formatted tasks
     * @throws IOException that might be thrown from dbManager
     */
    @Override
    public Response execute() throws IOException {
        setPreviousDisplayCommand(this);
        getDisplayedTasksList().clear();

        for (Long databaseId : getDbManager().getValidIdList()) {
            boolean isFound = false;
            String taskDescription =
                getDbManager().getInstance(databaseId).getDescription();
            taskDescription = taskDescription.toLowerCase();
            StringTokenizer taskDescriptions =
                new StringTokenizer(taskDescription);
            StringTokenizer keywords =
                new StringTokenizer(keyword.toLowerCase());

            if (keyword.length() > EMPTY_KEYWORDS_LENGTH &&
                keyword.charAt(FIRST_CHAR) == '\"' &&
                keyword.charAt(keyword.length() - CHAR_LENGTH_OFFSET) == '\"') {

                String modifiedKeyword = keyword.substring(SECOND_CHAR,
                                                           keyword.length() -
                                                           CHAR_LENGTH_OFFSET);
                isFound = searchExactKeyword(modifiedKeyword, taskDescriptions);

            } else if (keywords.countTokens() == ONE_WORD) {
                isFound = searchSingleKeyword(keyword, taskDescription);
            } else {
                isFound = searchMultipleKeyword(keywords, taskDescriptions);
            }
            if (isFound) {
                getDisplayedTasksList().add(databaseId);
            }
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.RED
                                                              : Color.GREEN;

        StringBuilder viewCount = new StringBuilder();
        viewCount.append(ColorFormatter.format(
            String.format(MESSAGE_SEARCH_RESULT, getDisplayedTasksList().size(),
                          keyword), headerColor));

        String taskData = Formatter.formatTaskList(getDisplayedTasksList(),
                                                   getDbManager());
        return new Response("", viewCount.toString(), taskData);
    }

    /**
     * Complementing searchMultipleKeyword.
     * <p/>
     * When search are being called, if keyword used in search contains only a
     * single word, this method will be called.
     * <p/>
     * This will actually check if the description itself contains the word and
     * return the value immediately
     * <p/>
     * To eliminate the getting unwanted result due to searching with
     * meaningless keywords
     *
     * @param keyword
     * @param taskDescription
     * @return if the description of the task contains the keyword.
     */
    private boolean searchSingleKeyword(String keyword,
                                        String taskDescription) {
        return taskDescription.toLowerCase().contains(keyword.toLowerCase());
    }

    /**
     * This method allows user to search for exact keyword, thus if " " are
     * used, only description that consist of exact same words will be shown.
     *
     * @param keyword
     * @param taskDescriptions tokenized taskDescription
     * @return if the description of the task contains the keyword.
     */
    private boolean searchExactKeyword(String keyword,
                                       StringTokenizer taskDescriptions) {

        while (taskDescriptions.hasMoreElements()) {

            String nextToken = taskDescriptions.nextToken();
            if (nextToken.equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Complementing searchSingleKeyword.
     * <p/>
     * When search are being called, if keyword used in search contains more
     * than a word, this method will be called.
     * <p/>
     * This will actually check if the description itself contains exactly all
     * the keyword as entered by the user
     * <p/>
     * To eliminate the getting unwanted result due to searching with
     * meaningless keywords
     *
     * @param keyword
     * @param taskDescription
     * @return if the description of the task contains the keyword.
     */
    private boolean searchMultipleKeyword(StringTokenizer keywords,
                                          StringTokenizer taskDescriptions) {
        String firstKeyword = keywords.nextToken();
        while (taskDescriptions.hasMoreElements()) {
            if (taskDescriptions.nextToken().equals(firstKeyword)) {
                if (keywords.countTokens() <= taskDescriptions.countTokens()) {
                    // check remaining keyword
                    while (keywords.hasMoreElements()) {
                        if (!keywords.nextToken()
                            .equals(taskDescriptions.nextToken())) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }
}
