import static org.junit.Assert.*;

import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogicTest {
    public Logic logic;

    @Before
    public void setUp() throws IOException {
        logic = Logic.getInstance();
        logic.getDB().resetDatabase();
    }

    @After
    public void tearDown() throws IOException {
        logic.getDB().resetDatabase();
    }

    /**
     * Test adding of task with todays date
     *
     * Add a task with todays date and current runtime Call display to display
     * specified task via id Store both actual and expected values Mark recent
     * created task as invalid Execute comparison
     *
     */
    @Test
    public void addTask() throws IOException {
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar today = Calendar.getInstance();
        DatePair dp = new DatePair(today);
        datePairList.add(dp);
        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);

        assertEquals(1, logic.getDB().getValidIdList().size());
    }

    /**
     * Adding Task without start date / end date
     *
     * Add a task without specifying any date After retrieving the value, mark
     * it as invalid
     *
     */
    @Test
    public void addNoDateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        Long id = logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = logic.viewTask(id);
        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done ";
        assertEquals(expected, actual);
    }

    /**
     * Adding Task with end date without start date
     *
     * Adding Task without a start date After retrieving the value, mark it as
     * invalid
     *
     */
    @Test
    public void addNoStartDateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        DatePair dp = new DatePair(null, Calendar.getInstance());
        dpList.add(dp);
        Long id = logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = logic.viewTask(id);

        // formatting current dateTime
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        dateFormat.setCalendar(dp.getEndDate());
        String endDate = dateFormat.format(dp.getEndDate().getTime());

        String expected = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Not Done \n[No Start Date] "
                + endDate;
        assertEquals(expected, actual);
    }

    /**
     * Search for keyword in description
     *
     */
    @Test
    public void searchKeywordTest() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        String actual = logic.searchWithKeyword("Lorem");
        String expected = "1 task with \"Lorem\" has been found.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Test undo function on Journal Add in a task, and call undo, Expected:
     * Display all should not have any values
     */
    @Test
    public void testJournalUndo() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        int originalSize = logic.getDB().getValidIdList().size();
        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        logic.undo();

        assertEquals(originalSize, logic.getDB().getValidIdList().size());
    }

    /**
     * Test redo function on Journal Add in a task, and call undo, Followed by
     * calling redo
     *
     */
    @Test
    public void testJournalRedo() throws IOException {
        int originalSize = logic.getDB().getValidIdList().size();

        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        logic.addTask("Test 1", dpList);
        logic.addTask("Test 2", dpList);
        logic.undo();
        logic.redo();

        assertEquals(originalSize + 2, logic.getDB().getValidIdList().size());
    }

    /**
     * Delete exist task
     * @throws IOException
     *
     */
    @Test
    public void DeleteExistTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        logic.viewAll(false);
        String expected = logic.deleteTask(1);
        String actual = ("\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been successfully deleted.");
        assertEquals(actual, expected);
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
        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        logic.viewAll(false);
        String actual = logic.updateTask(1, "Lorem ipsum dolor sit amet.",
                dpList);
        String expected = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been successfully updated.";
        assertEquals(expected, actual);
    }

    /**
     *
     * mark task as completed
     * @throws IOException
     *
     */

    @Test
    public void markTaskCompleted() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        logic.viewAll(false);
        String expected = logic.markTaskCompleted(1);
        String actual = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been marked to completed.";
        assertEquals(actual, expected);
    }

    /**
     *
     * mark task as completed
     * @throws IOException
     *
     */
    @Test
    public void markTaskUncompleted() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        logic.addTask(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        logic.viewAll(false);
        logic.markTaskCompleted(1);
        String expected = logic.markTaskUncompleted(1);
        String actual = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been marked to uncompleted.";
        assertEquals(actual, expected);
    }

}
