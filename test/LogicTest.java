import static org.junit.Assert.*;

import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogicTest {

    @Before
    public void setUp() throws IOException {
        Logic.startDatabase();
        Logic.getDB().resetDatabase();
    }

    @After
    public void tearDown() throws IOException {
        Logic.getDB().closeFile();
    }

    /**
     * Test adding of task with todays date
     * 
     * Add a task with todays date and current runtime
     * Call display to display specified task via id
     * Store both actual and expected values
     * Mark recent created task as invalid
     * Execute comparison
     * 
     */
    @Test
    public void addTask() {
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar today = Calendar.getInstance();
        DatePair dp = new DatePair(today);
        datePairList.add(dp);
        Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);

        assertEquals(1, Logic.getDB().getValidIdList().size());
    }

    /**
     * Adding Task without start date / end date
     * 
     * Add a task without specifying any date
     * After retrieving the value, mark it as invalid
     *  
     */
    @Test

    public void addNoDateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = Logic.viewTask(id);
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done ";
        Logic.delete(id);
        assertEquals(expected, actual);
    }

    /**
     * Adding Task with end date without start date 
     * 
     * Adding Task without a start date
     * After retrieving the value, mark it as invalid
     *  
     */
    @Test
    public void addNoStartDateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        DatePair dp = new DatePair(null, Calendar.getInstance());
        dpList.add(dp);
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = Logic.viewTask(id);

        // formatting current dateTime
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        dateFormat.setCalendar(dp.getEndDate());
        String endDate = dateFormat.format(dp.getEndDate().getTime());

        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done \n[No Start Date] "
                + endDate;
        Logic.delete(id);
        assertEquals(expected, actual);
    }

    /**
     * Search for keyword in description
     * 
     */
    @Test
    public void searchKeywordTest() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = Logic.searchWithKeyword("Lorem");
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done ";
        Logic.delete(id);
        assertEquals(expected, actual);
    }

    /**
     * Test for viewing task within period
     * Condition: Task have both start date and end date within period
     */
    @Test
    public void viewWithinPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 10);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        long id = Logic.addTask("Within Period", dpList);

        Calendar startDateRange = new GregorianCalendar();
        startDateRange.set(2014, Calendar.AUGUST, 1);
        Calendar endDateRange = new GregorianCalendar();
        endDateRange.set(2014, Calendar.AUGUST, 30);
        DatePair dpRange = new DatePair(startDateRange, endDateRange);
        ArrayList<DatePair> dpRangeList = new ArrayList<DatePair>();
        dpRangeList.add(dpRange);

        String actual = Logic.viewByPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String sd = dateFormat.format(startDate.getTime());
        String ed = dateFormat.format(endDate.getTime());
        String expected = "Within Period Not Done \n" + sd + " " + ed;
        Logic.delete(id);
        assertEquals(actual, expected);
    }

    /**
     * Test for viewing task within period
     * Condition: Task have only end date
     */
    @Test
    public void viewEndDateInPeriod() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.AUGUST, 10);

        DatePair dp = new DatePair(null, endDate);
        dpList.add(dp);
        long id = Logic.addTask("No Start Date", dpList);

        Calendar startDateRange = new GregorianCalendar();
        startDateRange.set(2014, Calendar.AUGUST, 1);
        Calendar endDateRange = new GregorianCalendar();
        endDateRange.set(2014, Calendar.AUGUST, 30);
        DatePair dpRange = new DatePair(startDateRange, endDateRange);
        ArrayList<DatePair> dpRangeList = new ArrayList<DatePair>();
        dpRangeList.add(dpRange);

        String actual = Logic.viewByPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String ed = dateFormat.format(endDate.getTime());

        String expected = "No Start Date Not Done \n[No Start Date] " + ed;
        Logic.delete(id);
        assertEquals(expected, actual);
    }

    /**
     * Test for viewing task within period
     * Condition: Task have both start date and end date beyond period
     */
    @Test
    public void viewPeriodWithinTaskRange() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.JULY, 10);
        Calendar endDate = Calendar.getInstance();
        endDate.set(2014, Calendar.SEPTEMBER, 20);
        DatePair dp = new DatePair(startDate, endDate);
        dpList.add(dp);
        long id = Logic.addTask("Overlapping Period", dpList);

        Calendar startDateRange = new GregorianCalendar();
        startDateRange.set(2014, Calendar.AUGUST, 1);
        Calendar endDateRange = new GregorianCalendar();
        endDateRange.set(2014, Calendar.AUGUST, 30);
        DatePair dpRange = new DatePair(startDateRange, endDateRange);
        ArrayList<DatePair> dpRangeList = new ArrayList<DatePair>();
        dpRangeList.add(dpRange);

        String actual = Logic.viewByPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String sd = dateFormat.format(startDate.getTime());
        String ed = dateFormat.format(endDate.getTime());
        String expected = "Overlapping Period Not Done \n" + sd + " " + ed;
        Logic.delete(id);
        assertEquals(expected, actual);
    }

    /**
     * Test undo function on Journal
     * Add in a task, and call undo,
     * Expected: Display all should not have any values
     */
    @Test
    public void testJournalUndo() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        int originalSize = Logic.getDB().getValidIdList().size();
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        Logic.undo();

        assertEquals(originalSize, Logic.getDB().getValidIdList().size());
    }

    /**
     * Test redo function on Journal
     * Add in a task, and call undo,
     * Followed by calling redo
     * 
     */
    @Test
    public void testJournalRedo() throws IOException {
        int originalSize = Logic.getDB().getValidIdList().size();

        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Logic.addTask("Test 1", dpList);
        Logic.addTask("Test 2", dpList);
        Logic.undo();
        Logic.redo();

        assertEquals(originalSize + 2, Logic.getDB().getValidIdList().size());
    }

    /**
     * Delete exist task
     *  
     */
    @Test
    public void DeleteExistTask() { //TODO: This test has no meaning. Redo it after Logic.deleteTask is done
        boolean isDeleted = false;

        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        isDeleted = Logic.delete(id);
        assertTrue(isDeleted);
    }

    /**
     * 
     * update the task description
     * @throws IOException 
     *  
     */

    @Test
    public void updateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        Logic.viewAll();
        long newTaskId = Logic.updateTask(1, "Lorem ipsum dolor sit amet.",
                dpList);
        String expected = "Lorem ipsum dolor sit amet. Not Done ";
        String actual = Logic.viewTask(newTaskId);
        assertEquals(expected, actual);
    }

    /**
     * 
     * mark task as completed
     * @throws IOException 
     *  
     */

    @Test
    public void markTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        long taskId = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        Logic.viewAll();
        Long newTaskId = Logic.markTaskcompleted(taskId);
        boolean actual = Logic.getDB().getInstance(newTaskId).getIsDone();
        assertTrue(actual);
    }
}
