import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class GooManager {
    
    private static final String APPLICATION_NAME = "RubberDuck/0.2";
    private static final String CALENDAR_NAME = "RubberDuck";

    private static HttpTransport httpTransport;
    private static MemoryDataStoreFactory memoryDataStoreFactory = MemoryDataStoreFactory.getDefaultInstance();

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static com.google.api.services.calendar.Calendar client;

    private static String calendarId = null;


    private static Credential authorize() throws IOException {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId("849841048712-0t9rn1vi1nch19cqsuaaaj19oo7c7pl3.apps.googleusercontent.com");
        details.setClientSecret("Zq0v8OByEqQfPMXZis8Iw86D");
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(memoryDataStoreFactory)
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void setupConnection() throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Credential credential = authorize();

        client = new com.google.api.services.calendar.Calendar.Builder(
                httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
    }

    public static void initialize() throws IOException, GeneralSecurityException {
        setupConnection();

        String pageToken = null;
        do {
            CalendarList calendarList = client.calendarList().list().setPageToken(pageToken).execute();
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
            Calendar createdCalendar = client.calendars().insert(calendar).execute();
            calendarId = createdCalendar.getId();
        }
    }

    public static void main(String[] args) throws Exception {
        initialize();
        System.out.println(calendarId);
    }

}
