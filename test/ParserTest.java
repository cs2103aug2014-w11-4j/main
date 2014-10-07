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
    @Before
    public void setParser() {
        Parser.initParser();
    }

    @Test
    public void parseAdd() {
        assertEquals(Parser.parse("add meeting with boss today").getType(),
                CommandType.ADD);
        assertEquals(Parser.parse("add meeting with boss today")
                .getDescription(), "meeting with boss");
        assertEquals(Parser.parse("add meeting with boss today").getViewRange()
                .getStartDate().getTime(), getToday());
    }

    @Test
    public void parseView() {
        assertEquals(Parser.parse("view today").getType(), CommandType.VIEW);

        assertEquals(Parser.parse("display this week").getType(),
                CommandType.VIEW);
    }

    @Test
    public void parseDelete() {
        assertEquals(Parser.parse("delete 4").getType(), CommandType.DELETE);
        assertEquals(Parser.parse("delete 4").getTaskId(), 4);

        assertEquals(Parser.parse("remove 8").getType(), CommandType.DELETE);
        assertEquals(Parser.parse("remove 8").getTaskId(), 8);
    }

    @Test
    public void parseUpdate() {
        assertEquals(Parser.parse("update 3 tommorrow").getType(),
                CommandType.UPDATE);
    }

    @Test
    public void parseSearch() {
        assertEquals(Parser.parse("search meeting").getType(),
                CommandType.SEARCH);
        assertEquals(Parser.parse("search meeting").getKeyword(), "meeting");

        assertEquals(Parser.parse("find urgent").getType(), CommandType.SEARCH);
        assertEquals(Parser.parse("find urgent").getKeyword(), "urgent");

        assertEquals(Parser.parse("lookup homework").getType(),
                CommandType.SEARCH);
        assertEquals(Parser.parse("lookup homework").getKeyword(), "homework");
    }

    @Test
    public void parseUndo() {
        assertEquals(Parser.parse("undo").getType(), CommandType.UNDO);
        assertEquals(Parser.parse("ud").getType(), CommandType.UNDO);
    }

    @Test
    public void parseRedo() {
        assertEquals(Parser.parse("redo").getType(), CommandType.REDO);
        assertEquals(Parser.parse("rd").getType(), CommandType.REDO);
    }

    @Test
    public void parseMark() {
        assertEquals(Parser.parse("mark 2").getType(), CommandType.MARK);
        assertEquals(Parser.parse("mark 2").getTaskId(), 2);

        assertEquals(Parser.parse("completed 5").getType(), CommandType.MARK);
        assertEquals(Parser.parse("completed 5").getTaskId(), 5);

        assertEquals(Parser.parse("done 7").getType(), CommandType.MARK);
        assertEquals(Parser.parse("done 7").getTaskId(), 7);
    }

    @Test
    public void parseExit() {
        assertEquals(Parser.parse("exit").getType(), CommandType.EXIT);
        assertEquals(Parser.parse("quit").getType(), CommandType.EXIT);
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
