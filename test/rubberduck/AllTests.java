package rubberduck;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import rubberduck.common.datatransfer.DatePairTest;
import rubberduck.logic.command.CommandTest;
import rubberduck.logic.parser.ParserTest;
import rubberduck.storage.JournalControllerTest;

/**
 * Master test suite that developers should run every time they develop or
 * modify something to tackle regression.
 */
//@author A0111736M
@RunWith(Suite.class)
@SuiteClasses({DatePairTest.class, JournalControllerTest.class,
               CommandTest.class, ParserTest.class})
public class AllTests {

}
