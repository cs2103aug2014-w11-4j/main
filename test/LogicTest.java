import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class LogicTest {

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

        Logic.startDatabase();
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar today = Calendar.getInstance();
        DatePair dp = new DatePair(today);
        datePairList.add(dp);
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);
        String actual = Logic.viewTask(id);

        // formatting current dateTime
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String datePair = "";
        dateFormat.setCalendar(dp.getStartDate());
        String startDate = dateFormat.format(dp.getStartDate().getTime());
        dateFormat.setCalendar(dp.getEndDate());
        String endDate = dateFormat.format(dp.getEndDate().getTime());
        datePair = startDate + " " + endDate;

        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done \n"
                + datePair;
        Logic.delete(id);
        assertEquals(expected, actual);

    }

    /**
     * Adding Task without start date / end date
     * 
     * Add a task without specifying any date
     * After retrieving the value, mark it as invalid
     *  
     */

    @Test
    public void addNoDateTask() {
        Logic.startDatabase();
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
    public void addNoStartDateTask() {
        Logic.startDatabase();
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
     * Adding Task with start date without end date 
     * 
     * Adding task without end date
     * After retrieving the value, mark it as invalid
     *  
     */

    @Test
    public void addNoEndDateTask() {
        Logic.startDatabase();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        DatePair dp = new DatePair(Calendar.getInstance(), null);
        dpList.add(dp);
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = Logic.viewTask(id);

        // formatting current dateTime
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        dateFormat.setCalendar(dp.getStartDate());
        String startDate = dateFormat.format(dp.getStartDate().getTime());

        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done \n"
                + startDate + "[No End Date]";
        Logic.delete(id);
        assertEquals(expected, actual);

    }
    
 

 

    /**
     * Test for searching task within period
     * Condition: Task have both start date and end date within period
     */
    @Test
    public void searchWithinPeriodOne() {
        Logic.startDatabase();
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

        String actual = Logic.searchWithPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String sd = dateFormat.format(startDate.getTime());
        String ed = dateFormat.format(endDate.getTime());
        String expected = "Within Period Not Done \n" + sd + " " + ed;
        Logic.delete(id);
        assertEquals(actual, expected);

    }

    /**
     * Test for searching task within period
     * Condition: Task have only start date
     */
    @Test
    public void searchWithinPeriodTwo() {
        Logic.startDatabase();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Calendar startDate = Calendar.getInstance();
        startDate.set(2014, Calendar.AUGUST, 10);

        DatePair dp = new DatePair(startDate, null);
        dpList.add(dp);
        long id = Logic.addTask("No End Date", dpList);

        Calendar startDateRange = new GregorianCalendar();
        startDateRange.set(2014, Calendar.AUGUST, 1);
        Calendar endDateRange = new GregorianCalendar();
        endDateRange.set(2014, Calendar.AUGUST, 30);
        DatePair dpRange = new DatePair(startDateRange, endDateRange);
        ArrayList<DatePair> dpRangeList = new ArrayList<DatePair>();
        dpRangeList.add(dpRange);

        String actual = Logic.searchWithPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String sd = dateFormat.format(startDate.getTime());

        String expected = "No End Date Not Done \n" + sd + "[No End Date]";
        Logic.delete(id);
        assertEquals(expected, actual);

    }

    /**
     * Test for searching task within period
     * Condition: Task have only end date
     */
    @Test
    public void searchWithinPeriodThree() {
        Logic.startDatabase();
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

        String actual = Logic.searchWithPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String ed = dateFormat.format(endDate.getTime());

        String expected = "No Start Date Not Done \n[No Start Date] " + ed;
        Logic.delete(id);
        assertEquals(expected, actual);

    }

    /**
     * Test for searching task within period
     * Condition: Task have both start date and end date beyond period
     */
    @Test
    public void searchWithinPeriodFour() {
        Logic.startDatabase();
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

        String actual = Logic.searchWithPeriod(dpRange);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String sd = dateFormat.format(startDate.getTime());
        String ed = dateFormat.format(endDate.getTime());
        String expected = "Overlapping Period Not Done \n" + sd + " " + ed;
        Logic.delete(id);
        assertEquals(expected, actual);

    }
    
    
    /**
     * Delete exist task
     *  
     */
    @Test
    public void DeleteExistTask() {
        boolean isDeleted = false;
    	Logic.startDatabase();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        isDeleted  = Logic.delete(id);
        assertTrue(isDeleted);
    }
    
    /**
     * Adding Task without start date / end date
     * 
     * update the task description
     *  
     */

    @Test
    public void updateTask() {
        Logic.startDatabase();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        Long newID = Logic.updateTask(id, "Lorem ipsum dolor sit amet.", dpList);
        String actual = Logic.viewTask(newID);
        String expected = "Lorem ipsum dolor sit amet. Not Done ";
        Logic.delete(newID);
        assertEquals(expected, actual);

    }
}
