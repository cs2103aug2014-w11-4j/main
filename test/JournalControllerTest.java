//@author A0119416H

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class JournalControllerTest {

    DatabaseManager<String> dbManager;
    JournalController<String> journalController;

    @Before
    public void setUp() throws Exception {
        dbManager = new DatabaseManager<String>(File.createTempFile("JournalControllerTest", ".tmp").getPath());
        journalController = new JournalController<String>(dbManager);
    }

    @Test
    public void testRecordAction() throws Exception {
        Long newId = dbManager.putInstance("Test 0");
        journalController.recordAction(null, newId, "create instance");

        newId = dbManager.putInstance("Test 1");
        journalController.recordAction(null, newId, "create instance");

        dbManager.markAsInvalid(newId);
        Long modifiedId = dbManager.putInstance("Modified test 1");
        journalController.recordAction(newId, modifiedId, "modify instance");

        dbManager.markAsInvalid(modifiedId);
        journalController.recordAction(modifiedId, null, "delete instance");

        assertEquals(dbManager.getValidIdList().size(), 1);
        assertEquals(journalController.getUndoStackSize(), 4);
        assertEquals(journalController.getRedoStackSize(), 0);
    }

    @Test
    public void testUndo() throws Exception {
        Long newId = dbManager.putInstance("Test 0");
        journalController.recordAction(null, newId, "create instance");

        newId = dbManager.putInstance("Test 1");
        journalController.recordAction(null, newId, "create instance");

        dbManager.markAsInvalid(newId);
        Long modifiedId = dbManager.putInstance("Modified test 1");
        journalController.recordAction(newId, modifiedId, "modify instance");

        dbManager.markAsInvalid(modifiedId);
        journalController.recordAction(modifiedId, null, "delete instance");

        journalController.undo();
        assertEquals(dbManager.getValidIdList().size(), 2);
        assertEquals(journalController.getUndoStackSize(), 3);
        assertEquals(journalController.getRedoStackSize(), 1);

        journalController.undo();
        assertEquals(dbManager.getValidIdList().size(), 2);
        assertEquals(journalController.getUndoStackSize(), 2);
        assertEquals(journalController.getRedoStackSize(), 2);
    }

    @Test
    public void testRedo() throws Exception {
        Long newId = dbManager.putInstance("Test 0");
        journalController.recordAction(null, newId, "create instance");

        newId = dbManager.putInstance("Test 1");
        journalController.recordAction(null, newId, "create instance");

        dbManager.markAsInvalid(newId);
        Long modifiedId = dbManager.putInstance("Modified test 1");
        journalController.recordAction(newId, modifiedId, "modify instance");

        dbManager.markAsInvalid(modifiedId);
        journalController.recordAction(modifiedId, null, "delete instance");

        journalController.undo();
        journalController.undo();

        journalController.redo();
        assertEquals(dbManager.getValidIdList().size(), 2);

        journalController.redo();
        assertEquals(dbManager.getValidIdList().size(), 1);

        journalController.undo();
        assertEquals(dbManager.getValidIdList().size(), 2);
        newId = dbManager.putInstance("Test clear redo");
        journalController.recordAction(null, newId, "create instance");
        assertEquals(journalController.getRedoStackSize(), 0);
    }
}
