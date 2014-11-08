import com.rubberduck.command.AddCommand;
import com.rubberduck.command.Command;
import com.rubberduck.command.ConfirmCommand;
import com.rubberduck.command.DeleteCommand;
import com.rubberduck.command.HelpCommand;
import com.rubberduck.command.InvalidCommand;
import com.rubberduck.command.MarkCommand;
import com.rubberduck.command.RedoCommand;
import com.rubberduck.command.SearchCommand;
import com.rubberduck.command.UndoCommand;
import com.rubberduck.command.UpdateCommand;
import com.rubberduck.command.ViewCommand;
import com.rubberduck.logic.DatePair;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CommandTest {
    @Before
    public void setUp() throws IOException {
        Command.startDatabase();
        Command.getDbManager().resetDatabase();
    }

    @After
    public void tearDown() throws IOException {
        Command.getDbManager().closeFile();
    }

    /**
     * Test adding of task with todays date
     *
     * Add a task with todays date and current time
     * Response from command will be check to make sure it produces the appropriate message,
     * Signifying that task added successfully
     */
    //@author A0111794E
    @Test
    public void addTask() throws IOException {
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar today = Calendar.getInstance();
        DatePair dp = new DatePair(today);
        datePairList.add(dp);
        AddCommand command = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        Response s = command.execute();
        String actual = s.getMessages()[0];
        String expected = "[1;33m\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been";
        assertEquals(expected,actual);
    }

    /**
     * Adding Task without start date / end date
     *
     * Add a task without specifying any date After retrieving the value, mark
     * it as invalid
     */
    //@author A0111794E
    @Test
    public void addNoDateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        AddCommand command = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        Response r = command.execute();
        String actual = r.getMessages()[0];
        String expected = "[1;33m\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been";
        assertEquals(expected, actual);
    }

    /**
     * Adding Task with end date without start date
     *
     * Adding Task without a start date After retrieving the value, mark it as
     * invalid
     */
    //@author A0111794E
    @Test
    public void addNoStartDateTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        DatePair dp = new DatePair(Calendar.getInstance());
        dpList.add(dp);
        AddCommand command = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        Response r = command.execute();
        String actual = r.getMessages()[0];
        String expected = "[1;33m\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" has been";
        assertEquals(expected, actual);
    }

    /**
     * add tentative task
     */
    //@author A0119504L
    @Test
    public void addTentativeTask() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        Calendar date3 = Calendar.getInstance();
        Calendar date4 = Calendar.getInstance();
        date3.add(Calendar.DAY_OF_YEAR, 2);
        date4.add(Calendar.DAY_OF_YEAR, 3);
        DatePair dp2 = new DatePair(date3, date4);
        datePairList.add(dp2);

        Calendar date5 = Calendar.getInstance();
        Calendar date6 = Calendar.getInstance();
        date5.add(Calendar.DAY_OF_YEAR, 3);
        date6.add(Calendar.DAY_OF_YEAR, 4);
        DatePair dp3 = new DatePair(date5, date6);
        datePairList.add(dp3);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        String actual = addCommand.execute().getMessages()[0];

        String expected =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        assertTrue(actual.contains(expected));
    }

    /**
     * add task which deadline have passed
     *
     */
    //@author A0119504L
    @Test
    public void addTaskPassed() throws IOException {
		ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
		Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, -5);
        DatePair dp = new DatePair(date);
        datePairList.add(dp);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        String actual = addCommand.execute().getMessages()[1];

        String expected =
            "the end date has already passed.";
        assertTrue(actual.contains(expected));
    }

    /**
     * add conflict task
     * test the warning message came out
     *
     */
    //@author A0119504L
    @Test
    public void addConflictTask() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
		Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        ArrayList<DatePair> datePairList2 = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);
        datePairList2.add(dp);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();
        AddCommand addCommand2 = new AddCommand(
            "Lonsectetur adipiscing elit.",
            datePairList2);
        String actual = addCommand2.execute().getMessages()[2];

        String expected =
            "Please note that there are conflicting schedule(s)";
        assertTrue(actual.contains(expected));
    }

    /**
     * add wrong task type with multiple deadlines
     */
    //@author A0119504L
    @Test
    public void addTaskWrongType() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
		Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date);
        DatePair dp2 = new DatePair(date2);
        datePairList.add(dp);
        datePairList.add(dp2);

        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);
        String actual = addCommand.execute().getMessages()[0];

        String expected =
                "You have input an invalid task type.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Search for keyword in description
     */
    //@author A0111794E
    @Test
    public void searchKeywordTest() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
		Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        SearchCommand searchCommand = new SearchCommand("Lorem");
        String actual = searchCommand.execute().getViewCount();
        String expected = "1 task with \"Lorem\" has been found.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Search for exact keyword in description
     */
    //@author A0111794E
    @Test
    public void searchExactKeywordTest() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. #test",
            dpList);
        addCommand.execute();
        addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. test",
                dpList);
        addCommand.execute();

        SearchCommand searchCommand = new SearchCommand("\"#test\"");
        String actual = searchCommand.execute().getViewCount();
        String expected = "[1;32m1 task with \"#test\" has been found.[m";
        assertEquals(expected, actual);
    }

    /**
     * Search for keyword not exist
     */
    //@author A0119504L
    @Test
    public void searchKeywordNotExistTest() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
		Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        SearchCommand searchCommand = new SearchCommand("apple");
        String actual = searchCommand.execute().getViewCount();
        String expected = "0 task with \"apple\" has been found.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Search for keyword not exist
     * Applying new enchance search function where description are tokenie before being searched.
     */
    //@author A0111794E
    @Test
    public void searchKeywordNotExistTest2() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        SearchCommand searchCommand = new SearchCommand("r a");
        String actual = searchCommand.execute().getViewCount();
        String expected = "0 task with \"r a\" has been found.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Test undo function on Journal Add in a task, and call undo, Expected:
     * Display all should not have any values
     */
    //@author A0111794E
    @Test
    public void testJournalUndo() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        int originalSize = Command.getDbManager().getValidIdList().size();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        UndoCommand undoCommand = new UndoCommand();
        undoCommand.execute();

        assertEquals(originalSize, Command.getDbManager()
            .getValidIdList()
            .size());
    }

    /**
     * Test redo function on Journal Add in a task, and call undo, Followed by
     * calling redo
     */
    //@author A0111794E
    @Test
    public void testJournalRedo() throws IOException {
        int originalSize = Command.getDbManager().getValidIdList().size();

        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand("Test 1", dpList);
        addCommand.execute();
        addCommand = new AddCommand("Test 2", dpList);
        addCommand.execute();

        UndoCommand undoCommand = new UndoCommand();
        undoCommand.execute();

        RedoCommand redoCommand = new RedoCommand();
        redoCommand.execute();

        assertEquals(originalSize + 2, Command.getDbManager()
            .getValidIdList()
            .size());
    }

    /**
     * Test help command
     */
    //@author A0119504L
    @Test
    public void helpTest() throws IOException {
        HelpCommand command = new HelpCommand(false, null);
        String actual = command.execute().getMessages()[0];

        String expected = "Here are for the available commands in RubberDuck.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Test specific help command
     */
    //@author A0119504L
    @Test
    public void helpSpecificTest() throws IOException {
        HelpCommand command = new HelpCommand(true, "add");
        String actual = command.execute().getMessages()[0];
        String expected = "More information about your queried command.";
        assertTrue(actual.contains(expected));
    }

    /**
     * Test invalid help command
     */
    //@author A0119504L
    @Test
    public void helpInvalidCommandTest() throws IOException {
        HelpCommand command = new HelpCommand(true, "abc");
        String actual = command.execute().getMessages()[0];
        String expected = "No such command/alias.";
        assertTrue(actual.contains(expected));

    }

    /**
     * Test invalid command
     */
    //@author A0119504L
    @Test
    public void InvalidCommandTest() throws IOException {
        InvalidCommand command = new InvalidCommand("abc");
        String actual = command.execute().getMessages()[0];
        String expected = "abc";
        assertTrue(actual.contains(expected));

    }

    /**
     * Delete exist task
     */
    //@author A0119504L
    @Test
    public void deleteExistTask() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        DeleteCommand deleteCommand = new DeleteCommand(1);
        deleteCommand.execute();
        assertEquals(0, Command.getDbManager().getValidIdList().size());
    }

    /**
     * Delete not exist task
     */
    //@author A0119504L
    @Test
    public void deleteNotExistTask() throws IOException {
    	ArrayList<ViewCommand.ViewFilter> viewChoice = new ArrayList<ViewCommand.ViewFilter>();
		viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
		viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
		viewChoice.add(ViewCommand.ViewFilter.TASK);
		ViewCommand viewCommand = new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
		viewCommand.execute();
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        DeleteCommand deleteCommand = new DeleteCommand(2);

        String expected = deleteCommand.execute().getMessages()[0];
        assertTrue(expected.contains( ColorFormatter.
            format("This is not a valid task ID to delete.", Color.RED)));
    }

    /**
     * update the task description
     */
    //@author A0119504L
    @Test
    public void updateTaskDescription() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);

        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(1,
                                                        "Lorem ipsum dolor sit amet.",
                                                        dpList);
        updateCommand.execute();
        String actual = Command.getDbManager()
            .getInstance(Command.getDisplayedTasksList().get(0))
            .getDescription();
        String expected = "Lorem ipsum dolor sit amet.";
        assertEquals(expected, actual);
    }

    /**
     * update the task dateList
     */
    //@author A0119504L
    @Test
    public void updateTaskDate() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        addCommand.execute();

        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp2 = new DatePair(date, date2);
        datePairList.add(dp2);

        UpdateCommand updateCommand = new UpdateCommand(1,
                                                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                                        datePairList);
        updateCommand.execute();
        String actual = Command.getDbManager()
            .getInstance(Command.getDisplayedTasksList().get(0)).getDateList()
            .toString();
        String expected = datePairList.toString();
        assertEquals(expected, actual);
    }

    /**
     * update the task does not exist
     */
    //@author A0119504L
    @Test
    public void updateTaskNotExist() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        addCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(2,
                                                        "Lorem ipsum dolor sit amet.",
                                                        dpList);
        String expected = updateCommand.execute().getMessages()[0];
        String actual = ColorFormatter.format("You have input an invalid ID.",
                                              Color.RED);

        assertTrue(expected.contains(actual));
    }

    /**
     * update to wrong task type with multiple deadlines
     */
    //@author A0119504L
    @Test
    public void updateTaskWrongType() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date);
        DatePair dp2 = new DatePair(date2);
        datePairList.add(dp);
        datePairList.add(dp2);

        UpdateCommand updateCommand = new UpdateCommand(1,
                                                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                                        datePairList);
        String actual = updateCommand.execute().getMessages()[0];
        String expected =
            ColorFormatter.format("You have input an invalid task type.",
                                  Color.RED);

        assertTrue(actual.contains(expected));
    }

    /**
     * update task date which has passed
     */
    //@author A0119504L
    @Test
    public void updateTaskPassed() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_MONTH, -5);
        DatePair dp = new DatePair(date);
        datePairList.add(dp);

        UpdateCommand updateCommand = new UpdateCommand(1,
                                                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                                                        datePairList);
        String actual = updateCommand.execute().getMessages()[0];
        String expected = ColorFormatter.format(
            "You cannot update the end date that has already passed.",
            Color.RED);
        assertEquals(expected, actual);
    }

    /**
     * update task with conflict date
     */
    //@author A0119504L
    @Test
    public void updateTaskConflict() throws IOException {

        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);

        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        ArrayList<DatePair> datePairList2 = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();
        AddCommand addCommand2 = new AddCommand(
            "Lonsectetur adipiscing elit.",
            datePairList2);
        addCommand2.execute();
        datePairList2.add(dp);
        UpdateCommand updateCommand = new UpdateCommand(2,
                                                        "Lorem ipsum dolor sit amet.",
                                                        datePairList2);
        String actual = updateCommand.execute().getMessages()[1];
        String expected =
            "Please note that there are conflicting schedule(s).";
        assertTrue(actual.contains(expected));
    }

    /**
     * mark task as completed
     */
    //@author A0119504L
    @Test
    public void markTaskUncompleted() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        MarkCommand markCommand = new MarkCommand(1);
        markCommand.execute();

        ViewCommand viewCommandComplete =
            new ViewCommand(ViewCommand.ViewType.ALL, true, null, viewChoice);
        viewCommandComplete.execute();

        MarkCommand markCommandAgain = new MarkCommand(1);
        markCommandAgain.execute();

        ViewCommand viewCommandAgain =
            new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommandAgain.execute();

        boolean actual = Command.getDbManager()
            .getInstance(Command.getDisplayedTasksList().get(0))
            .getIsDone();
        boolean expected = false;
        assertEquals(actual, expected);
    }

    /**
     * mark task as completed
     */
    //@author A0119504L
    @Test
    public void markTaskCompleted() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();

        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();

        MarkCommand markCommand = new MarkCommand(1);
        markCommand.execute();

        ViewCommand viewCommandComplete =
            new ViewCommand(ViewCommand.ViewType.ALL, true, null, viewChoice);
        viewCommandComplete.execute();

        boolean actual = Command.getDbManager()
            .getInstance(Command.getDisplayedTasksList().get(0))
            .getIsDone();
        boolean expected = true;
        assertEquals(actual, expected);
    }

    /**
     * mark task does not exist
     */
    //@author A0119504L
    @Test
    public void markTaskNotExist() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            dpList);
        addCommand.execute();
        MarkCommand markCommand = new MarkCommand(2);
        String actual = markCommand.execute().getMessages()[0];

        String expected = ColorFormatter.
            format("You have input an invalid ID.", Color.RED);
        assertTrue(actual.contains(expected));
    }

    /**
     * confirm task
     */
    //@author A0119504L
    @Test
    public void confirmTask() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
            Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        Calendar date3 = Calendar.getInstance();
        Calendar date4 = Calendar.getInstance();
        date3.add(Calendar.DAY_OF_YEAR, 2);
        date4.add(Calendar.DAY_OF_YEAR, 3);
        DatePair dp2 = new DatePair(date3, date4);
        datePairList.add(dp2);

        Calendar date5 = Calendar.getInstance();
        Calendar date6 = Calendar.getInstance();
        date5.add(Calendar.DAY_OF_YEAR, 3);
        date6.add(Calendar.DAY_OF_YEAR, 4);
        DatePair dp3 = new DatePair(date5, date6);
        datePairList.add(dp3);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();

        ConfirmCommand confirmCommand = new ConfirmCommand(1, 2);
        confirmCommand.execute();

        String actual = Command.getDbManager()
            .getInstance(Command.getDisplayedTasksList().get(0))
            .getDateList()
            .get(0)
            .toString();
        String expected = dp2.toString();

        assertEquals(expected, actual);
    }

    /**
     * confirm task which does not exist
     */
    //@author A0119504L
    @Test
    public void confirmTaskNotExist() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
            Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        Calendar date3 = Calendar.getInstance();
        Calendar date4 = Calendar.getInstance();
        date3.add(Calendar.DAY_OF_YEAR, 2);
        date4.add(Calendar.DAY_OF_YEAR, 3);
        DatePair dp2 = new DatePair(date3, date4);
        datePairList.add(dp2);

        Calendar date5 = Calendar.getInstance();
        Calendar date6 = Calendar.getInstance();
        date5.add(Calendar.DAY_OF_YEAR, 3);
        date6.add(Calendar.DAY_OF_YEAR, 4);
        DatePair dp3 = new DatePair(date5, date6);
        datePairList.add(dp3);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();

        ConfirmCommand confirmCommand = new ConfirmCommand(2, 2);
        String actual = confirmCommand.execute().getMessages()[0];

        String expected = ColorFormatter.
            format("You have input an invalid task ID.", Color.RED);

        assertTrue(actual.contains(expected));
    }

    /**
     * confirm task not tentative
     */
    //@author A0119504L
    @Test
    public void confirmTaskNotTentative() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
            Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();

        ConfirmCommand confirmCommand = new ConfirmCommand(1, 2);
        String actual = confirmCommand.execute().getMessages()[0];


        String expected ="is not tentative";

        assertTrue(actual.contains(expected));
    }

    /**
     * confirm task invalid date id
     */
    //@author A0119504L
    @Test
    public void confirmTaskInvalidDateId() throws IOException {
        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
            viewCommand.execute();
            Command.setPreviousDisplayCommand(viewCommand);
        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        Calendar date3 = Calendar.getInstance();
        Calendar date4 = Calendar.getInstance();
        date3.add(Calendar.DAY_OF_YEAR, 2);
        date4.add(Calendar.DAY_OF_YEAR, 3);
        DatePair dp2 = new DatePair(date3, date4);
        datePairList.add(dp2);

        Calendar date5 = Calendar.getInstance();
        Calendar date6 = Calendar.getInstance();
        date5.add(Calendar.DAY_OF_YEAR, 3);
        date6.add(Calendar.DAY_OF_YEAR, 4);
        DatePair dp3 = new DatePair(date5, date6);
        datePairList.add(dp3);

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();

        ConfirmCommand confirmCommand = new ConfirmCommand(1, 4);
        String actual = confirmCommand.execute().getMessages()[0];

        String expected = ColorFormatter.
            format("You have input an invalid date ID.", Color.RED);

        assertTrue(actual.contains(expected));
    }

    /**
     * confirm task with conflict date
     */
    //@author A0119504L
    @Test
    public void confirmTaskConflict() throws IOException {

        ArrayList<ViewCommand.ViewFilter> viewChoice =
            new ArrayList<ViewCommand.ViewFilter>();
        viewChoice.add(ViewCommand.ViewFilter.DEADLINE);
        viewChoice.add(ViewCommand.ViewFilter.SCHEDULE);
        viewChoice.add(ViewCommand.ViewFilter.TASK);
        ViewCommand viewCommand =
                new ViewCommand(ViewCommand.ViewType.ALL, false, null, viewChoice);
        viewCommand.execute();
        Command.setPreviousDisplayCommand(viewCommand);

        ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
        ArrayList<DatePair> datePairList2 = new ArrayList<DatePair>();
        Calendar date = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();

        date.add(Calendar.DAY_OF_YEAR, 1);
        date2.add(Calendar.DAY_OF_YEAR, 2);
        DatePair dp = new DatePair(date, date2);
        datePairList.add(dp);

        Calendar date3 = Calendar.getInstance();
        Calendar date4 = Calendar.getInstance();
        date3.add(Calendar.DAY_OF_YEAR, 2);
        date4.add(Calendar.DAY_OF_YEAR, 3);
        DatePair dp2 = new DatePair(date3, date4);
        datePairList.add(dp2);

        Calendar date5 = Calendar.getInstance();
        Calendar date6 = Calendar.getInstance();
        date5.add(Calendar.DAY_OF_YEAR, 3);
        date6.add(Calendar.DAY_OF_YEAR, 4);
        DatePair dp3 = new DatePair(date5, date6);
        datePairList.add(dp3);

        datePairList2.add(dp2);

        AddCommand addCommand2 = new AddCommand(
                "Lonsectetur adipiscing elit.",
                datePairList2);
        addCommand2.execute();

        AddCommand addCommand = new AddCommand(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
            datePairList);
        addCommand.execute();

        datePairList2.add(dp);
        ConfirmCommand confirmCommand = new ConfirmCommand(1, 2);
        String actual = confirmCommand.execute().getMessages()[2];
        String expected =
            "Please note that there are conflicting schedule(s).";
        assertTrue(actual.contains(expected));
    }

}
