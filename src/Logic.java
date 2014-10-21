import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The logic center of the task manager which handles all the logic behind
 * functionalities.
 *
 * Interacts with UI and DatabaseManager.
 *
 */
public class Logic {
    private static final String JOURNAL_MESSAGE_UNDONE = "Undone operation \"%s\".";
    private static final String JOURNAL_MESSAGE_REDONE = "Redone operation \"%s\".";
    private static final String JOURNAL_MESSAGE_ADD = "Added task \"%s\"";
    private static final String JOURNAL_MESSAGE_MARK_AS_COMPLETED = "Mark task \"%s\" as completed";
    private static final String JOURNAL_MESSAGE_MARK_AS_UNCOMPLETED = "Mark task \"%s\" as uncompleted";
    private static final String JOURNAL_MESSAGE_UPDATE = "Updated task \"%s\"";
    private static final String JOURNAL_MESSAGE_DELETE = "Deleted task \"%s\"";
    private static final String JOURNAL_MESSAGE_CONFIRM = "Confirm task \"%s\"";

    private static final String MESSAGE_ADD = "\"%s\" has been successfully added.";
    private static final String MESSAGE_ADD_CONFLICT = "\"%s\" has been successfully added.\nPlease note that there are conflicting task";
    private static final String MESSAGE_ADD_PAST = "\"%s\" cannot be added, as it has passed.";
    private static final String MESSAGE_DELETE = "\"%s\" has been successfully deleted.";
    private static final String MESSAGE_UPDATE = "\"%s\" has been successfully updated.";
    private static final String MESSAGE_MARK_COMPLETED = "\"%s\" has been marked to completed.";
    private static final String MESSAGE_MARK_UNCOMPLETED = "\"%s\" has been marked to uncompleted.";
    private static final String MESSAGE_SEARCH_RESULT = "%s task with \"%s\" has been found.";
    private static final String MESSAGE_CONFIRM = "\"%s\" has been confirmed.";

    private static final String MESSAGE_VIEWALL_RESULT = "You have %s uncompleted task(s).";
    private static final String MESSAGE_VIEWDATE_RESULT = "You have %s uncompleted task(s) %s.";
    private static final String MESSAGE_VIEWALL_CRESULT = "You have %s completed task(s).";
    private static final String MESSAGE_VIEWDATE_CRESULT = "You have %s completed task(s) %s.";

    private static final String MESSAGE_ERROR_DATABASE_IOEXCEPTION = "Exception has occured when accessing local storage.";
    private static final String MESSAGE_ERROR_WRONG_TASK_ID = "You have input an invalid ID.";
    private static final String MESSAGE_ERROR_WRONG_DATE_ID = "You have input an invalid date ID.";


    private static final String MESSAGE_ERROR_NOT_TENTATIVE = "\"%s\" is not tentative and does not need confirmation.";


    private static final int ADD_OK = 0;
    private static final int ADD_CONFLICT = 1;
    private static final int ADD_PASSED = 2;

    private static final int CONSOLE_MAX_WIDTH = 80;

    private static final Logger logger = Logger
            .getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String DATABASE_NAME = "database.xml";
    private static final String CURRENT_DIRECTORY = System
            .getProperty("user.dir");

    private static Logic logicInstance;

    private ArrayList<Long> displayedTasksList;
    private DatabaseManager<Task> dbManager;

    /**
     * Private Constructor for Singleton Implementation.
     */
    private Logic() {
        displayedTasksList = new ArrayList<Long>();
        startDatabase();
    }

    /**
     * Method that retrieves the singleton instance of the Logic
     *
     * @return instance of Parser
     */
    public static Logic getInstance() {
        if (logicInstance == null) {
            logicInstance = new Logic();
        }

        return logicInstance;
    }

    /**
     * Start the database, if not found new database will be created.
     *
     * @return states if the database has been started successfully
     */
    public boolean startDatabase() {
        try {
            dbManager = new DatabaseManager<Task>(CURRENT_DIRECTORY
                    + File.separator + DATABASE_NAME);
        } catch (IOException e) {
            logger.log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return false;
        }
        return true;
    }

