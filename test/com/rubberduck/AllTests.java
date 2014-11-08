package com.rubberduck;

import com.rubberduck.logic.command.CommandTest;
import com.rubberduck.logic.parser.ParserTest;
import com.rubberduck.storage.JournalControllerTest;
import com.rubberduck.storage.task.DatePairTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

//@author A0111736M
@RunWith(Suite.class)
@SuiteClasses({DatePairTest.class, JournalControllerTest.class,
               CommandTest.class, ParserTest.class})
public class AllTests {

}
