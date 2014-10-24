import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * Test Cases used in Unit Testing for Parser class
 *
 * @author hooitong
 *
 */
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
        assertEquals("will return INVALID command due to argument",
                Command.CommandType.INVALID, noCommand.getType());

        /* Boundary case for date only partition */
        String dateArgument = "view 25 Oct";
        Command dateCommand = parser.parse(dateArgument);
        assertEquals("must be VIEW command", Command.CommandType.VIEW,
                dateCommand.getType());
        assertEquals("must have a view date", true, ((ViewCommand) dateCommand).getViewRange()
                .hasEndDate());

        /* Boundary case for date range partition */
        String rangeArgument = "view 25 Oct - 30 Oct";
        Command rangeCommand = parser.parse(rangeArgument);
        assertEquals("must be VIEW command", Command.CommandType.VIEW,
                rangeCommand.getType());
        assertEquals("must have a view range", true,
                ((ViewCommand) rangeCommand).getViewRange().hasDateRange());

        /* Boundary case for 'all' partition */
        String allArgument = "view all";
        Command allCommand = parser.parse(allArgument);
        assertEquals("must be VIEW command", Command.CommandType.VIEW,
                allCommand.getType());
        assertEquals("boolean for viewAll should be true", true,
                ((ViewCommand) allCommand).isViewAll());

        /* Boundary case for other String input partition */
        String otherArgument = "view randomstring";
        Command otherCommand = parser.parse(otherArgument);
        assertEquals("will return INVALID command due to no valid argument",
                Command.CommandType.INVALID, otherCommand.getType());

        /* Testing for different alias with valid input */
        assertEquals(Command.CommandType.VIEW, parser.parse("view 25 oct 2014")
                .getType());
        assertEquals(Command.CommandType.VIEW,
                parser.parse("display this week").getType());
    }

    @Test
    public void parseSearch() {
        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
        String noArgument = "search";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no argument",
                Command.CommandType.INVALID, noCommand.getType());

        /* Boundary case for valid argument (String) partition */
        String stringArgument = "search meeting with boss";
        Command stringCommand = parser.parse(stringArgument);
        assertEquals("must be SEARCH command", Command.CommandType.SEARCH,
                stringCommand.getType());
        assertEquals("keyword must be properly stored", "meeting with boss",
                ((SearchCommand) stringCommand).getKeyword());

        /* Testing for different alias with valid input */
        assertEquals(Command.CommandType.SEARCH, parser.parse("search meeting")
                .getType());

        assertEquals(Command.CommandType.SEARCH, parser.parse("find urgent")
                .getType());

        assertEquals(Command.CommandType.SEARCH,
                parser.parse("lookup homework").getType());
    }

    @Test
    public void parseAdd() {
        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
        String noArgument = "add";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no argument",
                Command.CommandType.INVALID, noCommand.getType());

        /* Boundary case for date only partition */
        String dateArgument = "add today";
        Command dateCommand = parser.parse(dateArgument);
        assertEquals("will return INVALID command due to lack of description",
                Command.CommandType.INVALID, dateCommand.getType());

        /* Boundary case for description only partition */
        String descArgument = "add meeting";
        Command descCommand = parser.parse(descArgument);
        assertEquals("must be ADD command", Command.CommandType.ADD,
                descCommand.getType());
        assertEquals("description must be assigned", "meeting",
                descCommand.getDescription());

        /* Boundary case for date and description partition */
        String descDateArgument = "add meeting today";
        Command descDateCommand = parser.parse(descDateArgument);
        assertEquals("must be ADD command", Command.CommandType.ADD,
                descDateCommand.getType());
        assertEquals("description must be assigned", "meeting",
                descDateCommand.getDescription());
        assertEquals("date must be assigned", false,
                descDateCommand.getDatePairs().isEmpty());

        /* Testing for different alias with valid input */
        assertEquals(Command.CommandType.ADD, parser.parse("add meeting")
                .getType());
        assertEquals(Command.CommandType.ADD, parser.parse("insert meeting")
                .getType());
        assertEquals(Command.CommandType.ADD, parser.parse("ins meeting")
                .getType());
        assertEquals(Command.CommandType.ADD, parser.parse("new meeting")
                .getType());
    }

    @Test
    public void parseDelete() {
        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
        String noArgument = "delete";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no argument",
                Command.CommandType.INVALID, noCommand.getType());

        /* Boundary case for > 1 argument partition */
        String twoArgument = "delete 1 2";
        Command twoCommand = parser.parse(twoArgument);
        assertEquals("must be DELETE command", Command.CommandType.DELETE,
                twoCommand.getType());
        assertEquals("Only first argument will be parsed and assigned", 1,
                twoCommand.getTaskId());

        /* Boundary case for 1 valid argument partition */
        String oneArgument = "delete 4";
        Command oneCommand = parser.parse(oneArgument);
        assertEquals("must be DELETE command", Command.CommandType.DELETE,
                oneCommand.getType());
        assertEquals("Task ID should be properly assigned", 4,
                oneCommand.getTaskId());

        /* Testing for invalid arguments in input */
        String notInteger = "delete notinteger";
        Command notIntCommand = parser.parse(notInteger);
        assertEquals("will return INVALID command due to String argument",
                Command.CommandType.INVALID, notIntCommand.getType());

        /* Testing for different alias with valid input */
        assertEquals(Command.CommandType.DELETE, parser.parse("delete 1")
                .getType());
        assertEquals(Command.CommandType.DELETE, parser.parse("remove 2")
                .getType());
    }

    @Test
    public void parseUpdate() {
        /* Testing for number of arguments in input */
        /* Boundary case for no task ID partition */
        String noIdArgument = "update no meeting";
        Command noIdCommand = parser.parse(noIdArgument);
        assertEquals("will return INVALID command due to no task ID",
                Command.CommandType.INVALID, noIdCommand.getType());

        /* Boundary case for task ID w/o argument partition */
        String noArgument = "update 2";
        Command noCommand = parser.parse(noArgument);
        assertEquals("will return INVALID command due to no task ID",
                Command.CommandType.INVALID, noCommand.getType());

        /* Boundary case for task ID w/ argument */
        String haveArgument = "update 2 desc today";
        Command haveCommand = parser.parse(haveArgument);
        assertEquals("will return INVALID command due to no task ID",
                Command.CommandType.UPDATE, haveCommand.getType());
        assertEquals("description must be assigned", "desc",
                haveCommand.getDescription());
        assertEquals("date must be assigned", false, haveCommand.getDatePairs()
                .isEmpty());

        /* Testing for different alias with valid input */
        assertEquals(Command.CommandType.UPDATE, parser.parse("change 1 desc")
                .getType());
        assertEquals(Command.CommandType.UPDATE, parser.parse("update 2 desc")
                .getType());
        assertEquals(Command.CommandType.UPDATE, parser.parse("edit 2 25 Nov")
                .getType());
    }

    @Test
    public void parseUndo() {
        /* Testing for different alias for UNDO command */
        assertEquals(Command.CommandType.UNDO, parser.parse("undo").getType());
        assertEquals(Command.CommandType.UNDO, parser.parse("ud").getType());
    }

    @Test
    public void parseRedo() {
        /* Testing for different alias for REDO command */
        assertEquals(Command.CommandType.REDO, parser.parse("redo").getType());
        assertEquals(Command.CommandType.REDO, parser.parse("rd").getType());
    }

    @Test
    public void parseMark() {
        /* Testing for number of arguments in input */
        /* Boundary case for no arguments partition */
        String noArgument = "mark";
        Command noCommand = parser.parse(noArgument);
        assertEquals("Will return INVALID command due to lack of arguments",
                Command.CommandType.INVALID, noCommand.getType());

        /* Boundary case for >1 arguments partition */
        String twoArgument = "mark 1 2";
        Command twoCommand = parser.parse(twoArgument);
        assertEquals("Must be a MARK command", Command.CommandType.MARK,
                twoCommand.getType());
        assertEquals("Will only accept the first argument to be the valid one",
                1, twoCommand.getTaskId());

        /* Boundary case for 1 argument partition */
        String oneArgument = "mark 4";
        Command oneCommand = parser.parse(oneArgument);
        assertEquals("Must be a MARK command", Command.CommandType.MARK,
                oneCommand.getType());
        assertEquals("Task ID should be properly recorded", 4,
                oneCommand.getTaskId());

        /* Testing for invalid arguments in input */
        String notInteger = "mark asamplestring";
        Command notIntCommand = parser.parse(notInteger);
        assertEquals("Will return INVALID command due to invalid task id",
                Command.CommandType.INVALID, notIntCommand.getType());

        /* Testing for different alias with valid input */
        String markAlias = "mark 2";
        Command markCommand = parser.parse(markAlias);
        assertEquals("Must be a MARK command", Command.CommandType.MARK,
                markCommand.getType());

        String completedAlias = "completed 5";
        Command completedCommand = parser.parse(completedAlias);
        assertEquals("Must be a MARK command", Command.CommandType.MARK,
                completedCommand.getType());

        String doneAlias = "done 3";
        Command doneCommand = parser.parse(doneAlias);
        assertEquals("Must be a MARK command", Command.CommandType.MARK,
                doneCommand.getType());
    }

    @Test
    public void parseConfirm() {
        /* Testing for number of arguments in input */
        /* Boundary case for < 2 arguments partition */
        String oneArgument = "confirm 1";
        Command oneCommand = parser.parse(oneArgument);
        assertEquals("Will return INVALID command due to lack of arguments",
                Command.CommandType.INVALID, oneCommand.getType());

        /* Boundary case for > 2 arguments partition */
        String threeArgument = "confirm 1 2 3";
        Command threeCommand = parser.parse(threeArgument);
        assertEquals("Must be a CONFIRM command", Command.CommandType.CONFIRM,
                threeCommand.getType());
        assertEquals("Will only accept first two arguments", 1,
                threeCommand.getTaskId());
        assertEquals("Will only accept first two arguments", 2,
                ((ConfirmCommand) threeCommand).getDateId());

        /* Boundary case for 2 arguments partition */
        String twoArgument = "confirm 1 2";
        Command twoCommand = parser.parse(twoArgument);
        assertEquals("Must be a CONFIRM command", Command.CommandType.CONFIRM,
                twoCommand.getType());
        assertEquals("Task ID should be the first argument",
                twoCommand.getTaskId(), 1);
        assertEquals("Date ID should be the second argument",
                ((ConfirmCommand) twoCommand).getDateId(), 2);

        /* Testing for invalid arguments in input */
        String notInteger = "mark asamplestring";
        Command notIntCommand = parser.parse(notInteger);
        assertEquals("Will return INVALID command due to invalid task id",
                Command.CommandType.INVALID, notIntCommand.getType());
    }

    @Test
    public void parseExit() {
        /* Testing for different alias for EXIT command */
        assertEquals(Command.CommandType.EXIT, parser.parse("exit").getType());
        assertEquals(Command.CommandType.EXIT, parser.parse("quit").getType());
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
