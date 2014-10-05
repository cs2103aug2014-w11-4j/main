import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DatePairTest {

    @Before
    public void setUp() throws Exception {
        Logic.startDatabase();
        Logic.getDB().resetDatabase();
    }

    @After
    public void tearDown() throws Exception {
        Logic.getDB().closeFile();
    }


    /**
     * Test for view of task that are within date range
     * Task : 1 aug 2014 - 20 aug 2014
     * View Scope : 1 july 2014 - 25 aug 2014
     * result: true
     */

    @Test
    public void taskWithinPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.JULY, 1);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 25);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Test for view of task overlap date range
     * Task : 1 aug 2014 - 20 aug 2014
     * View Scope : 5 aug 2014 - 15 aug 2014
     * Expected: true
     */

    @Test
    public void taskOverlapPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 5);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 15);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

    /**
     * Test for view of task overlap date range
     * Task : 20 aug 2014
     * View Scope : 5 aug 2014 - 30 aug 2014
     * Expected: true
     */

    @Test
    public void endDateWithinPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(endDate);
        dpList.add(dp);
        Task task = new Task("Test Date", dpList);

        Calendar viewStartDate = Calendar.getInstance();
        viewStartDate.set(2014, Calendar.AUGUST, 5);
        Calendar viewEndDate = Calendar.getInstance();
        viewEndDate.set(2014, Calendar.AUGUST, 30);

        DatePair viewDp = new DatePair(viewStartDate, viewEndDate);
        boolean actual = task.isWithinPeriod(viewDp);

        assertEquals(true, actual);

    }

}
