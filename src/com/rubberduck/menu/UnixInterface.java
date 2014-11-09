package com.rubberduck.menu;

import com.rubberduck.logic.command.Command;
import com.rubberduck.logic.command.Response;
import com.rubberduck.logic.parser.Parser;
import com.rubberduck.common.formatter.ColorFormatter;
import com.rubberduck.common.formatter.ColorFormatter.Color;
import com.rubberduck.common.formatter.Formatter;

import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class handles the user interface of the application that is running on
 * Mac/Linux. This will handle all input from the user and show the required
 * response back to the user.
 */
//@author A0111736M
public class UnixInterface extends MenuInterface {

    private static final String MESSAGE_PAGE_PROMPT =
        "Press [Enter] to continue...";
    private static final String MESSAGE_SET_24HOUR =
        "Successfully toggled time formatting to 24 hour format.";
    private static final String MESSAGE_SET_12HOUR =
        "Successfully toggled time formatting to 12 hour format.";
    private static final String[] ARGUMENTS_VIEW =
        new String[]{"all", "deadline", "task", "schedule", "completed",
                     "overdue"};
    private static final String SEPARATOR_BORDER =
        "--------------------------------------------------------------------------------";

    private ConsoleReader consoleInstance;

    /**
     * Default constructor of UnixInterface that setup the consoleReader
     * instance.
     */
    protected UnixInterface() {
        try {
            consoleInstance = setupConsoleReader();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_SETUP, e);
        }
    }


    /**
     * Handles the interface of the program. It prompts from user and passes to
     * the parser to determine the command to be executed. It then proceed to
     * execute the returned command and print the returned response to the
     * user.
     */
    @Override
    public void handleInterface() {
        try {
            showToUser(getWelcomeMessage());
            while (true) {
                String line = consoleInstance.readLine(DEFAULT_PROMPT);
                Response res = Parser.getInstance().parseInput(line);
                showToUser(res);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
        }
    }

    /**
     * Used to setup and instantiate the ConsoleReader from jLine.
     *
     * @return ConsoleReader object
     * @throws IOException occurs when ConsoleReader has problem with output
     */
    //@author A0111794E
    private ConsoleReader setupConsoleReader() throws IOException {
        ConsoleReader cr = new ConsoleReader();
        cr.clearScreen();
        cr.setPrompt(DEFAULT_PROMPT);
        setCompleter(cr);
        setKeybinding(cr);
        return cr;
    }

    /**
     * Map the required keyboard keys to perform the required function when
     * triggered.
     *
     * @param cr ConsoleReader object
     */
    private void setKeybinding(ConsoleReader cr) {
        final String insert = "\033[2~";

        cr.getKeys().bind(insert, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleTimeFormat();
            }
        });
    }

    /**
     * Toggles the formatter datestamp between 12 hour and 24 hour format.
     */
    private void toggleTimeFormat() {
        Formatter.toggleTimeFormat();
        Response r = Command.getPreviousDisplayCommand().safeExecute();
        String toggleMessage = Formatter.is12HourFormat() ? MESSAGE_SET_12HOUR
                                                          : MESSAGE_SET_24HOUR;
        r.setMessages(ColorFormatter.format(toggleMessage, Color.CYAN));
        try {
            showToUser(r);
            consoleInstance.restoreLine(consoleInstance.getPrompt(),
                                        consoleInstance.getCursorBuffer().
                                            current());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
        }
    }

    /**
     * Set up the auto-complete feature by specifying the required completers
     * into the consoleReader.
     *
     * @param cr ConsoleReader object
     */
    private void setCompleter(ConsoleReader cr) {
        Set<String> viewAliasSet =
            Command.CommandType.getAlias(Command.CommandType.VIEW);
        Set<String> otherAliasSet =
            new HashSet<String>(Command.CommandType.getAlias());
        otherAliasSet.removeAll(viewAliasSet);

        StringsCompleter otherCompleter = new StringsCompleter(otherAliasSet);
        ArgumentCompleter viewCompleter = new ArgumentCompleter(
            new StringsCompleter(viewAliasSet),
            new StringsCompleter(ARGUMENTS_VIEW));
        cr.addCompleter(new AggregateCompleter(otherCompleter, viewCompleter));
    }

    /**
     * Returns messages and information to display on startup when user opens
     * application for the first time.
     *
     * @return Response object that contains the welcome message
     */
    //@author A0111736M
    private Response getWelcomeMessage() {
        StringBuilder messages = new StringBuilder();
        messages.append(MESSAGE_WELCOME);
        messages.append(System.lineSeparator());
        messages.append(ColorFormatter.format(MESSAGE_HELP, Color.YELLOW));

        Response res = Parser.getInstance().parseInput(WELCOME_EXECUTE);
        res.setMessages(messages.toString());
        return res;
    }

    /**
     * Format and output a Response object given by Command to the ConsoleReader
     * instance which will be visible to the user.
     *
     * @param res Response object to be displayed
     * @throws IOException occurs when ConsoleReader has problem with output
     */
    private void showToUser(Response res) throws IOException {
        consoleInstance.clearScreen();
        String[] messages = res.getMessages();
        String viewCount = res.getViewCount();
        String viewData = res.getViewData();

        String header = String.format(Formatter.FORMAT_TABLE, "ID",
                                      "Done", "Task", "Date");

        StringBuilder collatedBuilder = new StringBuilder();

        for (String m : messages) {
            if (collatedBuilder.length() > 0) {
                collatedBuilder.append(System.lineSeparator());
            }
            collatedBuilder.append(m);
        }

        if (viewCount != null) {
            if (collatedBuilder.length() > 0) {
                collatedBuilder.append(System.lineSeparator());
            }
            collatedBuilder.append(viewCount);
        }

        if (viewData != null && !viewData.trim().isEmpty()) {
            collatedBuilder.append(System.lineSeparator());
            collatedBuilder.append(SEPARATOR_BORDER);
            collatedBuilder.append(System.lineSeparator());
            collatedBuilder.append(header);
            collatedBuilder.append(System.lineSeparator());
            collatedBuilder.append(SEPARATOR_BORDER);

            String[] taskArray = viewData.split(System.lineSeparator());
            for (String task : taskArray) {
                collatedBuilder.append(System.lineSeparator());
                collatedBuilder.append(task);
            }

            collatedBuilder.append(System.lineSeparator());
            collatedBuilder.append(SEPARATOR_BORDER);
        }

        /* Additional one way paging to not overwhelm the user at one go. */
        String collatedResponse = collatedBuilder.toString();
        String[] pageBuffer = collatedResponse.split(System.lineSeparator());
        int bufferHeight = consoleInstance.getTerminal().getHeight() - 2;
        for (int i = 0; i < pageBuffer.length; i++) {
            consoleInstance.println(pageBuffer[i]);
            if (i >= bufferHeight) {
                consoleInstance.readLine(
                    ColorFormatter.format(MESSAGE_PAGE_PROMPT, Color.CYAN));
                consoleInstance.clearScreen();
                bufferHeight += bufferHeight + 1;
            }
        }
    }

    /**
     * Displays a prompt midway through an execution of a command and request an
     * input from the user which will be returned to the command execution
     * flow.
     *
     * @param prompt String literals to be displayed to the user
     * @return response by the user
     */
    @Override
    public String requestPrompt(String... prompt) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String p : prompt) {
                if (sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(p);
            }
            showToUser(new Response(sb.toString(), true));
            return consoleInstance.readLine(DEFAULT_PROMPT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            return MESSAGE_ERROR_CR_IOEXCEPTION;
        }
    }
}
