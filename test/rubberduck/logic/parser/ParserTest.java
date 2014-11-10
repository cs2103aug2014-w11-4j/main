package rubberduck.logic.parser;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import rubberduck.common.datatransfer.DatePair;
import rubberduck.logic.command.AddCommand;
import rubberduck.logic.command.ClearCommand;
import rubberduck.logic.command.Command;
import rubberduck.logic.command.ConfirmCommand;
import rubberduck.logic.command.DeleteCommand;
import rubberduck.logic.command.ExitCommand;
import rubberduck.logic.command.HelpCommand;
import rubberduck.logic.command.InvalidCommand;
import rubberduck.logic.command.MarkCommand;
import rubberduck.logic.command.RedoCommand;
import rubberduck.logic.command.SearchCommand;
import rubberduck.logic.command.SyncCommand;
import rubberduck.logic.command.UndoCommand;
import rubberduck.logic.command.UpdateCommand;
import rubberduck.logic.command.ViewCommand;

import static org.junit.Assert.assertEquals;

/**
 * Unit Test Cases used in Unit Testing for Parser class. Each test case in the
 * this test unit represents a single method tested in Parser.
 */
//@author A0111736M
public class ParserTest {

    private Parser parser;

    @Before
    public void setParser() {
        parser = Parser.getInstance();
    }

    @Test
    public void parseView() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method viewRangeMethod = ViewCommand.class.getDeclaredMethod(
                "getViewRange");
            viewRangeMethod.setAccessible(true);
            Method viewTypeMethod = ViewCommand.class.getDeclaredMethod(
                "getViewType");
            viewTypeMethod.setAccessible(true);

            /* Testing for arguments (number and validity) in input */
            /* Boundary case for no argument partition */
            String noArgument = "view";
            Command noCommand = parser.parse(noArgument);
            assertEquals("will return VIEW command (prev)", true,
                         noCommand instanceof ViewCommand);

            /* Boundary case for date only partition */
            String dateArgument = "view 25 Oct";
            Command dateCommand = parser.parse(dateArgument);
            assertEquals("must be VIEW command", true,
                         dateCommand instanceof ViewCommand);

            DatePair dp = (DatePair) viewRangeMethod.invoke(dateCommand);
            assertEquals("must have a view date", true, dp.hasEndDate());


            /* Boundary case for date range partition */
            String rangeArgument = "view 25 Oct - 30 Oct";
            Command rangeCommand = parser.parse(rangeArgument);
            assertEquals("must be VIEW command", true,
                         rangeCommand instanceof ViewCommand);

            dp = (DatePair) viewRangeMethod.invoke(rangeCommand);
            assertEquals("must have a view range", true, dp.hasDateRange());

            /* Boundary case for 'all' partition */
            String allArgument = "view all";
            Command allCommand = parser.parse(allArgument);
            assertEquals("must be VIEW command", true,
                         allCommand instanceof ViewCommand);
            ViewCommand.ViewType vt = (ViewCommand.ViewType) viewTypeMethod
                .invoke(allCommand);
            assertEquals("boolean for viewAll should be true", true,
                         vt == ViewCommand.ViewType.ALL);

            /* Boundary case for other String input partition */
            String otherArgument = "view randomstring";
            Command otherCommand = parser.parse(otherArgument);
            assertEquals("will return VIEW command (prev)",
                         true, otherCommand instanceof ViewCommand);

