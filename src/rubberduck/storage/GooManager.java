package rubberduck.storage;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Tasks;
import rubberduck.common.datatransfer.DatePair;

//@author A0119416H

/**
 * GooManager is a component that is responsible for all interaction with Google Calendar / Google Tasks.
 */
public class GooManager {

    /**
     * A dummy Exception for differentiating IOException happened due to network error and disk error.
     */
    public static class NetworkException extends IOException {
        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static boolean initialized = false;

    /**
     * Constants needed for Google API Client
     */
    private static final String CLIENT_ID = "849841048712-0t9rn1vi1nch19cqsuaaaj19oo7c7pl3.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "Zq0v8OByEqQfPMXZis8Iw86D";
    private static final String APPLICATION_NAME = "RubberDuck/0.5";
    private static final String CALENDAR_NAME = "RubberDuck";

    /**
     * To mark completed timed tasks remotely as Google Calendar does not have such function.
     */
    private static final String REMOTE_FLAG_COMPLETED = "[Completed]\n";

    /**
     * To be used to set unnamed tasks as both Google Calendar and Tasks support items with empty title.
     */
    private static final String LOCAL_FLAG_UNNAMED_TASK = "[Unnamed]";

    /**
     * Flags to store UUID locally after being pushed to the server.
     */
    private static final String LOCAL_UUID_PREFIX_TASK = "_RD_T_";
    private static final String LOCAL_UUID_PREFIX_EVENT = "_RD_E_";

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);
    private static HttpTransport httpTransport;

    /**
     * DataStore needed for persisting OAuth Token.
     */
    private static FileDataStoreFactory fileDataStoreFactory;
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    private static final String DATA_STORE_DIR = "/data/sync/";
    private static final String DATA_STORE_NAME = "StoredCredential";

    /**
     * Keep a record of when the last sync happened.
     */
    //private static final String REMOTE_SYNC_FLAG_FORMAT = "Last Synced on: ";
    private static Date lastSyncTime;
    private static final String LAST_SYNC_FILE_NAME = "LastSync";

    private static com.google.api.services.calendar.Calendar calendarClient;
    private static com.google.api.services.tasks.Tasks tasksClient;

    /**
     * Logger and information strings.
     */
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static final String LOG_MESSAGE_INITIALIZATION = "Google API Initialization finished.";
    private static final String LOG_MESSAGE_INITIALIZED = "Already initialized, skipping...";
    private static final String LOG_MESSAGE_CREATING_CALENDAR = "Remote calendar not found, creating...";
    private static final String LOG_MESSAGE_CREATING_TASKLIST = "Remote task list not found, creating...";
    private static final String LOG_MESSAGE_CALENDAR_ID = "Found remote calendar, ID is %s";
    private static final String LOG_MESSAGE_TASKLIST_ID = "Found remote task list, ID is %s";
    private static final String LOG_MESSAGE_PUSHING = "Starting pushing...";
    private static final String LOG_MESSAGE_PULLING = "Starting pulling...";
    private static final String LOG_MESSAGE_TWO_WAY = "Starting two-way syncing...";

    private static String calendarId = null;
    private static String taskListId = null;

    /**
     * As Google Tasks does not support set due date to some specific time, this information will be stored in its notes.
     */
    private static final String REMOTE_TASK_TIME_FORMAT = "Due: ";

    /**
     * Color IDs defined by Google Calendar to differentiate completed and uncompleted tasks.
     */
    private static final String COLOR_ID_COMPLETED = "2";
    private static final String COLOR_ID_UNCOMPLETED = "4";

    /**
     * Authorize the application to access Google Accounts.
     *
     * @return Credential required by Google API Clients
     * @throws NetworkException if network failure happens
     * @throws IOException if I/O error happens when reading/writing oauth2 tokens
     */
    private static Credential authorize() throws IOException {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(CLIENT_ID);
        details.setClientSecret(CLIENT_SECRET);
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        ArrayList<String> scopes = new ArrayList<String>();
        scopes.add(CalendarScopes.CALENDAR);
        scopes.add(TasksScopes.TASKS);

        fileDataStoreFactory = new FileDataStoreFactory(new File(CURRENT_DIRECTORY + DATA_STORE_DIR));

        try {
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, scopes).
                            setDataStoreFactory(fileDataStoreFactory).build();

            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Set up Google API Clients
     *
     * @throws NetworkException         if network failure happens
     * @throws GeneralSecurityException if server cannot be trusted (possible MITM)
     * @throws IOException              if I/O error happens when reading/writing oauth2 tokens
     */
    private static void setupConnection() throws IOException, GeneralSecurityException {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }

        Credential credential = authorize();

        calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();

        tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();

        LOGGER.info(LOG_MESSAGE_INITIALIZATION);
    }

