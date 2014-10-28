package com.rubberduck.io;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Tasks;
import com.google.api.services.tasks.model.Task;
import com.rubberduck.logic.DatePair;

public class GooManager {

    private static final String CLIENT_ID = "849841048712-0t9rn1vi1nch19cqsuaaaj19oo7c7pl3.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "Zq0v8OByEqQfPMXZis8Iw86D";
    private static final String APPLICATION_NAME = "RubberDuck/0.2";
    private static final String CALENDAR_NAME = "RubberDuck";

    private static final String REMOTE_FLAG_COMPLETED = "[Completed]";

    private static final String LOCAL_UUID_TASK = "_RD_T_";
    private static final String LOCAL_UUID_EVENT = "_RD_E_";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport httpTransport;
    private static MemoryDataStoreFactory memoryDataStoreFactory = MemoryDataStoreFactory.getDefaultInstance();

    private static com.google.api.services.calendar.Calendar calendarClient;
    private static String calendarId = null;

    private static com.google.api.services.tasks.Tasks tasksClient;
    private static String taskListId = null;

    private static Credential authorize() throws IOException {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(CLIENT_ID);
        details.setClientSecret(CLIENT_SECRET);
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        ArrayList<String> scopes = new ArrayList<String>();
        scopes.add(CalendarScopes.CALENDAR);
        scopes.add(TasksScopes.TASKS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, scopes).setDataStoreFactory(
                memoryDataStoreFactory).build();

        return new AuthorizationCodeInstalledApp(flow,
                new LocalServerReceiver()).authorize("user");
    }

    private static void setupConnection() throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = authorize();

        calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();

        tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(
                APPLICATION_NAME).build();
    }

    public static void initialize() throws IOException, GeneralSecurityException {
        setupConnection();

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

        if (calendarId == null) {
            // System.out.println("Not found, creating new one"); //TODO
            Calendar calendar = new Calendar();
            calendar.setSummary(CALENDAR_NAME);
            calendar.setTimeZone(TimeZone.getDefault().getID());
            Calendar createdCalendar = calendarClient.calendars().insert(calendar).execute();
            calendarId = createdCalendar.getId();
        }

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

        if (taskListId == null) {
            // System.out.println("Not found, creating new one"); //TODO
            TaskList taskList = new TaskList();
            taskList.setTitle(CALENDAR_NAME);
            TaskList createdTaskList = tasksClient.tasklists().insert(taskList).execute();
            taskListId = createdTaskList.getId();
        }
    }

    private static boolean isPushedAsTask(com.rubberduck.logic.Task task) {
        return isLocalTaskUuid(task.getUuid());
    }

    private static boolean isPushedAsEvent(com.rubberduck.logic.Task task) {
        return isLocalEventUuid(task.getUuid());
    }

    private static boolean isLocalTaskUuid(String localUuid) {
        return localUuid.startsWith(LOCAL_UUID_TASK);
    }

    private static boolean isLocalEventUuid(String localUuid) {
        return localUuid.startsWith(LOCAL_UUID_EVENT);
    }

    private static String constructLocalTaskUuid(String remoteUuid) {
        return LOCAL_UUID_TASK + remoteUuid;
    }

    private static String constructLocalEventUuid(String remoteUuid) {
        return LOCAL_UUID_EVENT + remoteUuid;
    }

    private static String constructRemoteTaskId(String localUuid) {
        assert isLocalTaskUuid(localUuid);
        return localUuid.replaceFirst(LOCAL_UUID_TASK, "");
    }

    private static String constructRemoteEventId(String localUuid) {
        assert isLocalEventUuid(localUuid);
        return localUuid.replaceFirst(LOCAL_UUID_EVENT, "");
    }

    public static boolean isPushed(com.rubberduck.logic.Task task) {
        return isPushedAsTask(task) || isPushedAsEvent(task);
    }

    private static String getRemoteUuid(com.rubberduck.logic.Task task) {
        if (isPushedAsTask(task)) {
            return constructRemoteTaskId(task.getUuid());
        } else if (isPushedAsEvent(task)) {
            return constructRemoteEventId(task.getUuid());
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public static boolean isInRemote(com.rubberduck.logic.Task task) throws IOException {
        if (isPushedAsTask(task)) {
            return (getRemoteTask(getRemoteUuid(task)) != null);
        } else if (isPushedAsEvent(task)) {
            return (getRemoteEvent(getRemoteUuid(task)) != null);
        } else {
            return false;
        }
    }

    public static void pushTask(com.rubberduck.logic.Task originalTask) throws IOException {
        boolean shouldUpdate = true;
        if (originalTask.isFloatingTask() || originalTask.isDeadline()) {
            Task remoteTask = null;
            if (isPushedAsTask(originalTask)) {
                remoteTask = getRemoteTask(getRemoteUuid(originalTask));
            }
            if (remoteTask == null) {
                remoteTask = new Task();
                shouldUpdate = false;
            }
            prepareTask(remoteTask, originalTask);
            if (shouldUpdate) {
                remoteTask = tasksClient.tasks().update(taskListId, remoteTask.getId(), remoteTask).execute();
            } else {
                remoteTask = tasksClient.tasks().insert(taskListId, remoteTask).execute();
            }
            originalTask.setUuid(constructLocalTaskUuid(remoteTask.getId()));
        } else {
            Event remoteEvent = null;
            if (isPushedAsEvent(originalTask)) {
                remoteEvent = getRemoteEvent(getRemoteUuid(originalTask));
            }
            if (remoteEvent == null) {
                remoteEvent = new Event();
                shouldUpdate = false;
            }
            prepareEvent(remoteEvent, originalTask);
            if (shouldUpdate) {
                remoteEvent = calendarClient.events().update(calendarId, remoteEvent.getId(), remoteEvent).execute();
            } else {
                remoteEvent = calendarClient.events().insert(calendarId, remoteEvent).execute();
            }
            originalTask.setUuid(constructLocalEventUuid(remoteEvent.getId()));
        }
    }

    public static Task getRemoteTask(String remoteId) throws IOException {
        try {
            return tasksClient.tasks().get(taskListId, remoteId).execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getDetails().getCode() == 400
                    && e.getDetails().getMessage().equals("Invalid Value")) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public static Event getRemoteEvent(String remoteId) throws IOException {
        try {
            return calendarClient.events().get(calendarId, remoteId).execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if ((e.getDetails().getCode() == 400 && e.getDetails().getMessage().equals("Invalid Value"))
                    || (e.getDetails().getCode() == 404 && e.getDetails().getMessage().equals("Not Found"))) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private static DateTime calendarToDateTime(java.util.Calendar calendar) {
        return new DateTime(calendar.getTime(), calendar.getTimeZone());
    }

    private static EventDateTime calendarToEventDateTime(java.util.Calendar calendar) {
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDateTime(calendarToDateTime(calendar));
        return eventDateTime;
    }

    private static java.util.Calendar dateTimeToCalendar(DateTime dateTime) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(dateTime.getValue());
        return calendar;
    }

    private static java.util.Calendar eventDateTimeToCalendar(EventDateTime eventDateTime) {
        return dateTimeToCalendar(eventDateTime.getDateTime());
    }

    private static void prepareTask(Task task, com.rubberduck.logic.Task originalTask) {
        task.setTitle(originalTask.getDescription());
        if (isLocalTaskUuid(originalTask.getUuid())) {
            task.setId(constructRemoteTaskId(originalTask.getUuid()));
        }
        if (!originalTask.isFloatingTask()) {
            task.setDue(calendarToDateTime(originalTask.getEarliestDate()));
        }
        if (originalTask.getIsDone()) {
            task.setStatus("completed");
        } else {
            task.setStatus("needsAction");
            task.setCompleted(null);
        }
    }

    private static void prepareEvent(Event event, com.rubberduck.logic.Task originalTask) {
        event.setSummary(originalTask.getDescription());
        if (isLocalEventUuid(originalTask.getUuid())) {
            event.setId(constructRemoteEventId(originalTask.getUuid()));
        }
        DatePair datePair = originalTask.getDateList().get(0);
        event.setStart(calendarToEventDateTime(datePair.getStartDate()));
        event.setEnd(calendarToEventDateTime(datePair.getEndDate()));
        if (originalTask.getIsDone()) {
            event.setDescription(REMOTE_FLAG_COMPLETED);
        }
    }

    private static com.rubberduck.logic.Task reconstructTask(Task remoteTask) {
        com.rubberduck.logic.Task task = new com.rubberduck.logic.Task();
        task.setUuid(constructLocalTaskUuid(remoteTask.getId()));
        task.setDescription(remoteTask.getTitle());
        if (remoteTask.getStatus().equals("completed")) {
            task.setIsDone(true);
        } else {
            task.setIsDone(false);
        }
        if (remoteTask.getDue() != null) {
            ArrayList<DatePair> dateList = new ArrayList<DatePair>();
            dateList.add(new DatePair(dateTimeToCalendar(remoteTask.getDue())));
            task.setDateList(dateList);
        } else {
            task.setDateList(new ArrayList<DatePair>());
        }
        return task;
    }

    private static com.rubberduck.logic.Task reconstructEvent(Event remoteEvent) {
        com.rubberduck.logic.Task task = new com.rubberduck.logic.Task();
        task.setUuid(constructLocalEventUuid(remoteEvent.getId()));
        task.setDescription(remoteEvent.getSummary());
        ArrayList<DatePair> dateList = new ArrayList<DatePair>();
        dateList.add(new DatePair(
                eventDateTimeToCalendar(remoteEvent.getStart()),
                eventDateTimeToCalendar(remoteEvent.getEnd())));
        task.setDateList(dateList);
        if (remoteEvent.getDescription().startsWith(REMOTE_FLAG_COMPLETED)) {
            task.setIsDone(true);
        } else {
            task.setIsDone(false);
        }
        return task;
    }

    public static com.rubberduck.logic.Task pullTask(String localId) throws IOException {
        com.rubberduck.logic.Task task;
        if (isLocalTaskUuid(localId)) {
            Task remoteTask = getRemoteTask(constructRemoteTaskId(localId));
            task = reconstructTask(remoteTask);
        } else if (isLocalEventUuid(localId)) {
            Event remoteEvent = getRemoteEvent(constructRemoteEventId(localId));
            task = reconstructEvent(remoteEvent);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
        return task;
    }

    public static ArrayList<Task> getRemoteTaskList() throws IOException {
        ArrayList<Task> remoteTaskList = new ArrayList<Task>();

        String pageToken = null;
        do {
            Tasks tasks = tasksClient.tasks().list(taskListId).setPageToken(pageToken).execute();
            remoteTaskList.addAll(tasks.getItems());
            pageToken = tasks.getNextPageToken();
        } while (pageToken != null);

        return remoteTaskList;
    }

    public static ArrayList<Event> getRemoteEventList() throws IOException {
        ArrayList<Event> remoteEventList = new ArrayList<Event>();

        String pageToken = null;
        do {
            Events events = calendarClient.events().list(calendarId).setPageToken(pageToken).execute();
            remoteEventList.addAll(events.getItems());
            pageToken = events.getNextPageToken();
        } while (pageToken != null);

        return remoteEventList;
    }

    public static void clearRemoteEvents() throws IOException {
        calendarClient.calendars().clear(calendarId);
    }

    public static void clearRemoteTasks() throws IOException {
        tasksClient.tasks().clear(taskListId);
    }

    public static void pushAll(DatabaseManager<com.rubberduck.logic.Task> dbManager) throws IOException {
        for (Long databaseId : dbManager.getValidIdList()) {
            com.rubberduck.logic.Task localTask = dbManager.getInstance(databaseId);
            pushTask(localTask);
            dbManager.modify(databaseId, localTask, null);
        }
        dbManager.rewriteFile();
    }

    public static void forcePushAll(DatabaseManager<com.rubberduck.logic.Task> dbManager) throws IOException {
        clearRemoteEvents();
        clearRemoteTasks();
        pushAll(dbManager);
    }

    public static void pullAll(DatabaseManager<com.rubberduck.logic.Task> dbManager) throws IOException {
        HashMap<String, Long> uuidMap = new HashMap<String, Long>();
        for (Long databaseId : dbManager.getValidIdList()) {
            uuidMap.put(dbManager.getInstance(databaseId).getUuid(), databaseId);
        }
        for (Task remoteTask : getRemoteTaskList()) {
            if (remoteTask != null && !remoteTask.getTitle().isEmpty()) {
                dbManager.modify(uuidMap.get(constructLocalTaskUuid(remoteTask.getId())), reconstructTask(remoteTask), null);
            }
        }
        for (Event remoteEvent : getRemoteEventList()) {
            if (remoteEvent != null) {
                dbManager.modify(uuidMap.get(constructLocalEventUuid(remoteEvent.getId())), reconstructEvent(remoteEvent), null);
            }
        }
    }

    public static void forcePullAll(DatabaseManager<com.rubberduck.logic.Task> dbManager) throws IOException {
        dbManager.resetDatabase();
        pullAll(dbManager);
    }

    public static void main(String[] args) throws Exception {
        initialize();
        System.out.println(calendarId);
        System.out.println(taskListId);
    }

}