            /* Testing for different alias with valid input */
            assertEquals(true, parser.parse("view 25 oct 2014") instanceof
                ViewCommand);
            assertEquals(true, parser.parse("display this week") instanceof
                ViewCommand);

        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in VIEW.");
        }
    }

    @Test
    public void parseSearch() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method keywordMethod = SearchCommand.class.getDeclaredMethod(
                "getKeyword");
            keywordMethod.setAccessible(true);

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
            String keyword = (String) keywordMethod.invoke(stringCommand);
            assertEquals("keyword must be properly stored", "meeting with boss",
                         keyword);

            /* Testing for different alias with valid input */
            assertEquals(true,
                         parser.parse("search me") instanceof SearchCommand);
            assertEquals(true,
                         parser.parse("find urgent") instanceof SearchCommand);
            assertEquals(true,
                         parser.parse("lookup hw") instanceof SearchCommand);

        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in SEARCH.");
        }
    }

    @Test
    public void parseAdd() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method descriptionMethod = AddCommand.class.getDeclaredMethod(
                "getDescription");
            descriptionMethod.setAccessible(true);
            Method datePairsMethod = AddCommand.class.getDeclaredMethod(
                "getDatePairs");
            datePairsMethod.setAccessible(true);

        /* Testing for number of arguments in input */
        /* Boundary case for no argument partition */
            String noArgument = "add";
            Command noCommand = parser.parse(noArgument);
            assertEquals("will return INVALID command due to no argument", true,
                         noCommand instanceof InvalidCommand);

        /* Boundary case for date only partition */
            String dateArgument = "add today";
            Command dateCommand = parser.parse(dateArgument);
            assertEquals(
                "will return INVALID command due to lack of description",
                true, dateCommand instanceof InvalidCommand);

        /* Boundary case for description only partition */
            String descArgument = "add meeting";
            Command descCommand = parser.parse(descArgument);
            assertEquals("must be ADD command", true,
                         descCommand instanceof AddCommand);
            String desc = (String) descriptionMethod.invoke(descCommand);
            assertEquals("description must be assigned", "meeting", desc);

        /* Boundary case for date and description partition */
            String descDateArgument = "add meeting today";
            Command descDateCommand = parser.parse(descDateArgument);
            assertEquals("must be ADD command", true,
                         descDateCommand instanceof AddCommand);
            desc = (String) descriptionMethod.invoke(descCommand);
            ArrayList dp = (ArrayList) datePairsMethod.invoke(descDateCommand);
            assertEquals("description must be assigned", "meeting", desc);
            assertEquals("date must be assigned", false, dp.isEmpty());

        /* Testing for different alias with valid input */
            assertEquals(true,
                         parser.parse("add meeting") instanceof AddCommand);
            assertEquals(true,
                         parser.parse("insert meeting") instanceof AddCommand);
            assertEquals(true,
                         parser.parse("ins meeting") instanceof AddCommand);
            assertEquals(true,
                         parser.parse("new meeting") instanceof AddCommand);
        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in ADD.");
        }
    }

    @Test
    public void parseDelete() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method taskIdMethod = DeleteCommand.class.getDeclaredMethod(
                "getTaskId");
            taskIdMethod.setAccessible(true);

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
            int taskId = (Integer) taskIdMethod.invoke(twoCommand);
            assertEquals("Only first argument will be parsed and assigned", 1,
                         taskId);

            /* Boundary case for 1 valid argument partition */
            String oneArgument = "delete 4";
            Command oneCommand = parser.parse(oneArgument);
            assertEquals("must be DELETE command", true,
                         oneCommand instanceof DeleteCommand);
            taskId = (Integer) taskIdMethod.invoke(oneCommand);
            assertEquals("Task ID should be properly assigned", 4,
                         taskId);

            /* Testing for invalid arguments in input */
            String notInteger = "delete notinteger";
            Command notIntCommand = parser.parse(notInteger);
            assertEquals("will return INVALID command due to String argument",
                         true, notIntCommand instanceof InvalidCommand);

            /* Testing for different alias with valid input */
            assertEquals(true,
                         parser.parse("delete 1") instanceof DeleteCommand);
            assertEquals(true,
                         parser.parse("remove 2") instanceof DeleteCommand);
        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in DELETE.");
        }
    }

    @Test
    public void parseUpdate() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method descriptionMethod = UpdateCommand.class.getDeclaredMethod(
                "getDescription");
            descriptionMethod.setAccessible(true);
            Method datePairsMethod = UpdateCommand.class.getDeclaredMethod(
                "getDatePairs");
            datePairsMethod.setAccessible(true);

            /* Testing for number of arguments in input */
            /* Boundary case for no task ID partition */
            String noIdArgument = "update no meeting";
            Command noIdCommand = parser.parse(noIdArgument);
            assertEquals("will return INVALID command due to no task ID", true,
                         noIdCommand instanceof InvalidCommand);

            /* Boundary case for task ID w/o argument partition */
            String noArgument = "update 2";
            Command noCommand = parser.parse(noArgument);
            assertEquals("will return INVALID command due to no args", true,
                         noCommand instanceof InvalidCommand);

            /* Boundary case for task ID w/ argument */
            String haveArgument = "update 2 desc today";
            Command haveCommand = parser.parse(haveArgument);
            assertEquals("will return UPDATE command since valid", true,
                         haveCommand instanceof UpdateCommand);
            String desc = (String) descriptionMethod.invoke(haveCommand);
            ArrayList datePairs = (ArrayList) datePairsMethod
                .invoke(haveCommand);

            assertEquals("description must be assigned", "desc", desc);
            assertEquals("date must be assigned", false, datePairs.isEmpty());

            /* Testing for different alias with valid input */
            assertEquals(true,
                         parser
                             .parse("change 1 desc") instanceof UpdateCommand);
            assertEquals(true,
                         parser
                             .parse("update 2 desc") instanceof UpdateCommand);
            assertEquals(true,
                         parser
                             .parse("edit 2 25 Nov") instanceof UpdateCommand);
        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in UPDATE.");
        }
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
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method taskIdMethod = MarkCommand.class.getDeclaredMethod(
                "getTaskId");
            taskIdMethod.setAccessible(true);

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
            int taskId = (Integer) taskIdMethod.invoke(twoCommand);
            assertEquals("Will only accept first argument", 1, taskId);

            /* Boundary case for 1 argument partition */
            String oneArgument = "mark 4";
            Command oneCommand = parser.parse(oneArgument);
            assertEquals("Must be a MARK command", true,
                         oneCommand instanceof MarkCommand);
            taskId = (Integer) taskIdMethod.invoke(oneCommand);
            assertEquals("Task ID should be properly recorded", 4, taskId);

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
        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in MARK.");
        }
    }

    @Test
    public void parseConfirm() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method taskIdMethod = ConfirmCommand.class.getDeclaredMethod(
                "getTaskId");
            taskIdMethod.setAccessible(true);
            Method dateIdMethod = ConfirmCommand.class.getDeclaredMethod(
                "getDateId");
            dateIdMethod.setAccessible(true);

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
            int taskId = (Integer) taskIdMethod.invoke(threeCommand);
            int dateId = (Integer) dateIdMethod.invoke(threeCommand);
            assertEquals("Will only accept first two arguments", 1, taskId);
            assertEquals("Will only accept first two arguments", 2, dateId);

            /* Boundary case for 2 arguments partition */
            String twoArgument = "confirm 1 2";
            Command twoCommand = parser.parse(twoArgument);
            assertEquals("Must be a CONFIRM command", true,
                         twoCommand instanceof ConfirmCommand);
            taskId = (Integer) taskIdMethod.invoke(twoCommand);
            dateId = (Integer) dateIdMethod.invoke(twoCommand);
            assertEquals("Task ID should be the first argument", 1, taskId);
            assertEquals("Date ID should be the second argument", 2, dateId);

            /* Testing for invalid arguments in input */
            String notInteger = "mark asamplestring";
            Command notIntCommand = parser.parse(notInteger);
            assertEquals("Will return INVALID command due to invalid task id",
                         true, notIntCommand instanceof InvalidCommand);
        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in CONFIRM.");
        }
    }

    @Test
    public void parseSync() {
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method syncTypeMethod = SyncCommand.class.getDeclaredMethod(
                "getType");
            syncTypeMethod.setAccessible(true);
            SyncCommand.SyncType st;

            /* Testing for all different SyncType for SYNC command */
            String forcePush = "sync force push";
            Command fPushCommand = parser.parse(forcePush);
            st = (SyncCommand.SyncType) syncTypeMethod.invoke(fPushCommand);
            assertEquals("Must be FORCE_PUSH", SyncCommand.SyncType.FORCE_PUSH,
                         st);

            String forcePull = "sync force pull";
            Command fPullCommand = parser.parse(forcePull);
            st = (SyncCommand.SyncType) syncTypeMethod.invoke(fPullCommand);
            assertEquals("Must be FORCE_PULL", SyncCommand.SyncType.FORCE_PULL,
                         st);

            String push = "sync push";
            Command pushCommand = parser.parse(push);
            st = (SyncCommand.SyncType) syncTypeMethod.invoke(pushCommand);
            assertEquals("Must be PUSH", SyncCommand.SyncType.PUSH,
                         st);

            String pull = "sync pull";
            Command pullCommand = parser.parse(pull);
            st = (SyncCommand.SyncType) syncTypeMethod.invoke(pullCommand);
            assertEquals("Must be PULL", SyncCommand.SyncType.PULL,
                         st);

            String twoWay = "sync";
            Command twoWayCommand = parser.parse(twoWay);
            st = (SyncCommand.SyncType) syncTypeMethod.invoke(twoWayCommand);
            assertEquals("Must be TWO_WAY", SyncCommand.SyncType.TWO_WAY,
                         st);

            String logout = "sync logout";
            Command logoutCommand = parser.parse(logout);
            st = (SyncCommand.SyncType) syncTypeMethod.invoke(logoutCommand);
            assertEquals("Must be LOGOUT", SyncCommand.SyncType.LOGOUT,
                         st);

            /* Boundary case for random String arguments input*/
            String invalid = "sync randomstring123 123";
            Command invalidCommand = parser.parse(invalid);
            assertEquals("Must be INVALID", true, invalidCommand instanceof
                InvalidCommand);

        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in SYNC.");
        }
    }

    @Test
    public void parseHelp() {
        /* Testing for different help mode for HELP command */
        try {
            /* Use Reflection to Make Private/Protected Methods Accessible */
            Method typeMethod = HelpCommand.class.getDeclaredMethod(
                "getType");
            typeMethod.setAccessible(true);
            Method isSpecificMethod = HelpCommand.class.getDeclaredMethod(
                "isSpecific");
            isSpecificMethod.setAccessible(true);
            String type;
            boolean isSpecific;

            /* Testing for different way of parsing HELP command */
            String allHelp = "help";
            Command allHelpCommand = parser.parse(allHelp);
            type = (String) typeMethod.invoke(allHelpCommand);
            isSpecific = (boolean) isSpecificMethod.invoke(allHelpCommand);
            assertEquals("Should be null.", null, type);
            assertEquals("Must be not specific.", false, isSpecific);

            String specificHelp = "help view";
            Command specificHelpCommand = parser.parse(specificHelp);
            type = (String) typeMethod.invoke(specificHelpCommand);
            isSpecific = (boolean) isSpecificMethod.invoke(specificHelpCommand);
            assertEquals("Should be the argument.", "view", type);
            assertEquals("Must be specific.", true, isSpecific);

            /* Only the first argument is taken and no arg validation is done */
            String invalidHelp = "help random guy";
            Command invalidHelpCommand = parser.parse(invalidHelp);
            type = (String) typeMethod.invoke(invalidHelpCommand);
            isSpecific = (boolean) isSpecificMethod.invoke(invalidHelpCommand);
            assertEquals("Should be the argument.", "random", type);
            assertEquals("Must be specific.", true, isSpecific);
        } catch (NoSuchMethodException | IllegalAccessException |
            InvocationTargetException e) {
            System.out.println("Error when performing reflection in HELP.");
        }

        /* Testing for different alias for HELP command */
        assertEquals(true, parser.parse("help") instanceof HelpCommand);
        assertEquals(true, parser.parse("?") instanceof HelpCommand);
    }

    @Test
    public void parseClear() {
        /* Testing for different alias for CLEAR command */
        assertEquals(true, parser.parse("clear") instanceof ClearCommand);
        assertEquals(true, parser.parse("cls") instanceof ClearCommand);
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