    /**
     * Initialize GooManager to authorize and get information about Calendar and TaskList RubberDuck should write to.
     *
     * @throws NetworkException         if network failure happens
     * @throws GeneralSecurityException if server cannot be trusted (possible MITM)
     * @throws IOException              if I/O error happens when reading/writing oauth2 tokens
     */
    public static void initialize() throws IOException, GeneralSecurityException {
        if (initialized) {
            LOGGER.info(LOG_MESSAGE_INITIALIZED);
        } else {
            setupConnection();

            try {
                String pageToken = null;
                do {
                    CalendarList calendarList = calendarClient.calendarList().list().setPageToken(pageToken).execute();
                    List<CalendarListEntry> items = calendarList.getItems();
                    for (CalendarListEntry calendarListEntry : items) {
                        if (calendarListEntry.getSummary().equals(CALENDAR_NAME)) {
                            calendarId = calendarListEntry.getId();
                            break;
                        }
                    }
                    if (calendarId != null) {
                        break;
                    }
                    pageToken = calendarList.getNextPageToken();
                } while (pageToken != null);

                /*
                 * If there is no Calendar named RubberDuck, create a new one.
                 */
                if (calendarId == null) {
                    LOGGER.info(LOG_MESSAGE_CREATING_CALENDAR);
                    Calendar calendar = new Calendar();
                    calendar.setSummary(CALENDAR_NAME);
                    calendar.setTimeZone(TimeZone.getDefault().getID());
                    Calendar createdCalendar = calendarClient.calendars().insert(calendar).execute();
                    calendarId = createdCalendar.getId();
                }

                LOGGER.info(String.format(LOG_MESSAGE_CALENDAR_ID, calendarId));

                pageToken = null;
                do {
                    TaskLists taskLists = tasksClient.tasklists().list().setPageToken(pageToken).execute();
                    List<TaskList> items = taskLists.getItems();
                    for (TaskList taskList : items) {
                        if (taskList.getTitle().equals(CALENDAR_NAME)) {
                            taskListId = taskList.getId();
                            break;
                        }
                    }
                    if (taskListId != null) {
                        break;
                    }
                    pageToken = taskLists.getNextPageToken();
                } while (pageToken != null);

                /*
                 * If there is no TaskList named RubberDuck, create a new one.
                 */
                if (taskListId == null) {
                    LOGGER.info(LOG_MESSAGE_CREATING_TASKLIST);
                    TaskList taskList = new TaskList();
                    taskList.setTitle(CALENDAR_NAME);
                    TaskList createdTaskList = tasksClient.tasklists().insert(taskList).execute();
                    taskListId = createdTaskList.getId();
                }

                LOGGER.info(String.format(LOG_MESSAGE_TASKLIST_ID, taskListId));
            } catch (IOException e) {
                throw new NetworkException(e.getMessage(), e.getCause());
            }

            initialized = true;
        }
    }

    /**
     * Log the user out and clear the credential.
     *
     * @throws IOException if fail to delete the credential.
     */
    public static void logOut() throws IOException {
        initialized = false;
        new File(CURRENT_DIRECTORY + DATA_STORE_DIR + DATA_STORE_NAME).delete();
    }

    /**
     * Set last sync time on server to current time.
     *
     * @throws IOException if fails to write to file
     */
    private static void setLastSyncTime() throws IOException {
        FileWriter fileWriter = new FileWriter(CURRENT_DIRECTORY + DATA_STORE_DIR + LAST_SYNC_FILE_NAME, false);
        String line = DATE_FORMAT.format(java.util.Calendar.getInstance().getTime());
        fileWriter.write(line);
        fileWriter.close();
    }

