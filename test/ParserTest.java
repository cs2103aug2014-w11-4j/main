import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * Test Cases for Parser class
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
    public void parseAdd() {
        assertEquals(parser.parse("add meeting with boss today").getType(),
                Command.CommandType.ADD);
        assertEquals(parser.parse("add meeting with boss today")
                .getDescription(), "meeting with boss");
        assertEquals(parser.parse("add meeting with boss today")
                .getDatePairs()
                .get(0)
                .getEndDate()
                .getTime(), getToday());
    }

    @Test
    public void parseView() {
        assertEquals(parser.parse("view today").getType(),
                Command.CommandType.VIEW);

        assertEquals(parser.parse("display this week").getType(),
                Command.CommandType.VIEW);
    }

    @Test
    public void parseDelete() {
        assertEquals(parser.parse("delete 4").getType(),
                Command.CommandType.DELETE);
        assertEquals(parser.parse("delete 4").getTaskId(), 4);

        assertEquals(parser.parse("remove 8").getType(),
                Command.CommandType.DELETE);
        assertEquals(parser.parse("remove 8").getTaskId(), 8);
    }

    @Test
    public void parseUpdate() {
        assertEquals(parser.parse("update 3 tommorrow").getType(),
                Command.CommandType.UPDATE);
    }

    @Test
    public void parseSearch() {
        assertEquals(parser.parse("search meeting").getType(),
                Command.CommandType.SEARCH);
        assertEquals(parser.parse("search meeting").getKeyword(), "meeting");

        assertEquals(parser.parse("find urgent").getType(),
                Command.CommandType.SEARCH);
        assertEquals(parser.parse("find urgent").getKeyword(), "urgent");

        assertEquals(parser.parse("lookup homework").getType(),
                Command.CommandType.SEARCH);
        assertEquals(parser.parse("lookup homework").getKeyword(), "homework");
    }

    @Test
    public void parseUndo() {
        assertEquals(parser.parse("undo").getType(), Command.CommandType.UNDO);
        assertEquals(parser.parse("ud").getType(), Command.CommandType.UNDO);
    }

    @Test
    public void parseRedo() {
        assertEquals(parser.parse("redo").getType(), Command.CommandType.REDO);
        assertEquals(parser.parse("rd").getType(), Command.CommandType.REDO);
    }

    @Test
    public void parseMark() {
        assertEquals(parser.parse("mark 2").getType(), Command.CommandType.MARK);
        assertEquals(parser.parse("mark 2").getTaskId(), 2);

        assertEquals(parser.parse("completed 5").getType(),
                Command.CommandType.MARK);
        assertEquals(parser.parse("completed 5").getTaskId(), 5);

        assertEquals(parser.parse("done 7").getType(), Command.CommandType.MARK);
        assertEquals(parser.parse("done 7").getTaskId(), 7);
    }

    @Test
    public void parseExit() {
        assertEquals(parser.parse("exit").getType(), Command.CommandType.EXIT);
        assertEquals(parser.parse("quit").getType(), Command.CommandType.EXIT);
    }

    public Date getDate(int day, int month, int year, int hour, int minute) {
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

    public Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
