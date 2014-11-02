import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.rubberduck.command.*;
import com.rubberduck.logic.DatePair;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

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
        AddCommand command = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                datePairList);
        command.execute();
        assertEquals(1, Command.getDbManager().getValidIdList().size());
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

        AddCommand command = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        command.execute();

        for (Long databaseId : Command.getDbManager().getValidIdList()) {
            String taskInDb = Command.getDbManager()
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
        DatePair dp = new DatePair(Calendar.getInstance());
        dpList.add(dp);
        String keyword = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        AddCommand command = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        command.execute();

        for (Long databaseId : Command.getDbManager().getValidIdList()) {
            String taskInDb = Command.getDbManager()
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

        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
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
     *
     */
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
     * Delete exist task
     *
     * @throws IOException
     *
     */
    @Test
    public void deleteExistTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
        DeleteCommand deleteCommand = new DeleteCommand(1);
        deleteCommand.execute();
        assertEquals(0, Command.getDbManager().getValidIdList().size());
    }

    /**
     * Delete not exist task
     *
     * @throws IOException
     *
     */
    @Test
    public void deleteNotExistTask() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
        DeleteCommand deleteCommand = new DeleteCommand(2);
        String expected = deleteCommand.execute();
        assertEquals(expected, ColorFormatter.
                format("This is not a valid task ID to delete.", Color.RED));
    }
    
    /**
     *
     * update the task description
     *
     * @throws IOException
     *
     */
    @Test
    public void updateTaskDescription() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
        UpdateCommand updateCommand = new UpdateCommand(1,
                "Lorem ipsum dolor sit amet.", dpList);
        updateCommand.execute();
        String actual = Command.getDbManager()
                .getInstance(Command.getDisplayedTasksList().get(0))
                .getDescription();
        String expected = "Lorem ipsum dolor sit amet.";
        assertEquals(expected, actual);
    }
    
    /**
    *
    * update the task dateList
    *
    * @throws IOException
    *
    */
   @Test
   public void updateTaskDate() throws IOException {
       ArrayList<DatePair> dpList = new ArrayList<DatePair>();
       ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
       viewChoice.add(ViewCommand.ViewType.DEADLINE);
       viewChoice.add(ViewCommand.ViewType.SCHEDULE);
       viewChoice.add(ViewCommand.ViewType.TASK);
       AddCommand addCommand = new AddCommand(
               "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
               dpList);
       addCommand.execute();
       ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
       viewCommand.execute();
       
       ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
       Calendar date = Calendar.getInstance();
       Calendar date2 = Calendar.getInstance();
       date.add(Calendar.DAY_OF_YEAR, 1);
       date2.add(Calendar.DAY_OF_YEAR, 2);
       DatePair dp2 = new DatePair(date, date2);
       datePairList.add(dp2);
       
       UpdateCommand updateCommand = new UpdateCommand(1,
    		   "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", datePairList);
       updateCommand.execute();
       String actual = Command.getDbManager()
               .getInstance(Command.getDisplayedTasksList().get(0)).getDateList().toString();
       String expected = datePairList.toString();
       assertEquals(expected, actual);
   }

   	/**
	 *
	 * update the task does not exist
	 *
	 * @throws IOException
	 *
	 */
	@Test
	public void updateTaskNotExist() throws IOException {
		ArrayList<DatePair> dpList = new ArrayList<DatePair>();
		ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
		viewChoice.add(ViewCommand.ViewType.DEADLINE);
		viewChoice.add(ViewCommand.ViewType.SCHEDULE);
		viewChoice.add(ViewCommand.ViewType.TASK);
		AddCommand addCommand = new AddCommand(
				"Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
				dpList);
		addCommand.execute();
		ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
		viewCommand.execute();
		UpdateCommand updateCommand = new UpdateCommand(2,
				"Lorem ipsum dolor sit amet.", dpList);
		String expected = updateCommand.execute();
		String actual = ColorFormatter.format("You have input an invalid ID.",
                Color.RED);
		assertEquals(expected, actual);
	}
	
    /**
    *
    * update to wrong task type with multiple deadlines
    *
    * @throws IOException
    *
    */
   @Test
   public void updateTaskWrongType() throws IOException {
       ArrayList<DatePair> dpList = new ArrayList<DatePair>();
       ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
       viewChoice.add(ViewCommand.ViewType.DEADLINE);
       viewChoice.add(ViewCommand.ViewType.SCHEDULE);
       viewChoice.add(ViewCommand.ViewType.TASK);
       AddCommand addCommand = new AddCommand(
               "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
               dpList);
       addCommand.execute();
       ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
       viewCommand.execute();
       
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
    		   "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", datePairList);
       String actual = updateCommand.execute();
       String expected = ColorFormatter.format("You have input an invalid task type.",
               Color.RED);
       assertEquals(expected, actual);
   }
   
   /**
   *
   * update task date which has passed
   *
   * @throws IOException
   *
   */
  @Test
  public void updateTaskPassed() throws IOException {
      ArrayList<DatePair> dpList = new ArrayList<DatePair>();
      ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
      viewChoice.add(ViewCommand.ViewType.DEADLINE);
      viewChoice.add(ViewCommand.ViewType.SCHEDULE);
      viewChoice.add(ViewCommand.ViewType.TASK);
      AddCommand addCommand = new AddCommand(
              "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
              dpList);
      addCommand.execute();
      ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
      viewCommand.execute();
      
      ArrayList<DatePair> datePairList = new ArrayList<DatePair>();
      Calendar date = Calendar.getInstance();
      date.add(Calendar.DAY_OF_MONTH, -5);
      DatePair dp = new DatePair(date);
      datePairList.add(dp);
      
      UpdateCommand updateCommand = new UpdateCommand(1,
   		   "Lorem ipsum dolor sit amet, consectetur adipiscing elit.", datePairList);
      String actual = updateCommand.execute();
      String expected = ColorFormatter.format("You cannot update the end date that has already passed.",
              Color.RED);
      assertEquals(expected, actual);
  }
   
    /**
     *
     * mark task as completed
     *
     * @throws IOException
     *
     */

    @Test
    public void markTaskUncompleted() throws IOException {
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();

        MarkCommand markCommand = new MarkCommand(1);
        markCommand.execute();

        ViewCommand viewCommandComplete = new ViewCommand(true, true, null, viewChoice);
        viewCommandComplete.execute();

        MarkCommand markCommandAgain = new MarkCommand(1);
        markCommandAgain.execute();

        ViewCommand viewCommandAgain = new ViewCommand(true, false, null, viewChoice);
        viewCommandAgain.execute();

        boolean actual = Command.getDbManager()
                .getInstance(Command.getDisplayedTasksList().get(0))
                .getIsDone();
        boolean expected = false;
        assertEquals(actual, expected);
    }

    /**
     *
     * mark task as completed
     *
     * @throws IOException
     *
     */
    @Test
    public void markTaskCompleted() throws IOException {
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
        ArrayList<DatePair> dpList = new ArrayList<DatePair>();
        AddCommand addCommand = new AddCommand(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                dpList);
        addCommand.execute();
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();

        MarkCommand markCommand = new MarkCommand(1);
        markCommand.execute();

        ViewCommand viewCommandComplete = new ViewCommand(true, true, null, viewChoice);
        viewCommandComplete.execute();

        boolean actual = Command.getDbManager()
                .getInstance(Command.getDisplayedTasksList().get(0))
                .getIsDone();
        boolean expected = true;
        assertEquals(actual, expected);
    }

    /**
    *
    * mark task does not exist
    *
    * @throws IOException
    *
    */
   @Test
   public void markTaskNotExist() throws IOException {
       ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
       viewChoice.add(ViewCommand.ViewType.DEADLINE);
       viewChoice.add(ViewCommand.ViewType.SCHEDULE);
       viewChoice.add(ViewCommand.ViewType.TASK);
       ArrayList<DatePair> dpList = new ArrayList<DatePair>();
       AddCommand addCommand = new AddCommand(
               "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
               dpList);
       addCommand.execute();
       ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
       viewCommand.execute();

       MarkCommand markCommand = new MarkCommand(2);
       String actual = markCommand.execute();

       String expected = ColorFormatter.
               format("You have input an invalid ID.", Color.RED);
       assertEquals(actual, expected);
   }
   
    /**
 	 * confirm task
 	 * @throws IOException
     *
     */
    @Test
    public void confirmTask() throws IOException {
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
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
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
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
 	 * @throws IOException
     *
     */
    @Test
    public void confirmTaskNotExist() throws IOException {
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
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
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
        ConfirmCommand confirmCommand = new ConfirmCommand(2, 2);
        String actual = confirmCommand.execute();

        String expected = ColorFormatter.
                format("You have input an invalid task ID.", Color.RED);

        assertEquals(expected, actual);
    }
    
    /**
 	 * confirm task not tentative
 	 * @throws IOException
     *
     */
    @Test
    public void confirmTaskNotTentative() throws IOException {
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
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
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
        ConfirmCommand confirmCommand = new ConfirmCommand(1, 2);
        String actual = confirmCommand.execute();

        String expected = ColorFormatter.
                format("\"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\" is not tentative and does not need confirmation.", Color.RED);

        assertEquals(expected, actual);
    }
    
    /**
 	 * confirm task invalid date id
 	 * @throws IOException
     *
     */
    @Test
    public void confirmTaskInvalidDateId() throws IOException {
        ArrayList<ViewCommand.ViewType> viewChoice = new ArrayList<ViewCommand.ViewType>();
        viewChoice.add(ViewCommand.ViewType.DEADLINE);
        viewChoice.add(ViewCommand.ViewType.SCHEDULE);
        viewChoice.add(ViewCommand.ViewType.TASK);
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
        ViewCommand viewCommand = new ViewCommand(true, false, null, viewChoice);
        viewCommand.execute();
        ConfirmCommand confirmCommand = new ConfirmCommand(1, 4);
        String actual = confirmCommand.execute();

        String expected = ColorFormatter.
                format("You have input an invalid date ID.", Color.RED);

        assertEquals(expected, actual);
    }
    
}