    /**
     * Executes command provided by user in the interface.
     *
     * @param command the command object that holds instruction from user
     * @return result of the command instruction
     */
    public String executeCommand(Command command) {
        try {
            logger.info("Executing command: " + command.getType().toString());
            switch (command.getType()) {
                case ADD:
                    int result = addTask(command.getDescription(),
                            command.getDatePairs());
                    if (result == ADD_CONFLICT) {
                        return String.format(MESSAGE_ADD_CONFLICT,
                                command.getDescription());
                    } else if (result == ADD_OK) {
                        return String.format(MESSAGE_ADD,
                                command.getDescription());
                    } else if (result == ADD_PASSED) {
                        return String.format(MESSAGE_ADD_PAST,
                                command.getDescription());
                    }

                case VIEW:
                    if (command.isViewAll()) {
                        return viewAll(command.isCompleted());
                    } else {
                        return viewByPeriod(command.getViewRange(),
                                command.isCompleted());
                    }

                case SEARCH:
                    return searchWithKeyword(command.getKeyword());

                case MARK:
                    return markTask(command.getTaskId());

                case DELETE:
                    return deleteTask(command.getTaskId());

                case UPDATE:
                    return updateTask(command.getTaskId(),
                            command.getDescription(), command.getDatePairs());

                case UNDO:
                    return undo();

                case REDO:
                    return redo();

                case CONFIRM:
                    return confirmTask(command.getTaskId(), command.getDateId());

                case INVALID:
                    return command.getDescription();

                case HELP:
                    return showHelp();

                case CLEAR:
                    return clearScreen();

                case EXIT:
                    dbManager.closeFile();
                    System.exit(0);

                default: /* should never reach default */
                    throw new AssertionError(command.getType());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return MESSAGE_ERROR_DATABASE_IOEXCEPTION;
        }
    }

    /**
     * Create and add the task to the database.
     *
     * @param description of the task
     * @param dateList of possible DatePair
     * @return status of adding ADD_OK:successful ADD_CONFLICT:exist conflicts ADD_PASSED:cannot be beyond today
     *
     * @throws IOException
     */
    public int addTask(String description, ArrayList<DatePair> dateList)
            throws IOException {

        assert dateList != null;
        assert dateList.size() >= 0;

        if (dateList.size() > 0
                && dateList.get(0).getEndDate().before(Calendar.getInstance())) {
            return ADD_PASSED;
        }

        assert description != null;
        assert description != "";

        Task task = new Task(description, dateList);
        
        assert task != null;
        boolean hasConflict = checkConflictWithDB(task);
        long id = dbManager.putInstance(task);
        dbManager.recordAction(null, id,
                String.format(JOURNAL_MESSAGE_ADD, task.getDescription()));
        assert id >= 0;

        if (hasConflict) {
            return ADD_CONFLICT;
        } else {
            return ADD_OK;
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
     * Mark a task (completed to uncompleted and vice versa)
     * @param displayedId displayed id of the task
     * @return message of mark
     * @throws IOException
     */
    public String markTask(int displayedId) throws IOException {
        if (!isValidDisplayedId(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        if (isCompletedTask(displayedId)) {
            return markTaskUncompleted(displayedId);
        } else {
            return markTaskCompleted(displayedId);
        }
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
        return String
                .format(MESSAGE_MARK_UNCOMPLETED, oldTask.getDescription());
    }

    /**
     * Update the task to the database.
     *
     * @param displayedId id of the task as displayed in the last view command
     * @param description updated description, if not changed will be null
     * @param dateList updated date list, if not changed will be null
     * @return updated message with the displayed id
     */
    public String updateTask(int displayedId, String description,
            ArrayList<DatePair> dateList) throws IOException {
        if (!isValidDisplayedId(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        long databaseId = displayedTasksList.get(displayedId - 1);

        Task task = dbManager.getInstance(databaseId);
        String oldDescription = task.getDescription();

        if (!description.isEmpty()) {
            task.setDescription(description);
        }
        if (!dateList.isEmpty()) {
            if (task.isFloatingTask() || task.isDeadline()) {
                task.setDateList(dateList);
                if (!task.isFloatingTask() && !task.isDeadline()) {
                    task.generateUuid();
                }
            } else {
                task.setDateList(dateList);
                if (task.isFloatingTask() || task.isDeadline()) {
                    task.generateUuid();
                }
            }
        }

        long newDatabaseId = dbManager.putInstance(task);
        dbManager.markAsInvalid(databaseId);

        displayedTasksList.set(displayedId - 1, newDatabaseId);
        dbManager.recordAction(databaseId, newDatabaseId,
                String.format(JOURNAL_MESSAGE_UPDATE, oldDescription));

        return String.format(MESSAGE_UPDATE, oldDescription);
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
     * Delete Task of Database.
     *
     * @param displayedId displayed id of the task
     * @return delete message including the task description
     *
     * @throws IOException
     */
    public String deleteTask(int displayedId) throws IOException {
        if (!isValidDisplayedId(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        long databaseId = displayedTasksList.get(displayedId - 1);
        Task oldTask = dbManager.getInstance(databaseId);
        String oldDescription = oldTask.getDescription();
        dbManager.markAsInvalid(databaseId);
        displayedTasksList.set(displayedId - 1, (long) -1);
        dbManager.recordAction(databaseId, null,
                String.format(JOURNAL_MESSAGE_DELETE, oldDescription));
        return String.format(MESSAGE_DELETE, oldDescription);
    }

    /**
     * Search for task based on description.
     *
     * @param keyword the keyword that is used to search for the task
     */

    public String searchWithKeyword(String keyword) throws IOException {
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

    /**
     * Searches the Database for a related task that coincides with the
     * dateRange requested.
     *
     * @param dateRange DatePair object containing the start date and end date
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

    /**
     * Confirm the date of task to the database.
     *
     * @param displayedId id of the task as displayed in the last view command
     * @param date id to be confirmed
     * @return confirm message with the displayed id
     *
     */
    public String confirmTask(int displayedId, int dateId) throws IOException {
        if (!isValidDisplayedId(displayedId)) {
            return MESSAGE_ERROR_WRONG_TASK_ID;
        }
        long databaseId = displayedTasksList.get(displayedId - 1);

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

        displayedTasksList.set(displayedId - 1, newDatabaseId);
        dbManager.recordAction(databaseId, newDatabaseId,
                String.format(JOURNAL_MESSAGE_CONFIRM, oldDescription));

        return String.format(MESSAGE_CONFIRM, oldDescription);
    }

    /**
     * Method that undo previous action in the journal.
     *
     */
    public String undo() throws IOException {
        try {
            return String.format(JOURNAL_MESSAGE_UNDONE, dbManager.undo());
        } catch (UnsupportedOperationException e) { // Nothing to undo
            return e.getMessage();
        }
    }

    /**
     * Method that redo previous (undone) action in the journal.
     *
     */
    public String redo() throws IOException {
        try {
            return String.format(JOURNAL_MESSAGE_REDONE, dbManager.redo());
        } catch (UnsupportedOperationException e) { // Nothing to redo
            return e.getMessage();
        }
    }

    /**
     * Helper method that formats the output of tasks.
     *
     * @param displayingId the id of the task
     * @return the formatted output of the task
     *
     * @throws IOException
     */
    private String formatTaskOutput(int displayingId) throws IOException {
        Task task = dbManager.getInstance(displayedTasksList.get(displayingId));
        return task.formatOutput(displayingId + 1);
    }

    private String formatTaskListOutput() throws IOException {
        Collections.sort(displayedTasksList, dbManager.getInstanceComparator());

        StringBuilder stringBuilder = new StringBuilder();
        String header = String.format("%-7s%-6s%-43s%-24s", "ID", "Done",
                "Task", "Date");
        String border = "";
        for (int i = 0; i < CONSOLE_MAX_WIDTH; i++) {
            border += "-";
        }

        stringBuilder.append(border + System.lineSeparator() + header
                + System.lineSeparator() + border + System.lineSeparator());

        for (int i = 0; i < displayedTasksList.size(); i++) {
            stringBuilder.append(formatTaskOutput(i));
            stringBuilder.append(System.lineSeparator());
        }
        stringBuilder.append(border);

        return stringBuilder.toString();
    }

    private boolean isValidDisplayedId(int displayedId) {
        return !(displayedId > displayedTasksList.size() || displayedId <= 0 || displayedTasksList
                .get(displayedId - 1) == -1);
    }

    /**
     * Shows the available command for the end user in the system. TODO: May
     * need refactoring as currently it is hardcoded.
     *
     * @return a String object containing all the commands available
     */
    private String showHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are for the available commands in RubberDuck.");
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "view",
                "View your agenda given a date range or \"all\"."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "search",
                "Search for tasks related to the given keyword."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "add",
                "Add a new task of provided description with optional date."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "delete",
                "Delete a task from the system given task ID."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "update",
                "Update task given task ID and new information."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "undo",
                "Undo your previous action."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "redo",
                "Redo your undone action."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "mark",
                "Mark any task to complete/incomplete given task ID."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "confirm",
                "Confirm any tentative task given task ID and date ID."));
        sb.append(System.lineSeparator());
        sb.append(String.format("%-15s%-65s", "exit", "Exit from RubberDuck."));
        return sb.toString();
    }

    /**
     * Clear the screen of the current interface.
     *
     * @throws IOException
     */
    private String clearScreen() throws IOException {
        final String os = System.getProperty("os.name");

        if (os.contains("Windows")) {
            Runtime.getRuntime().exec("cls");
        } else {
            Runtime.getRuntime().exec("clear");
        }

        return "";
    }

    /**
     * Non-official methods added quickly to assist testing.
     *
     * @return details of the task requested
     *
     * @throws IOException
     */
    public String viewTask(long id) throws IOException {
        return dbManager.getInstance(id).toString();
    }

    public DatabaseManager<Task> getDB() {
        return dbManager;
    }

    public boolean checkConflictWithDB(Task t) throws IOException {
        boolean isConflict = false;
        ArrayList<Long> validIDList = dbManager.getValidIdList();
        for (int i = 0; i < validIDList.size(); i++) {
            Task storedTask = dbManager.getInstance(validIDList.get(i));
            ArrayList<DatePair> dp = storedTask.getDateList();
            for (int j = 0; j < dp.size(); j++) {
                if (t.isWithinPeriod(dp.get(j))) {
                    isConflict = true;
                }
            }
        }

        return isConflict;
    }

}
