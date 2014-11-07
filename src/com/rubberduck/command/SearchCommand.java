package com.rubberduck.command;

import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Formatter;
import com.rubberduck.menu.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.StringTokenizer;

/**
 * Concrete Command Class that can be executed to search the data store for
 * tasks containing the provided keyword and returns back the task details.
 */
//@author A0111794E
public class SearchCommand extends Command {

    private static final String MESSAGE_SEARCH_RESULT =
        "%s task with \"%s\" has been found.";
    private static final String SCHEDULE_SEPERATOR =
        "--------------------------------[  SCHEDULES  ]---------------------------------";
    private static final String FLOATING_SEPERATOR =
        "--------------------------------[    TASKS    ]---------------------------------";
    private static final String DEADLINE_SEPERATOR =
        "--------------------------------[  DEADLINES  ]---------------------------------";

    private static final int FLOATING_TASK = 0;
    private static final int DEADLINE_TASK = 1;
    private static final int TIMED_TASK = 2;

    /* Information required for search */
    private String keyword;

    /**
     * Getter method for keyword.
     *
     * @return String object
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Public constructor of SearchCommand.
     *
     * @param keyword that is used to search for the task
     */
    public SearchCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Search for task based on description and return a Response containing
     * formatted string of tasks back to parent.
     *
     * @return Response object containing formatted tasks
     */
    @Override
    public Response execute() throws IOException {
        setPreviousDisplayCommand(this);
        getDisplayedTasksList().clear();

        for (Long databaseId : getDbManager().getValidIdList()) {
            boolean isFound=false;
            String taskDescription =
                getDbManager().getInstance(databaseId).getDescription();
            taskDescription = taskDescription.toLowerCase();                       
            StringTokenizer taskDescriptions = new StringTokenizer(taskDescription);
            StringTokenizer keywords = new StringTokenizer(keyword.toLowerCase());
            
            if(keyword.charAt(0) == '\"' && keyword.charAt(keyword.length()-1) =='\"'){
                keyword = keyword.substring(1,keyword.length()-1);                
                isFound = searchExactKeyword(keyword, taskDescriptions);
            }else if(keywords.countTokens()==1){
                isFound = searchSingleKeyword(keyword, taskDescription);
            }else{
                isFound = searchMultipleKeyword(keywords, taskDescriptions);                
            }
            if(isFound){
                getDisplayedTasksList().add(databaseId);
            }
            
                     
        }

        Color headerColor = getDisplayedTasksList().isEmpty() ? Color.RED
                                                              : Color.GREEN;

        StringBuilder viewCount = new StringBuilder();
        viewCount.append(ColorFormatter.format(
            String.format(MESSAGE_SEARCH_RESULT, getDisplayedTasksList().size(),
                          keyword), headerColor));

        return new Response("", viewCount.toString(), formatTaskListOutput());
    }
    
    /**
     * Complementing searchMultipleKeyword.
     * <p>When search are being called, if keyword used in search contains only a single word, this method will be called.</p>
     * <p>This will actually check if the description itself contains the word and return the value immediately</p>
     * <p>To eliminate the getting unwanted result due to searching with meaningless keywords</p>
     * @param keyword
     * @param taskDescription
     * @return if the description of the task contains the keyword.
     */
    private boolean searchSingleKeyword(String keyword, String taskDescription){
        if(taskDescription.toLowerCase().contains(keyword.toLowerCase())){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * This method allows user to search for exact keyword, thus if " " are used,
     * only description that consist of exact same words will be shown.
     * 
     * @param keyword 
     * @param taskDescriptions tokenized taskDescription
     * @return if the description of the task contains the keyword.
     */
    private boolean searchExactKeyword(String keyword,StringTokenizer taskDescriptions) {
        
        while (taskDescriptions.hasMoreElements()) {
            if (taskDescriptions.nextToken().equalsIgnoreCase(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Complementing searchSingleKeyword.
     * <p>When search are being called, if keyword used in search contains more than a word, this method will be called.</p>
     * <p>This will actually check if the description itself contains exactly all the keyword as entered by the user</p>
     * <p>To eliminate the getting unwanted result due to searching with meaningless keywords</p>
     * @param keyword
     * @param taskDescription
     * @return if the description of the task contains the keyword.
     */
    private boolean searchMultipleKeyword(StringTokenizer keywords,StringTokenizer taskDescriptions){        
        String firstKeyword = keywords.nextToken();
        while (taskDescriptions.hasMoreElements()) {
            if (taskDescriptions.nextToken().equals(firstKeyword)) {
                if (keywords.countTokens() <= taskDescriptions.countTokens()) {
                    // check remaining keyword
                    boolean stillValid = true;
                    while (keywords.hasMoreElements() && stillValid == true) {
                        if(!keywords.nextToken().equals(taskDescriptions.nextToken())){
                            stillValid = false;
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


    /**
     * Format the list of tasks into a String output and return.
     *
     * @return the formatted string of all tasks involved
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0111736M
    private String formatTaskListOutput() throws IOException {
        Collections.sort(getDisplayedTasksList(),
                         getDbManager().getInstanceIdComparator());
        StringBuilder taskData = new StringBuilder();

        int prevType = -1;
        for (int i = 0; i < getDisplayedTasksList().size(); i++) {
            if (taskData.length() > 0) {
                taskData.append(System.lineSeparator());
            }

            int currentType = getTaskType(i);
            if (currentType != prevType) {
                if (currentType == FLOATING_TASK) {
                    taskData.append(FLOATING_SEPERATOR);
                } else if (currentType == DEADLINE_TASK) {
                    taskData.append(DEADLINE_SEPERATOR);
                } else if (currentType == TIMED_TASK) {
                    taskData.append(SCHEDULE_SEPERATOR);
                }
                taskData.append(System.lineSeparator());
            }
            prevType = currentType;
            taskData.append(formatTaskOutput(i));
        }
        return taskData.toString();
    }

    /**
     * Helper method that formats the output of an individual task.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    private String formatTaskOutput(int displayingId) throws IOException {
        Task task = getDbManager().
            getInstance(getDisplayedTasksList().get(displayingId));
        return Formatter.formatTask(task, displayingId + 1 + "");
    }

    /**
     * Retrieve the task type from the database given the ID displayed.
     *
     * @param displayingId the id of the task
     * @return enum which specifies what type of task it is
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    //@author A0111794E
    private int getTaskType(int displayingId) throws IOException {
        Task t = getDbManager().
            getInstance(getDisplayedTasksList().get(displayingId));
        if (t.isFloatingTask()) {
            return FLOATING_TASK;
        } else if (t.isDeadline()) {
            return DEADLINE_TASK;
        } else {
            return TIMED_TASK;
        }
    }
}
