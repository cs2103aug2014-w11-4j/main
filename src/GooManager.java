import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.client.util.DateTime;
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

    public static void setupConnection() throws IOException, GeneralSecurityException {
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

    public static boolean isPushed(Task task) {
        return !(task.getUuid().contains("-"));
    }

    public static boolean isInRemote(Task task) throws IOException {
        if (!isPushed(task)) {
            return false;
        }
        try {
            if (task.isFloatingTask() || task.isDeadline()) {
                com.google.api.services.tasks.model.Task remoteTask = getRemoteTask(task.getUuid());
            } else {
                com.google.api.services.calendar.model.Event remoteTask = getRemoteEvent(task.getUuid());
            }
            return true;
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getDetails().getCode() == 400 && e.getDetails().getMessage().equals("Invalid Value")) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public static Task pushNewTask(Task originalTask) throws IOException {
        if (originalTask.isFloatingTask() || originalTask.isDeadline()) {
            com.google.api.services.tasks.model.Task task = new com.google.api.services.tasks.model.Task();
            task.setTitle(originalTask.getDescription());
            if (!originalTask.isFloatingTask()) {
                task.setDue(new DateTime(originalTask.getEarliestDate().getTime(), originalTask.getEarliestDate().getTimeZone()));
            }
            if (originalTask.getIsDone()) {
                task.setStatus("completed");
            }
            task = tasksClient.tasks().insert(taskListId, task).execute();
            originalTask.setUuid(task.getId());
        } else {
            DatePair datePair = originalTask.getDateList().get(0);
            com.google.api.services.calendar.model.Event event = new com.google.api.services.calendar.model.Event();
            event.setSummary(originalTask.getDescription());
            EventDateTime startTime = new EventDateTime();
            startTime.setDateTime(new DateTime(datePair.getStartDate().getTime(), datePair.getStartDate().getTimeZone()));
            event.setStart(startTime);
            EventDateTime endTime = new EventDateTime();
            endTime.setDateTime(new DateTime(datePair.getEndDate().getTime(), datePair.getEndDate().getTimeZone()));
            event.setEnd(endTime);
            event.setId(originalTask.getUuid().replace("-", ""));
            event = calendarClient.events().insert(calendarId, event).execute();
            originalTask.setUuid(event.getId());
        }
        return originalTask;
    }

    public static com.google.api.services.tasks.model.Task getRemoteTask(String id) throws IOException {
        return tasksClient.tasks().get(taskListId, id).execute();
    }

    public static com.google.api.services.calendar.model.Event getRemoteEvent(String id) throws IOException {
        return calendarClient.events().get(calendarId, id).execute();
    }

    public static void main(String[] args) throws Exception {
        initialize();
        System.out.println(calendarId);
        System.out.println(taskListId);
    }

}
