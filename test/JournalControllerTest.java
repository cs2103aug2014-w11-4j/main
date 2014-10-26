//@author A0119416H

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.rubberduck.io.DatabaseManager;
import com.rubberduck.io.JournalController;

public class JournalControllerTest {

    DatabaseManager<String> dbManager;

    @Before
    public void setUp() throws Exception {
        dbManager = new DatabaseManager<String>(File.createTempFile(
                "JournalControllerTest", ".tmp").getPath());
    }

    @Test
    public void testRecordAction() throws Exception {
        Long newId = dbManager.modify(null, "Test 0", "create instance");

        newId = dbManager.modify(null, "Test 1", "create instance");

        Long modifiedId = dbManager.modify(newId, "Modified test 1", "modify instance");

        dbManager.modify(modifiedId, null, "delete instance");

        assertEquals(dbManager.getValidIdList().size(), 1);
        assertEquals(dbManager.getJournal().getUndoStackSize(), 4);
        assertEquals(dbManager.getJournal().getRedoStackSize(), 0);
    }

    @Test
    public void testUndo() throws Exception {
        Long newId = dbManager.modify(null, "Test 0", "create instance");

        newId = dbManager.modify(null, "Test 1", "create instance");

        Long modifiedId = dbManager.modify(newId, "Modified test 1", "modify instance");

        dbManager.modify(modifiedId, null, "delete instance");

        dbManager.undo();
        assertEquals(dbManager.getValidIdList().size(), 2);
        assertEquals(dbManager.getJournal().getUndoStackSize(), 3);
        assertEquals(dbManager.getJournal().getRedoStackSize(), 1);

        dbManager.undo();
        assertEquals(dbManager.getValidIdList().size(), 2);
        assertEquals(dbManager.getJournal().getUndoStackSize(), 2);
        assertEquals(dbManager.getJournal().getRedoStackSize(), 2);
    }

    @Test
    public void testRedo() throws Exception {
        Long newId = dbManager.modify(null, "Test 0", "create instance");

        newId = dbManager.modify(null, "Test 1", "create instance");

        Long modifiedId = dbManager.modify(newId, "Modified test 1", "modify instance");

        dbManager.modify(modifiedId, null, "delete instance");

        dbManager.undo();
        dbManager.undo();

        dbManager.redo();
        assertEquals(dbManager.getValidIdList().size(), 2);

        dbManager.redo();
        assertEquals(dbManager.getValidIdList().size(), 1);

        dbManager.undo();
        assertEquals(dbManager.getValidIdList().size(), 2);
        newId = dbManager.modify(null, "Test clear redo", "create instance");
        assertEquals(dbManager.getJournal().getRedoStackSize(), 0);
    }
}