    /**
     * Get last sync time on server.
     *
     * @return last time a sync was finished, or null if never synced.
     * @throws IOException if fails to write to file
     */
    private static Date getLastSyncTime() throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(CURRENT_DIRECTORY + DATA_STORE_DIR + LAST_SYNC_FILE_NAME));
            String line = br.readLine();
            return DATE_FORMAT.parse(line);
        } catch (FileNotFoundException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Get the remote Task
     *
     * @param remoteId Remote ID of the task
     * @return the remote task inquired
     * @throws NetworkException                 if network failure happens.
     * @throws GoogleJsonResponseException if the operation cannot be finished.
     */
    public static Task getRemoteTask(String remoteId) throws NetworkException, GoogleJsonResponseException {
        assert initialized;
        try {
            return tasksClient.tasks().get(taskListId, remoteId).execute();
        } catch (GoogleJsonResponseException e) {
            /*
             * This happens when the requested ID cannot be found.
             */
            if (e.getDetails().getCode() == 400 && e.getDetails().getMessage().equals("Invalid Value")) {
                return null;
            } else {
                throw e;
            }
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Get the remote Event
     *
     * @param remoteId Remote ID of the event
     * @return the remote event inquired
     * @throws NetworkException if network failure happens.
     * * @throws GoogleJsonResponseException if the operation cannot be finished.
     */
    public static Event getRemoteEvent(String remoteId) throws NetworkException, GoogleJsonResponseException {
        assert initialized;
        try {
            return calendarClient.events().get(calendarId, remoteId).execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            /*
             * This happens when the requested ID cannot be found.
             */
            if ((e.getDetails().getCode() == 400 && e.getDetails().getMessage().equals("Invalid Value"))
                    || (e.getDetails().getCode() == 404 && e.getDetails().getMessage().equals("Not Found"))) {
                return null;
            } else {
                throw e;
            }
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Test if a task exists in remote database.
     *
     * @param task the local task to be tested
     * @return true if the task exists
     * @throws NetworkException if network failure happens.
     */
    public static boolean isInRemote(rubberduck.common.datatransfer.Task task) throws NetworkException {
        assert initialized;
        try {
            if (isPushedAsTask(task)) {
                return (getRemoteTask(getRemoteUuid(task)) != null);
            } else if (isPushedAsEvent(task)) {
                return (getRemoteEvent(getRemoteUuid(task)) != null);
            } else {
                return false;
            }
        } catch (GoogleJsonResponseException e) {
            return false;
        }
    }

    /**
     * Delete the given task on remote server.
     *
     * @param localTask the local task to be deleted remotely
     * @throws NetworkException if network failure happens.
     */
    public static void deleteTask(rubberduck.common.datatransfer.Task localTask) throws NetworkException {
        assert initialized;
        try {
            if (isPushedAsTask(localTask)) {
                tasksClient.tasks().delete(taskListId, getRemoteUuid(localTask)).execute();
            }
            if (isPushedAsEvent(localTask)) {
                calendarClient.events().delete(calendarId, getRemoteUuid(localTask)).execute();
            }
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Push the given task to remote server and overwrite any existing copy.
     * Note that after this, UUID of the task will be overwritten and it should be kept
     * by saving to the database.
     *
     * @param localTask the local task to be pushed
     * @throws NetworkException if network failure happens.
     */
    public static void pushTask(rubberduck.common.datatransfer.Task localTask) throws NetworkException {
        assert initialized;
        try {
            /*
             * We need to see if a copy exists on the server. If so, we need to use update instead of insert.
             */
            boolean shouldUpdate = true;
            if (localTask.isFloatingTask() || localTask.isDeadline()) {
                Task remoteTask = null;
                if (isPushedAsTask(localTask)) {
                    remoteTask = getRemoteTask(getRemoteUuid(localTask));
                }
                if (remoteTask == null) {
                    remoteTask = new Task();
                    shouldUpdate = false;
                }
                constructRemoteTask(remoteTask, localTask);
                if (shouldUpdate) {
                    remoteTask = tasksClient.tasks().update(taskListId, remoteTask.getId(), remoteTask).execute();
                } else {
                    remoteTask = tasksClient.tasks().insert(taskListId, remoteTask).execute();
                }
                localTask.setUuid(constructLocalTaskUuid(remoteTask.getId()));
            } else {
                Event remoteEvent = null;
                if (isPushedAsEvent(localTask)) {
                    remoteEvent = getRemoteEvent(getRemoteUuid(localTask));
                }
                if (remoteEvent == null) {
                    remoteEvent = new Event();
                    shouldUpdate = false;
                }
                constructRemoteEvent(remoteEvent, localTask);
                if (shouldUpdate) {
                    remoteEvent = calendarClient.events().update(calendarId, remoteEvent.getId(), remoteEvent).execute();
                } else {
                    remoteEvent = calendarClient.events().insert(calendarId, remoteEvent).execute();
                }
                localTask.setUuid(constructLocalEventUuid(remoteEvent.getId()));
            }
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Pull the requested task from remote server.
     *
     * @param localId the localId of the requested task.
     * @return the local task pulled
     * @throws NetworkException                   if network failure happens.
     * @throws UnsupportedOperationException if the task is never pushed.
     */
    public static rubberduck.common.datatransfer.Task pullTask(String localId) throws NetworkException {
        assert initialized;
        try {
            rubberduck.common.datatransfer.Task task;
            if (isLocalTaskUuid(localId)) {
                Task remoteTask = getRemoteTask(constructRemoteTaskId(localId));
                task = constructLocalTask(remoteTask);
            } else if (isLocalEventUuid(localId)) {
                Event remoteEvent = getRemoteEvent(constructRemoteEventId(localId));
                task = constructLocalEvent(remoteEvent);
            } else {
                throw new UnsupportedOperationException("This task is not pushed.");
            }
            return task;
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Convert a local task to a remote Task.
     *
     * @param remoteTask the local task to be converted
     * @param localTask  the converted remote Task
     */
    private static void constructRemoteTask(Task remoteTask, rubberduck.common.datatransfer.Task localTask) {
        remoteTask.setTitle(localTask.getDescription());
        if (isLocalTaskUuid(localTask.getUuid())) {
            remoteTask.setId(constructRemoteTaskId(localTask.getUuid()));
        }
        if (!localTask.isFloatingTask()) {
            remoteTask.setDue(calendarToDateTime(localTask.getEarliestDate()));
            /*
             * Google Tasks only supports set it to a date, so need to store the information in notes.
             */
            remoteTask.setNotes(REMOTE_TASK_TIME_FORMAT + DATE_FORMAT.format(localTask.getEarliestDate().getTime()));
        }
        if (localTask.getIsDone()) {
            remoteTask.setStatus("completed");
        } else {
            remoteTask.setStatus("needsAction");
            remoteTask.setCompleted(null);
        }
    }

    /**
     * Convert a local task to a remote Event.
     *
     * @param localTask   the local task to be converted
     * @param remoteEvent the converted remote Event
     */
    private static void constructRemoteEvent(Event remoteEvent, rubberduck.common.datatransfer.Task localTask) {
        remoteEvent.setSummary(localTask.getDescription());
        if (isLocalEventUuid(localTask.getUuid())) {
            remoteEvent.setId(constructRemoteEventId(localTask.getUuid()));
        }
        DatePair datePair = localTask.getDateList().get(0);
        remoteEvent.setStart(calendarToEventDateTime(datePair.getStartDate()));
        remoteEvent.setEnd(calendarToEventDateTime(datePair.getEndDate()));
        /*
         * Google Calendar does not support mark some event as done, need to store in description.
         * Furthermore, event color is utilized to store this information.
         */
        if (localTask.getIsDone()) {
            if (remoteEvent.getDescription() == null) {
                remoteEvent.setDescription(REMOTE_FLAG_COMPLETED);
            } else {
                String newDescription = remoteEvent.getDescription();
                newDescription = REMOTE_FLAG_COMPLETED + newDescription.replaceAll(REMOTE_FLAG_COMPLETED, "");
                remoteEvent.setDescription(newDescription);
            }
            remoteEvent.setColorId(COLOR_ID_COMPLETED);
        } else {
            if (remoteEvent.getDescription() == null) {
                remoteEvent.setDescription("");
            } else {
                remoteEvent.setDescription(remoteEvent.getDescription().replaceAll(REMOTE_FLAG_COMPLETED, ""));
            }
            remoteEvent.setColorId(COLOR_ID_UNCOMPLETED);
        }
    }

    /**
     * Convert a remote Task to a local task.
     *
     * @param remoteTask the remote Task to be converted
     * @return the converted local task
     */
    private static rubberduck.common.datatransfer.Task constructLocalTask(Task remoteTask) {
        rubberduck.common.datatransfer.Task localTask = new rubberduck.common.datatransfer.Task();
        localTask.setUuid(constructLocalTaskUuid(remoteTask.getId()));
        /*
         * Remote Task may have an empty title, need to rename it.
         */
        if (remoteTask.getTitle() == null || remoteTask.getTitle().isEmpty()) {
            localTask.setDescription(LOCAL_FLAG_UNNAMED_TASK);
        } else {
            localTask.setDescription(remoteTask.getTitle());
        }
        if (remoteTask.getStatus().equals("completed")) {
            localTask.setIsDone(true);
        } else {
            localTask.setIsDone(false);
        }
        if (remoteTask.getDue() != null) {
            java.util.Calendar remoteDueDate = dateTimeToCalendar(remoteTask.getDue());
            String line = remoteTask.getNotes();
            if (line != null && line.startsWith(REMOTE_TASK_TIME_FORMAT)) {
                try {
                    java.util.Calendar recordedDueDate = java.util.Calendar.getInstance();
                    recordedDueDate.setTime(DATE_FORMAT.parse(line.replace(REMOTE_TASK_TIME_FORMAT, "")));
                    if (isOnSameDate(remoteDueDate, recordedDueDate)) {
                        remoteDueDate = recordedDueDate;
                    }
                } catch (ParseException e) {
                }
            }
            ArrayList<DatePair> dateList = new ArrayList<DatePair>();
            dateList.add(new DatePair(remoteDueDate));
            localTask.setDateList(dateList);
        } else {
            localTask.setDateList(new ArrayList<DatePair>());
        }
        return localTask;
    }

    /**
     * Convert a remote Event to a local task.
     *
     * @param remoteEvent the remote Event to be converted
     * @return the converted local task
     */
    private static rubberduck.common.datatransfer.Task constructLocalEvent(Event remoteEvent) {
        rubberduck.common.datatransfer.Task localTask = new rubberduck.common.datatransfer.Task();
        localTask.setUuid(constructLocalEventUuid(remoteEvent.getId()));
        if (remoteEvent.getSummary() == null || remoteEvent.getSummary().isEmpty()) {
            localTask.setDescription(LOCAL_FLAG_UNNAMED_TASK);
        } else {
            localTask.setDescription(remoteEvent.getSummary());
        }
        ArrayList<DatePair> dateList = new ArrayList<DatePair>();
        dateList.add(new DatePair(eventDateTimeToCalendar(remoteEvent.getStart()),
                eventDateTimeToCalendar(remoteEvent.getEnd())));
        localTask.setDateList(dateList);
        /*
         * If the event color is one of the colors that is used to represent isDone, use that.
         * Otherwise, test if REMOTE_FLAG_COMPLETED exists in the description.
         */
        if (remoteEvent.getColorId() != null && remoteEvent.getColorId().equals(COLOR_ID_COMPLETED)) {
            localTask.setIsDone(true);
        } else if (remoteEvent.getColorId() != null && remoteEvent.getColorId().equals(COLOR_ID_UNCOMPLETED)) {
            localTask.setIsDone(false);
        } else {
            if (remoteEvent.getDescription() != null && remoteEvent.getDescription().contains(REMOTE_FLAG_COMPLETED)) {
                localTask.setIsDone(true);
            } else {
                localTask.setIsDone(false);
            }
        }
        return localTask;
    }

    /**
     * Get a list of remote Tasks
     *
     * @param filterByUpdateTime if the result should only contain items that were updated since last sync
     * @return the list of remote Tasks
     * @throws NetworkException if network failure happens.
     */
    public static ArrayList<Task> getRemoteTaskList(boolean filterByUpdateTime) throws NetworkException {
        assert initialized;
        try {
            ArrayList<Task> remoteTaskList = new ArrayList<Task>();

            String pageToken = null;
            do {
                com.google.api.services.tasks.Tasks.TasksOperations.List listOperation =
                        tasksClient.tasks().list(taskListId).setShowDeleted(true);
                if (filterByUpdateTime && lastSyncTime != null) {
                    listOperation.setUpdatedMin(new DateTime(lastSyncTime, TimeZone.getDefault()).toStringRfc3339());
                }
                Tasks tasks = listOperation.setPageToken(pageToken).execute();
                if (tasks != null && tasks.getItems() != null) {
                    remoteTaskList.addAll(tasks.getItems());
                }
                pageToken = tasks.getNextPageToken();
            } while (pageToken != null);

            return remoteTaskList;
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Get a list of remote Events
     *
     * @param filterByUpdateTime if the result should only contain items that were updated since last sync
     * @return the list of remote Events
     * @throws NetworkException if network failure happens.
     */
    public static ArrayList<Event> getRemoteEventList(boolean filterByUpdateTime) throws NetworkException {
        assert initialized;
        try {
            ArrayList<Event> remoteEventList = new ArrayList<Event>();

            String pageToken = null;
            do {
                com.google.api.services.calendar.Calendar.Events.List listOperation =
                        calendarClient.events().list(calendarId).setShowDeleted(true);
                if (filterByUpdateTime && lastSyncTime != null) {
                    listOperation = listOperation.setUpdatedMin(new DateTime(lastSyncTime, TimeZone.getDefault()));
                }
                Events events = listOperation.setPageToken(pageToken).execute();
                remoteEventList.addAll(events.getItems());
                pageToken = events.getNextPageToken();
            } while (pageToken != null);

            return remoteEventList;
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Delete all Tasks on the server.
     *
     * @throws NetworkException if network failure happens.
     */
    public static void clearRemoteTasks() throws NetworkException {
        assert initialized;
        try {
            tasksClient.tasks().clear(taskListId).execute();
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Delete all Events on the server.
     *
     * @throws NetworkException if network failure happens.
     */
    public static void clearRemoteEvents() throws NetworkException {
        assert initialized;
        try {
            calendarClient.calendars().clear(calendarId);
        } catch (IOException e) {
            throw new NetworkException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Push all local tasks to remote server and overwrite if exists.
     *
     * @param dbManager the DatabaseManager instance that stores tasks
     * @throws IOException if thrown by DatabaseManager.
     * @throws NetworkException if network failure happens.
     */
    public static void pushAll(DatabaseManager<rubberduck.common.datatransfer.Task> dbManager) throws IOException {
        assert initialized;
        LOGGER.info(LOG_MESSAGE_PUSHING);
        for (Long databaseId : dbManager.getValidIdList()) {
            rubberduck.common.datatransfer.Task localTask = dbManager.getInstance(databaseId);
            if (!(localTask.getDateList().size() > 1)) {
                pushTask(localTask);
                dbManager.modify(databaseId, localTask, null);
            }
        }
        for (Long databaseId : dbManager.getDeletedIdList()) {
            if (isPushed(dbManager.getInstance(databaseId))) {
                deleteTask(dbManager.getInstance(databaseId));
            }
        }
        dbManager.rewriteFile(true);
    }

    /**
     * Pull all tasks from remote server and overwrite if exists.
     *
     * @param dbManager the DatabaseManager instance that stores tasks
     * @throws IOException if thrown by DatabaseManager.
     * @throws NetworkException if network failure happens.
     */
    public static void pullAll(DatabaseManager<rubberduck.common.datatransfer.Task> dbManager) throws IOException {
        assert initialized;
        LOGGER.info(LOG_MESSAGE_PULLING);
        HashMap<String, Long> uuidMap = new HashMap<String, Long>();
        for (Long databaseId : dbManager.getValidIdList()) {
            uuidMap.put(dbManager.getInstance(databaseId).getUuid(), databaseId);
        }
        for (Task remoteTask : getRemoteTaskList(false)) {
            String localUuid = constructLocalTaskUuid(remoteTask.getId());
            if (remoteTask.getDeleted() != null && remoteTask.getDeleted()) {
                if (uuidMap.containsKey(localUuid)) {
                    dbManager.markAsInvalid(uuidMap.get(localUuid));
                }
            } else {
                dbManager.modify(uuidMap.get(localUuid), constructLocalTask(remoteTask), null);
            }
        }
        for (Event remoteEvent : getRemoteEventList(false)) {
            String localUuid = constructLocalEventUuid(remoteEvent.getId());
            if (remoteEvent.getStatus().equals("cancelled")) {
                if (uuidMap.containsKey(localUuid)) {
                    dbManager.markAsInvalid(uuidMap.get(localUuid));
                }
            } else {
                dbManager.modify(uuidMap.get(localUuid), constructLocalEvent(remoteEvent), null);
            }
        }
        dbManager.rewriteFile(true);
    }

    /**
     * Overwrite the whole remote database with local one.
     *
     * @param dbManager the DatabaseManager instance that stores tasks
     * @throws IOException if thrown by DatabaseManager.
     * @throws NetworkException if network failure happens.
     */
    public static void forcePushAll(DatabaseManager<rubberduck.common.datatransfer.Task> dbManager) throws IOException {
        assert initialized;
        clearRemoteEvents();
        clearRemoteTasks();
        pushAll(dbManager);
    }

    /**
     * Overwrite the whole local database with remote one.
     *
     * @param dbManager the DatabaseManager instance that stores tasks
     * @throws IOException if thrown by DatabaseManager.
     * @throws NetworkException if network failure happens.
     */
    public static void forcePullAll(DatabaseManager<rubberduck.common.datatransfer.Task> dbManager) throws IOException {
        assert initialized;
        dbManager.resetDatabase();
        pullAll(dbManager);
    }

    /**
     * Perform a two-way synchronization and update both databases with latest modified tasks.
     *
     * @param dbManager the DatabaseManager instance that stores tasks
     * @throws IOException if thrown by DatabaseManager.
     * @throws NetworkException if network failure happens.
     */
    public static void twoWaySync(DatabaseManager<rubberduck.common.datatransfer.Task> dbManager) throws IOException {
        assert initialized;
        LOGGER.info(LOG_MESSAGE_TWO_WAY);

        lastSyncTime = getLastSyncTime();
        HashMap<String, Long> localUuidMap = new HashMap<String, Long>();

        HashMap<String, Task> remoteModifiedTasks = new HashMap<String, Task>();
        for (Task remoteTask : getRemoteTaskList(true)) {
            remoteModifiedTasks.put(remoteTask.getId(), remoteTask);
        }
        HashMap<String, Event> remoteModifiedEvents = new HashMap<String, Event>();
        for (Event remoteEvent : getRemoteEventList(true)) {
            remoteModifiedEvents.put(remoteEvent.getId(), remoteEvent);
        }

        /*
         * Update remote database with locally modified tasks.
         */
        for (Long databaseId : dbManager.getValidIdList()) {
            rubberduck.common.datatransfer.Task localTask = dbManager.getInstance(databaseId);
            if (isPushed(localTask)) {
                localUuidMap.put(localTask.getUuid(), databaseId);
            }
            if (lastSyncTime == null || localTask.getLastUpdate().getTime().after(lastSyncTime)) {
                if (!(localTask.getDateList().size() > 1)) {
                    boolean shouldPush = false;
                    if (!isPushed(localTask)) {
                        shouldPush = true;
                    } else {
                        java.util.Calendar localUpdateTime = localTask.getLastUpdate();
                        if (localTask.isDeadline() || localTask.isFloatingTask()) {
                            String remoteUuid = constructRemoteTaskId(localTask.getUuid());
                            if (remoteModifiedTasks.containsKey(remoteUuid)) {
                                Task remoteTask = remoteModifiedTasks.get(remoteUuid);
                                if ((remoteTask.getDeleted() != null && remoteTask.getDeleted()) ||
                                        dateTimeToCalendar(remoteTask.getUpdated()).after(localUpdateTime)) {
                                    shouldPush = true;
                                    remoteModifiedTasks.remove(remoteUuid);
                                }
                            } else {
                                shouldPush = true;
                            }
                        } else {
                            String remoteUuid = constructRemoteEventId(localTask.getUuid());
                            if (remoteModifiedEvents.containsKey(remoteUuid)) {
                                Event remoteEvent = remoteModifiedEvents.get(remoteUuid);
                                if (remoteEvent.getStatus().equals("cancelled") ||
                                        dateTimeToCalendar(remoteEvent.getUpdated()).after(localUpdateTime)) {
                                    shouldPush = true;
                                    remoteModifiedEvents.remove(remoteUuid);
                                }
                            } else {
                                shouldPush = true;
                            }
                        }
                    }
                    if (shouldPush) {
                        pushTask(localTask);
                        dbManager.modify(databaseId, localTask, null);
                    }
                }
            }
        }

        /*
         * Delete locally deleted tasks on remote server if they are not modified remotely.
         */
        for (Long databaseId : dbManager.getDeletedIdList()) {
            rubberduck.common.datatransfer.Task localTask = dbManager.getInstance(databaseId);
            if (!(localTask.getDateList().size() > 1)) {
                boolean shouldDelete = false;
                if (!isPushed(localTask)) {
                    shouldDelete = false;
                } else {
                    if (localTask.isDeadline() || localTask.isFloatingTask()) {
                        String remoteUuid = constructRemoteTaskId(localTask.getUuid());
                        if (remoteModifiedTasks.containsKey(remoteUuid)) {
                            shouldDelete = false;
                            if (remoteModifiedTasks.get(remoteUuid).getDeleted() != null && remoteModifiedTasks.get(remoteUuid).getDeleted()) {
                                remoteModifiedTasks.remove(remoteUuid);
                            }
                        } else {
                            shouldDelete = true;
                        }
                    } else {
                        String remoteUuid = constructRemoteEventId(localTask.getUuid());
                        if (remoteModifiedEvents.containsKey(remoteUuid)) {
                            shouldDelete = false;
                            if (remoteModifiedEvents.get(remoteUuid).getStatus().equals("cancelled")) {
                                remoteModifiedEvents.remove(remoteUuid);
                            }
                        } else {
                            shouldDelete = true;
                        }
                    }
                }
                if (shouldDelete) {
                    deleteTask(localTask);
                }
            }
        }

        /*
         * Update local database with modified or deleted Tasks in remote database.
         */
        for (Task remoteTask : remoteModifiedTasks.values()) {
            String localUuid = constructLocalTaskUuid(remoteTask.getId());
            if (localUuidMap.containsKey(localUuid)) {
                if (remoteTask.getDeleted() != null && remoteTask.getDeleted()) {
                    dbManager.modify(localUuidMap.get(localUuid), null, null);
                } else {
                    dbManager.modify(localUuidMap.get(localUuid), constructLocalTask(remoteTask), null);
                }
            } else {
                if (remoteTask.getDeleted() == null || !remoteTask.getDeleted()) {
                    dbManager.modify(null, constructLocalTask(remoteTask), null);
                }
            }
        }

        /*
         * Update local database with modified or deleted Events in remote database.
         */
        for (Event remoteEvent : remoteModifiedEvents.values()) {
            String localUuid = constructLocalEventUuid(remoteEvent.getId());
            if (localUuidMap.containsKey(localUuid)) {
                if (remoteEvent.getStatus().equals("cancelled")) {
                    dbManager.modify(localUuidMap.get(localUuid), null, null);
                } else {
                    dbManager.modify(localUuidMap.get(localUuid), constructLocalEvent(remoteEvent), null);
                }
            } else {
                if (!remoteEvent.getStatus().equals("cancelled")) {
                    dbManager.modify(null, constructLocalEvent(remoteEvent), null);
                }
            }
        }

        dbManager.rewriteFile(true);
        setLastSyncTime();
    }

    /*
     * Some utility methods for EventDateTime, DateTime, java.formatter.Calendar and java.formatter.Date
     */

    private static boolean isOnSameDate(java.util.Calendar cal1, java.util.Calendar cal2) {
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private static DateTime calendarToDateTime(java.util.Calendar calendar) {
        return new DateTime(calendar.getTime(), calendar.getTimeZone());
    }

    private static EventDateTime calendarToEventDateTime(
            java.util.Calendar calendar) {
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDateTime(calendarToDateTime(calendar));
        return eventDateTime;
    }

    private static java.util.Calendar dateTimeToCalendar(DateTime dateTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (dateTime.isDateOnly()) {
            calendar.setTimeInMillis(dateTime.getValue() - TimeZone.getDefault().getOffset(dateTime.getValue()));
        } else {
            calendar.setTimeInMillis(dateTime.getValue());
        }
        return calendar;
    }

    private static java.util.Calendar eventDateTimeToCalendar(
            EventDateTime eventDateTime) {
        if (eventDateTime.getDateTime() != null) {
            return dateTimeToCalendar(eventDateTime.getDateTime());
        } else {
            return dateTimeToCalendar(eventDateTime.getDate());
        }
    }


    /*
     * Some utility methods for UUIDs
     */
    private static boolean isPushedAsTask(rubberduck.common.datatransfer.Task task) {
        return isLocalTaskUuid(task.getUuid());
    }

    private static boolean isPushedAsEvent(rubberduck.common.datatransfer.Task task) {
        return isLocalEventUuid(task.getUuid());
    }

    private static boolean isPushed(rubberduck.common.datatransfer.Task task) {
        return isPushedAsTask(task) || isPushedAsEvent(task);
    }

    private static boolean isLocalTaskUuid(String localUuid) {
        return localUuid.startsWith(LOCAL_UUID_PREFIX_TASK);
    }

    private static boolean isLocalEventUuid(String localUuid) {
        return localUuid.startsWith(LOCAL_UUID_PREFIX_EVENT);
    }

    private static String constructLocalTaskUuid(String remoteUuid) {
        return LOCAL_UUID_PREFIX_TASK + remoteUuid;
    }

    private static String constructLocalEventUuid(String remoteUuid) {
        return LOCAL_UUID_PREFIX_EVENT + remoteUuid;
    }

    private static String constructRemoteTaskId(String localUuid) {
        assert isLocalTaskUuid(localUuid);
        return localUuid.replaceFirst(LOCAL_UUID_PREFIX_TASK, "");
    }

    private static String constructRemoteEventId(String localUuid) {
        assert isLocalEventUuid(localUuid);
        return localUuid.replaceFirst(LOCAL_UUID_PREFIX_EVENT, "");
    }

    private static String getRemoteUuid(rubberduck.common.datatransfer.Task task) {
        if (isPushedAsTask(task)) {
            return constructRemoteTaskId(task.getUuid());
        } else if (isPushedAsEvent(task)) {
            return constructRemoteEventId(task.getUuid());
        } else {
            throw new UnsupportedOperationException("This task is not pushed.");
        }
    }

}
