package com.rubberduck.io;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;

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
import com.google.api.services.calendar.model.*;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GooManager {

    private static final String CLIENT_ID = "849841048712-0t9rn1vi1nch19cqsuaaaj19oo7c7pl3.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "Zq0v8OByEqQfPMXZis8Iw86D";
    private static final String APPLICATION_NAME = "RubberDuck/0.2";
    private static final String CALENDAR_NAME = "RubberDuck";

    private static final String LOCAL_UUID_TASK = "_RD_T_";
    private static final String LOCAL_UUID_EVENT = "_RD_E_";

    private static HttpTransport httpTransport;
    private static MemoryDataStoreFactory memoryDataStoreFactory = MemoryDataStoreFactory.getDefaultInstance();

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

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
                httpTransport, JSON_FACTORY, clientSecrets, scopes).setDataStoreFactory(memoryDataStoreFactory).build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private static void setupConnection() throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = authorize();

        calendarClient = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        tasksClient = new com.google.api.services.tasks.Tasks.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
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
            System.out.println("Not found, creating new one");
            Calendar calendar = new Calendar();
            calendar.setSummary(CALENDAR_NAME);
            calendar.setTimeZone("Asia/Singapore");
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
            System.out.println("Not found, creating new one");
            TaskList taskList = new TaskList();
            taskList.setTitle(CALENDAR_NAME);
            TaskList createdTaskList = tasksClient.tasklists().insert(taskList).execute();
            taskListId = createdTaskList.getId();
        }
    }

    private static boolean isPushedAsTask(Task task) {
        return isLocalTaskUuid(task.getUuid());
    }

    private static boolean isPushedAsEvent(Task task) {
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

    public static boolean isPushed(Task task) {
        return isPushedAsTask(task) || isPushedAsEvent(task);
    }

    private static String getRemoteUuid(Task task) {
        if (isPushedAsTask(task)) {
            return constructRemoteTaskId(task.getUuid());
        } else if (isPushedAsEvent(task)) {
            return constructRemoteEventId(task.getUuid());
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public static boolean isInRemote(Task task) throws IOException {
        if (isPushedAsTask(task)) {
            return (getRemoteTask(getRemoteUuid(task)) != null);
        } else if (isPushedAsEvent(task)) {
            return (getRemoteEvent(getRemoteUuid(task)) != null);
        } else {
            return false;
        }
    }

    public static Task pushTask(Task originalTask) throws IOException {
        boolean shouldUpdate = true;
        if (originalTask.isFloatingTask() || originalTask.isDeadline()) {
            com.google.api.services.tasks.model.Task task = null;
            if (isPushedAsTask(originalTask)) {
                task = getRemoteTask(getRemoteUuid(originalTask));
            }
            if (task == null) {
                task = new com.google.api.services.tasks.model.Task();
                shouldUpdate = false;
            }
            prepareTask(task, originalTask);
            if (shouldUpdate) {
                task = tasksClient.tasks().update(taskListId, task.getId(), task).execute();
            } else {
                task = tasksClient.tasks().insert(taskListId, task).execute();
            }
            originalTask.setUuid(constructLocalTaskUuid(task.getId()));
        } else {
            com.google.api.services.calendar.model.Event event = null;
            if (isPushedAsEvent(originalTask)) {
                event = getRemoteEvent(originalTask.getUuid());
            }
            if (event == null) {
                event = new com.google.api.services.calendar.model.Event();
                shouldUpdate = false;
            }
            prepareEvent(event, originalTask);
            if (shouldUpdate) {
                event = calendarClient.events().update(calendarId, event.getId(), event).execute();
            } else {
                event = calendarClient.events().insert(calendarId, event).execute();
            }
            originalTask.setUuid(constructLocalEventUuid(event.getId()));
        }
        return originalTask;
    }

    public static com.google.api.services.tasks.model.Task getRemoteTask(String remoteId) throws IOException {
        try {
            return tasksClient.tasks().get(taskListId, remoteId).execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getDetails().getCode() == 400 && e.getDetails().getMessage().equals("Invalid Value")) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public static com.google.api.services.calendar.model.Event getRemoteEvent(String remoteId) throws IOException {
        try {
            return calendarClient.events().get(calendarId, remoteId).execute();
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if ((e.getDetails().getCode() == 400 && e.getDetails().getMessage().equals("Invalid Value")) || (e.getDetails().getCode() == 404 && e.getDetails().getMessage().equals("Not Found"))) {
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

    private static void prepareTask(com.google.api.services.tasks.model.Task task, Task originalTask) {
        task.setTitle(originalTask.getDescription());
        if (!originalTask.isFloatingTask()) {
            task.setDue(calendarToDateTime(originalTask.getEarliestDate()));
        }
        if (originalTask.getIsDone()) {
            task.setStatus("completed");
        }
    }

    private static void prepareEvent(com.google.api.services.calendar.model.Event event, Task originalTask) {
        event.setSummary(originalTask.getDescription());
        DatePair datePair = originalTask.getDateList().get(0);
        event.setStart(calendarToEventDateTime(datePair.getStartDate()));
        event.setEnd(calendarToEventDateTime(datePair.getEndDate()));
    }

    public static Task pullTask(String localId) throws IOException {
        Task task = new Task();
        task.setUuid(localId);
        if (isLocalTaskUuid(localId)) {
            com.google.api.services.tasks.model.Task remoteTask = getRemoteTask(constructRemoteTaskId(localId));
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
            }
        } else if (isLocalEventUuid(localId)) {
            com.google.api.services.calendar.model.Event remoteEvent = getRemoteEvent(constructRemoteEventId(localId));
            task.setDescription(remoteEvent.getSummary());
            // TODO: what should we do with completed here?
            ArrayList<DatePair> dateList = new ArrayList<DatePair>();
            dateList.add(new DatePair(eventDateTimeToCalendar(remoteEvent.getStart()), eventDateTimeToCalendar(remoteEvent.getEnd())));
            task.setDateList(dateList);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
        return task;
    }

    public static ArrayList<com.google.api.services.tasks.model.Task> getRemoteTaskList() throws IOException {
        ArrayList<com.google.api.services.tasks.model.Task> remoteTaskList = new ArrayList<com.google.api.services.tasks.model.Task>();

        String pageToken = null;
        do {
            Tasks tasks = tasksClient.tasks().list(taskListId).setPageToken(pageToken).execute();
            remoteTaskList.addAll(tasks.getItems());
            pageToken = tasks.getNextPageToken();
        } while (pageToken != null);

        return remoteTaskList;
    }

    public static ArrayList<com.google.api.services.calendar.model.Event> getRemoteEventList() throws IOException {
        ArrayList<com.google.api.services.calendar.model.Event> remoteEventList = new ArrayList<com.google.api.services.calendar.model.Event>();

        String pageToken = null;
        do {
            Events events = calendarClient.events().list(calendarId).setPageToken(pageToken).execute();
            remoteEventList.addAll(events.getItems());
            pageToken = events.getNextPageToken();
        } while (pageToken != null);

        return remoteEventList;
    }

    public static void main(String[] args) throws Exception {
        initialize();
        System.out.println(calendarId);
        System.out.println(taskListId);
    }

}
