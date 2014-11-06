import com.rubberduck.command.AddCommand;
import com.rubberduck.command.Command;
import com.rubberduck.command.ConfirmCommand;
import com.rubberduck.command.DeleteCommand;
import com.rubberduck.command.ExitCommand;
import com.rubberduck.command.InvalidCommand;
import com.rubberduck.command.MarkCommand;
import com.rubberduck.command.RedoCommand;
import com.rubberduck.command.SearchCommand;
import com.rubberduck.command.UndoCommand;
import com.rubberduck.command.UpdateCommand;
import com.rubberduck.command.ViewCommand;
import com.rubberduck.logic.Parser;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Test Cases used in Unit Testing for Parser class
 */
//@author A0111736M
public class ParserTest {

    public Parser parser;

    @Before
    public void setParser() {
        parser = Parser.getInstance();
    }

    @Test
    public void parseView() {
        /* Testing for arguments (number and validity) in input */
        /* Boundary case for no argument partition */
        String noArgument = "view";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to argument", true,
                     noCommand instanceof InvalidCommand);

        /* Boundary case for date only partition */
        String dateArgument = "view 25 Oct";
        Command dateCommand = parser.parse(dateArgument);
        assertEquals("must be VIEW command", true,
                     dateCommand instanceof ViewCommand);
        assertEquals("must have a view date", true,
                     ((ViewCommand) dateCommand).getViewRange().hasEndDate());

        /* Boundary case for date range partition */
        String rangeArgument = "view 25 Oct - 30 Oct";
        Command rangeCommand = parser.parse(rangeArgument);
        assertEquals("must be VIEW command", true,
                     rangeCommand instanceof ViewCommand);
        assertEquals("must have a view range", true,
                     ((ViewCommand) rangeCommand).getViewRange()
                         .hasDateRange());

        /* Boundary case for 'all' partition */
        String allArgument = "view all";
        Command allCommand = parser.parse(allArgument);
        assertEquals("must be VIEW command", true,
                     allCommand instanceof ViewCommand);
        assertEquals("boolean for viewAll should be true", true,
                     ((ViewCommand) allCommand).isViewAll());

        /* Boundary case for other String input partition */
        String otherArgument = "view randomstring";
        Command otherCommand = parser.parse(otherArgument);
        assertEquals("will return INVALID command due to no valid argument",
                     true, otherCommand instanceof InvalidCommand);

