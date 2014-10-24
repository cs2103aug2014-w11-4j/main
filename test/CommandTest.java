import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommandTest {

    @Before
    public void setUp() throws IOException {
        Command.startDatabase();
        Command.getDB().resetDatabase();
    }

    @After
    public void tearDown() throws IOException {
        Command.getDB().closeFile();
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
        AddCommand command = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);
        command.execute();
        assertEquals(1, Command.getDB().getValidIdList().size());
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
        String keyword = "Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        String actual = "";
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand command = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        command.execute();

        for (Long databaseId : Command.getDB().getValidIdList()) {
            String taskInDb = Command.getDB()
                    .getInstance(databaseId)
                    .getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keyword.toLowerCase())) {
                actual = taskInDb;
            }
        }
        String expected = "lorem ipsum dolor sit amet, consectetur adipiscing elit.";
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
        String actual = "";
        DatePair dp = new DatePair(null, Calendar.getInstance());
        dpList.add(dp);
        String keyword = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        AddCommand command = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        command.execute();

        for (Long databaseId : Command.getDB().getValidIdList()) {
            String taskInDb = Command.getDB()
                    .getInstance(databaseId)
                    .getDescription();
            taskInDb = taskInDb.toLowerCase();
            if (taskInDb.contains(keyword.toLowerCase())) {
                actual = taskInDb;
            }
        }

        String expected = "lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        assertEquals(expected, actual);
    }

    /**
     * Search for keyword in description
     *
     */
    @Test
    public void searchKeywordTest() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();

        SearchCommand searchCommand = new SearchCommand("Lorem");
        String actual = searchCommand.execute();
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
        int originalSize = Command.getDB().getValidIdList().size();

        AddCommand addCommand = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();

        UndoCommand undoCommand = new UndoCommand();
        undoCommand.execute();

        assertEquals(originalSize, Command.getDB().getValidIdList().size());
    }

    /**
     * Test redo function on Journal Add in a task, and call undo, Followed by
     * calling redo
     *
     */
    @Test
    public void testJournalRedo() throws IOException {
        int originalSize = Command.getDB().getValidIdList().size();

        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand("Test 1", dpList);
        addCommand.execute();
        addCommand = new AddCommand("Test 2", dpList);
        addCommand.execute();

        UndoCommand undoCommand = new UndoCommand();
        undoCommand.execute();

        RedoCommand redoCommand = new RedoCommand();
        redoCommand.execute();

        assertEquals(originalSize + 2, Command.getDB().getValidIdList().size());
    }

    /**
     * Delete exist task
     * @throws IOException
     *
     */
    @Test
    public void DeleteExistTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        AddCommand addCommand = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null);
        viewCommand.execute();
        DeleteCommand deleteCommand = new DeleteCommand(1);
        String expected = deleteCommand.execute();
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
        AddCommand addCommand = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null);
        viewCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(1, "Lorem ipsum dolor sit amet.",
                dpList);
        String actual = updateCommand.execute();
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
        AddCommand addCommand = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null);
        viewCommand.execute();

        MarkCommand markCommand = new MarkCommand(1);
        String expected = markCommand.execute();
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
		AddCommand addCommand = new AddCommand(
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
				dpList);
		addCommand.execute();
		ViewCommand viewCommand = new ViewCommand(true, false, null);
		viewCommand.execute();
		
		MarkCommand markCommand = new MarkCommand(1);
		markCommand.execute();
		
		String expected = markCommand.execute();
		String actual = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been marked to uncompleted.";
		assertEquals(actual, expected);
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
    public void confirmTask() throws IOException {
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 1);
        DatePair dp = new DatePair(date);
        datePairList.add(dp);

        Calendar date2 = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp2 = new DatePair(date2);
        datePairList.add(dp2);

        Calendar date3 = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 3);
        DatePair dp3 = new DatePair(date3);
        datePairList.add(dp3);

        AddCommand addCommand = new AddCommand("Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null);
        viewCommand.execute();
        ConfirmCommand confirmCommand = new ConfirmCommand(1, 2);
        String actual = confirmCommand.execute();

        String expected = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been confirmed.";

        assertEquals(expected, actual);
    }

}
