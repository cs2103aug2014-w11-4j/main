import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test Cases for Parser class
 *
 * @author hooitong
 *
 */
public class ParserTest {

    @Test
    public void parseAdd() {
        assertEquals(Parser.parseAdd("meeting with boss today").getDescription(), "meeting with boss");
    }

    @Test
    public void parseView() {
        fail("Not yet implemented");
    }

    @Test
    public void parseDelete() {
        fail("Not yet implemented");
    }

    @Test
    public void parseUpdate() {
        fail("Not yet implemented");
    }

    @Test
    public void parseSearch() {
        fail("Not yet implemented");
    }

    @Test
    public void parseUndo() {
        fail("Not yet implemented");
    }

    @Test
    public void parseRedo() {
        fail("Not yet implemented");
    }

    @Test
    public void parseMark() {
        fail("Not yet implemented");
    }

    @Test
    public void parseExit() {
        fail("Not yet implemented");
    }

}