        /* Testing for different alias with valid input */
        assertEquals(true,
                     parser.parse("view 25 oct 2014") instanceof ViewCommand);
        assertEquals(true,
                     parser.parse("display this week") instanceof ViewCommand);
    }

    @Test
    public void parseSearch() {
        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
        String noArgument = "search";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no argument", true,
                     noCommand instanceof InvalidCommand);

        /* Boundary case for valid argument (String) partition */
        String stringArgument = "search meeting with boss";
        Command stringCommand = parser.parse(stringArgument);
        assertEquals("must be SEARCH command", true,
                     stringCommand instanceof SearchCommand);
        assertEquals("keyword must be properly stored", "meeting with boss",
                     ((SearchCommand) stringCommand).getKeyword());

        /* Testing for different alias with valid input */
        assertEquals(true, parser.parse("search me") instanceof SearchCommand);
        assertEquals(true,
                     parser.parse("find urgent") instanceof SearchCommand);
        assertEquals(true, parser.parse("lookup hw") instanceof SearchCommand);
    }

    @Test
    public void parseAdd() {
        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
        String noArgument = "add";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no argument", true,
                     noCommand instanceof InvalidCommand);

        /* Boundary case for date only partition */
        String dateArgument = "add today";
        Command dateCommand = parser.parse(dateArgument);
        assertEquals("will return INVALID command due to lack of description",
                     true, dateCommand instanceof InvalidCommand);

        /* Boundary case for description only partition */
        String descArgument = "add meeting";
        Command descCommand = parser.parse(descArgument);
        assertEquals("must be ADD command", true,
                     descCommand instanceof AddCommand);
        assertEquals("description must be assigned", "meeting",
                     ((AddCommand) descCommand).getDescription());

        /* Boundary case for date and description partition */
        String descDateArgument = "add meeting today";
        Command descDateCommand = parser.parse(descDateArgument);
        assertEquals("must be ADD command", true,
                     descDateCommand instanceof AddCommand);
        assertEquals("description must be assigned", "meeting",
                     ((AddCommand) descDateCommand).getDescription());
        assertEquals("date must be assigned", false,
                     ((AddCommand) descDateCommand).getDatePairs().isEmpty());

        /* Testing for different alias with valid input */
        assertEquals(true, parser.parse("add meeting") instanceof AddCommand);
        assertEquals(true,
                     parser.parse("insert meeting") instanceof AddCommand);
        assertEquals(true, parser.parse("ins meeting") instanceof AddCommand);
        assertEquals(true, parser.parse("new meeting") instanceof AddCommand);
    }

    @Test
    public void parseDelete() {
        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
        String noArgument = "delete";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no argument", true,
                     noCommand instanceof InvalidCommand);

        /* Boundary case for > 1 argument partition */
        String twoArgument = "delete 1 2";
        Command twoCommand = parser.parse(twoArgument);
        assertEquals("must be DELETE command", true,
                     twoCommand instanceof DeleteCommand);
        assertEquals("Only first argument will be parsed and assigned", 1,
                     ((DeleteCommand) twoCommand).getTaskId());

        /* Boundary case for 1 valid argument partition */
        String oneArgument = "delete 4";
        Command oneCommand = parser.parse(oneArgument);
        assertEquals("must be DELETE command", true,
                     oneCommand instanceof DeleteCommand);
        assertEquals("Task ID should be properly assigned", 4,
                     ((DeleteCommand) oneCommand).getTaskId());

        /* Testing for invalid arguments in input */
        String notInteger = "delete notinteger";
        Command notIntCommand = parser.parse(notInteger);
        assertEquals("will return INVALID command due to String argument",
                     true, notIntCommand instanceof InvalidCommand);

        /* Testing for different alias with valid input */
        assertEquals(true, parser.parse("delete 1") instanceof DeleteCommand);
        assertEquals(true, parser.parse("remove 2") instanceof DeleteCommand);
    }

    @Test
    public void parseUpdate() {
        /* Testing for number of arguments in input */
        /* Boundary case for no task ID partition */
        String noIdArgument = "update no meeting";
        Command noIdCommand = parser.parse(noIdArgument);
        assertEquals("will return INVALID command due to no task ID", true,
                     noIdCommand instanceof InvalidCommand);

        /* Boundary case for task ID w/o argument partition */
        String noArgument = "update 2";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no task ID", true,
                     noCommand instanceof InvalidCommand);

        /* Boundary case for task ID w/ argument */
        String haveArgument = "update 2 desc today";
        Command haveCommand = parser.parse(haveArgument);
        assertEquals("will return UPDATE command since valid", true,
                     haveCommand instanceof UpdateCommand);
        assertEquals("description must be assigned", "desc",
                     ((UpdateCommand) haveCommand).getDescription());
        assertEquals("date must be assigned", false,
                     ((UpdateCommand) haveCommand).getDatePairs().isEmpty());

        /* Testing for different alias with valid input */
        assertEquals(true,
                     parser.parse("change 1 desc") instanceof UpdateCommand);
        assertEquals(true,
                     parser.parse("update 2 desc") instanceof UpdateCommand);
        assertEquals(true,
                     parser.parse("edit 2 25 Nov") instanceof UpdateCommand);
    }

    @Test
    public void parseUndo() {
        /* Testing for different alias for UNDO command */
        assertEquals(true, parser.parse("undo") instanceof UndoCommand);
        assertEquals(true, parser.parse("ud") instanceof UndoCommand);
    }

    @Test
    public void parseRedo() {
        /* Testing for different alias for REDO command */
        assertEquals(true, parser.parse("redo") instanceof RedoCommand);
        assertEquals(true, parser.parse("rd") instanceof RedoCommand);
    }

    @Test
    public void parseMark() {
        /* Testing for number of arguments in input */
        /* Boundary case for no arguments partition */
        String noArgument = "mark";
        Command noCommand = parser.parse(noArgument);
        assertEquals("Will return INVALID command due to lack of arguments",
                     true, noCommand instanceof InvalidCommand);

        /* Boundary case for >1 arguments partition */
        String twoArgument = "mark 1 2";
        Command twoCommand = parser.parse(twoArgument);
        assertEquals("Must be a MARK command", true,
                     twoCommand instanceof MarkCommand);
        assertEquals("Will only accept the first argument to be the valid one",
                     1, ((MarkCommand) twoCommand).getTaskId());

        /* Boundary case for 1 argument partition */
        String oneArgument = "mark 4";
        Command oneCommand = parser.parse(oneArgument);
        assertEquals("Must be a MARK command", true,
                     oneCommand instanceof MarkCommand);
        assertEquals("Task ID should be properly recorded", 4,
                     ((MarkCommand) oneCommand).getTaskId());

        /* Testing for invalid arguments in input */
        String notInteger = "mark asamplestring";
        Command notIntCommand = parser.parse(notInteger);
        assertEquals("Will return INVALID command due to invalid task id",
                     true, notIntCommand instanceof InvalidCommand);

        /* Testing for different alias with valid input */
        String markAlias = "mark 2";
        Command markCommand = parser.parse(markAlias);
        assertEquals("Must be a MARK command", true,
                     markCommand instanceof MarkCommand);

        String completedAlias = "completed 5";
        Command completedCommand = parser.parse(completedAlias);
        assertEquals("Must be a MARK command", true,
                     completedCommand instanceof MarkCommand);

        String doneAlias = "done 3";
        Command doneCommand = parser.parse(doneAlias);
        assertEquals("Must be a MARK command", true,
                     doneCommand instanceof MarkCommand);
    }

    @Test
    public void parseConfirm() {
        /* Testing for number of arguments in input */
        /* Boundary case for < 2 arguments partition */
        String oneArgument = "confirm 1";
        Command oneCommand = parser.parse(oneArgument);
        assertEquals("Will return INVALID command due to lack of arguments",
                     true, oneCommand instanceof InvalidCommand);

        /* Boundary case for > 2 arguments partition */
        String threeArgument = "confirm 1 2 3";
        Command threeCommand = parser.parse(threeArgument);
        assertEquals("Must be a CONFIRM command", true,
                     threeCommand instanceof ConfirmCommand);
        assertEquals("Will only accept first two arguments", 1,
                     ((ConfirmCommand) threeCommand).getTaskId());
        assertEquals("Will only accept first two arguments", 2,
                     ((ConfirmCommand) threeCommand).getDateId());

        /* Boundary case for 2 arguments partition */
        String twoArgument = "confirm 1 2";
        Command twoCommand = parser.parse(twoArgument);
        assertEquals("Must be a CONFIRM command", true,
                     twoCommand instanceof ConfirmCommand);
        assertEquals("Task ID should be the first argument",
                     ((ConfirmCommand) twoCommand).getTaskId(), 1);
        assertEquals("Date ID should be the second argument",
                     ((ConfirmCommand) twoCommand).getDateId(), 2);

        /* Testing for invalid arguments in input */
        String notInteger = "mark asamplestring";
        Command notIntCommand = parser.parse(notInteger);
        assertEquals("Will return INVALID command due to invalid task id",
                     true, notIntCommand instanceof InvalidCommand);
    }

    @Test
    public void parseExit() {
        /* Testing for different alias for EXIT command */
        assertEquals(true, parser.parse("exit") instanceof ExitCommand);
        assertEquals(true, parser.parse("quit") instanceof ExitCommand);
    }

    /* Helper methods to get variable dates for comparison */
    private Date getDate(int day, int month, int year, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
