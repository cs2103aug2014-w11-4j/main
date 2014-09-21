import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

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
     * Search for keyword in description
     * 
     */
    @Test
    public void searchKeywordTest(){
        Logic.startDatabase();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
               
        Long id = Logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        System.out.println(id);
        String actual = Logic.searchWithKeyword("Lorem");
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done ";
        Logic.delete(id);
        assertEquals(expected, actual);
        
        
        
    }

}
