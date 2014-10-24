import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ DatePairTest.class, JournalControllerTest.class,
        CommandTest.class, ParserTest.class })
public class AllTests {

}
