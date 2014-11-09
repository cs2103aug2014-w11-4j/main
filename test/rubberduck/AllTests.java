package rubberduck;

import rubberduck.logic.command.CommandTest;
import rubberduck.logic.parser.ParserTest;
import rubberduck.storage.JournalControllerTest;
import rubberduck.storage.task.DatePairTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//@author A0111736M
@RunWith(Suite.class)
@SuiteClasses({DatePairTest.class, JournalControllerTest.class,
               CommandTest.class, ParserTest.class})
public class AllTests {

}
