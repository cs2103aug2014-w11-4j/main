package rubberduck.menu;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.common.formatter.Formatter;
import rubberduck.logic.command.Command;
import rubberduck.logic.parser.Parser;

import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

//@author A0111736M

/**
 * This class handles the user interface of the application that is running on
 * Windows platform. This will handle all input from the user and show the
 * required response back to the user.
 */
public class WinInterface extends MenuInterface {

    /* CMD command required to forcefully set the width and height of CMD*/
    private static final String COMMAND_SET_CONSOLE =
        "mode.com con cols=81 lines=41";

    private static final String MESSAGE_ERROR_CMD =
        "Interrupted when executing console setup command.";
    private static final String MESSAGE_SET_24HOUR =
        "Successfully toggled time formatting to 24 hour format.";
    private static final String MESSAGE_SET_12HOUR =
        "Successfully toggled time formatting to 12 hour format.";
    private static final String MESSAGE_ASSERT_RESPONSE =
        "Response object returned must not be null.";
    private static final String[] ARGUMENTS_VIEW =
        new String[]{"all", "deadline", "float", "schedule", "completed",
                     "overdue"};

    /* Separator Strings that are used to display as Footer of WinInterface */
    private static final String SEPARATOR_BORDER =
        "--------------------------------------------------------------------------------";
    private static final String SEPARATOR_PAGEUP =
        "-------------------[Page Up]----------------------------------------------------";
    private static final String SEPARATOR_PAGEDOWN =
        "--------------------------------------------------[Page Down]-------------------";
    private static final String SEPARATOR_PAGEUPDOWN =
        "-------------------[Page Up]----------------------[Page Down]-------------------";

    /* Margins & Useful Indexes for ConsoleBuffer */
    private static final int LINES_RESPONSE_AREA = 5;
    private static final int LINES_TASK_AREA = 34;
    private static final int INDEX_MESSAGE_START = 0;
    private static final int INDEX_VIEW_COUNT = 5;
    private static final int INDEX_HEADER_START = 6;
    private static final int INDEX_VIEW_START = 9;
    private static final int INDEX_FOOTER = 38;

    private ConsoleReader consoleInstance;
    private PrintWriter out;

    /* Local Buffer Variables */
    private String[] consoleBuffer;
    private String taskViewBuffer;
    private ArrayList<String> taskBuffer;
    private int lastTaskIndex;
    private static final int NOT_IN_BUFFER = -1;

    /**
     * Default constructor of WinInterface that initialises all the required
     * class variable and setup the consoleReader instance.
     */
    protected WinInterface() {
        consoleBuffer = new String[LINES_RESPONSE_AREA + LINES_TASK_AREA];
        taskBuffer = new ArrayList<String>();
        taskViewBuffer = "";
        lastTaskIndex = NOT_IN_BUFFER;

        try {
            consoleInstance = setupConsoleReader();
            out = new PrintWriter(consoleInstance.getOutput());
            Process p = Runtime.getRuntime().exec(COMMAND_SET_CONSOLE);
            p.waitFor();
        } catch (IOException e1) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_SETUP, e1);
        } catch (InterruptedException e2) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CMD, e2);
        }
    }

    /**
     * Handles the interface of the program. It prompts input from user and
     * passes to the parser. It then proceeds to print the output based on the
     * Response object returned from the Parser.
     */
    @Override
    public void handleInterface() {
        try {
            printOutput(getWelcomeMessage());
            while (true) {
                String line = consoleInstance.readLine(DEFAULT_PROMPT);
                Response res = Parser.getInstance().parseInput(line);
                assert res != null : MESSAGE_ASSERT_RESPONSE;
                printOutput(res);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
        }
    }

    /**
     * Displays a prompt midway through an execution of a command and request an
     * input from the user which will be returned to the command execution
     * flow.
     *
     * @param prompts String literals to be displayed to the user
     * @return response by the user
     */
    @Override
    public String requestPrompt(String... prompts) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String p : prompts) {
                if (sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(p);
            }
            printOutput(new Response(sb.toString(), true));
            return consoleInstance.readLine(DEFAULT_PROMPT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            return MESSAGE_ERROR_CR_IOEXCEPTION;
        }
    }

    /**
     * Setup and instantiates the ConsoleReader from jLine.
     *
     * @return ConsoleReader object
     * @throws IOException occurs when ConsoleReader has problem with output
     */
    private ConsoleReader setupConsoleReader() throws IOException {
        ConsoleReader cr = new ConsoleReader();
        cr.clearScreen();
        cr.setPrompt(DEFAULT_PROMPT);
        setCompleter(cr);
        setKeybinding(cr);
        return cr;
    }

    /**
     * Sets up the auto-complete feature by specifying the required completers
     * into the consoleReader object.
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
     * Maps the required keyboard keys to perform the required functionality
     * when triggered.
     *
     * @param cr ConsoleReader object
     */
    private void setKeybinding(ConsoleReader cr) {
        /* Declare ANSI keycode for each required key */
        final String pageUp = "\033[5~";
        final String pageDown = "\033[6~";
        final String insert = "\033[2~";

        cr.getKeys().bind(pageUp, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrollTaskUp();
            }
        });

        cr.getKeys().bind(pageDown, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrollTaskDown();
            }
        });

        cr.getKeys().bind(insert, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleTimeFormat();
            }
        });
    }

    /**
     * Scrolls the task area of the interface upwards if possible.
     */
    private void scrollTaskUp() {
        if (canScrollUp()) {
            int j = --lastTaskIndex;
            for (int i = INDEX_FOOTER - 1; i >= INDEX_VIEW_START; i--) {
                consoleBuffer[i] = taskBuffer.get(j--);
            }
            consoleBuffer[INDEX_FOOTER] = getFooter();

            try {
                bufferScreen();
                consoleInstance.restoreLine(consoleInstance.getPrompt(),
                                            consoleInstance.getCursorBuffer()
                                                .current());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            }
        }
    }

    /**
     * Scrolls the task area of the interface downwards if possible.
     */
    private void scrollTaskDown() {
        if (canScrollDown()) {
            int j = ++lastTaskIndex;
            for (int i = INDEX_FOOTER - 1; i >= INDEX_VIEW_START; i--) {
                consoleBuffer[i] = taskBuffer.get(j--);
            }
            consoleBuffer[INDEX_FOOTER] = getFooter();

            try {
                bufferScreen();
                consoleInstance.restoreLine(consoleInstance.getPrompt(),
                                            consoleInstance.getCursorBuffer()
                                                .current());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            }
        }
    }

    /**
     * Returns a boolean that represents whether a scroll up function can be
     * done. <p/> return true if task data can be scrolled up else false
     *
     * @return true if task data can be scrolled up else false
     */
    private boolean canScrollUp() {
        return lastTaskIndex != NOT_IN_BUFFER &&
               lastTaskIndex >= INDEX_FOOTER - INDEX_VIEW_START;
    }

    /**
     * Returns a boolean that represents whether a scroll down function can be
     * done.
     *
     * @return true if task data can be scrolled down else false
     */
    private boolean canScrollDown() {
        return lastTaskIndex != NOT_IN_BUFFER &&
               lastTaskIndex < taskBuffer.size() - 1 &&
               taskBuffer.size() > INDEX_FOOTER - INDEX_VIEW_START;
    }

    /**
     * Toggles the formatter date format between 12 hour and 24 hour format.
     */
    private void toggleTimeFormat() {
        Formatter.toggleTimeFormat();
        Response r = Command.getPreviousDisplayCommand().safeExecute();
        String toggleMessage = Formatter.is12HourFormat() ? MESSAGE_SET_12HOUR
                                                          : MESSAGE_SET_24HOUR;
        r.setMessages(ColorFormatter.format(toggleMessage, Color.CYAN));
        try {
            printOutput(r);
            consoleInstance.restoreLine(consoleInstance.getPrompt(),
                                        consoleInstance.getCursorBuffer()
                                            .current());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
        }
    }

    /**
     * Returns a Response object that contains the welcome screen that shows the
     * relevant information when user first execute RubberDuck.
     *
     * @return Response object that contains the welcome information
     */
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
     * Updates the console buffer based on the Response object and buffer the
     * terminal to reflect output changes.
     *
     * @param res Response object to update the buffer with
     * @throws IOException occurs when ConsoleReader encounters an I/O error
     */
    private void printOutput(Response res) throws IOException {
        updateBuffer(res);
        bufferScreen();
    }

    /**
     * Updates the console buffer based on the Response object provided in the
     * arguments.
     *
     * @param res Response object to update the buffer with
     */
    private void updateBuffer(Response res) {
        if (res.isOverwrite()) {
            /* if isOverwrite is true, display only what is in the messages */
            String[] messages = res.getMessages();
            for (int i = 0; i < consoleBuffer.length; i++) {
                if (i < messages.length) {
                    consoleBuffer[i] = messages[i - INDEX_MESSAGE_START];
                } else {
                    consoleBuffer[i] = "";
                }
            }
            lastTaskIndex = NOT_IN_BUFFER;
        } else {
            /* Update messages into the messages area in the Buffer */
            String[] messages = res.getMessages();
            for (int i = INDEX_MESSAGE_START; i < INDEX_VIEW_COUNT; i++) {
                if (i - INDEX_MESSAGE_START < messages.length) {
                    consoleBuffer[i] = messages[i - INDEX_MESSAGE_START];
                } else {
                    consoleBuffer[i] = "";
                }
            }

            /* Update viewCount into the viewCount area in the Buffer */
            String viewCount = res.getViewCount();
            if (viewCount != null) {
                taskViewBuffer = viewCount;
                consoleBuffer[INDEX_VIEW_COUNT] = viewCount;
            } else if (lastTaskIndex == NOT_IN_BUFFER) {
                consoleBuffer[INDEX_VIEW_COUNT] = taskViewBuffer;
            }

            /* Update viewData into the tasks area in the Buffer */
            String viewData = res.getViewData();
            if (viewData != null || lastTaskIndex == NOT_IN_BUFFER) {
                if (viewData != null) {
                    String[] viewSplit = res.getViewData()
                        .split(System.lineSeparator());

                    taskBuffer =
                        new ArrayList<String>(Arrays.asList(viewSplit));
                }

                String header = String.format(Formatter.FORMAT_TABLE, "ID",
                                              "Done", "Task", "Date");

                /* Format the table header portion of the GUI */
                consoleBuffer[INDEX_HEADER_START] = SEPARATOR_BORDER;
                consoleBuffer[INDEX_HEADER_START + 1] = header;
                consoleBuffer[INDEX_HEADER_START + 2] = SEPARATOR_BORDER;

                for (int i = INDEX_VIEW_START; i < INDEX_FOOTER; i++) {
                    if (i - INDEX_VIEW_START < taskBuffer.size()) {
                        consoleBuffer[i] = taskBuffer.get(i - INDEX_VIEW_START);
                        lastTaskIndex = i - INDEX_VIEW_START;
                    } else {
                        consoleBuffer[i] = "";
                    }
                }

                /* Update and format the table footer of the GUI */
                consoleBuffer[INDEX_FOOTER] = getFooter();
            }
        }
    }

    /**
     * Returns a String representation of the footer depending on the task data
     * and lastTaskIndex.
     *
     * @return correct footer based on scrollability of task data
     */
    private String getFooter() {
        if (canScrollDown() && canScrollUp()) {
            return SEPARATOR_PAGEUPDOWN;
        } else if (canScrollDown()) {
            return SEPARATOR_PAGEDOWN;
        } else if (canScrollUp()) {
            return SEPARATOR_PAGEUP;
        } else {
            return SEPARATOR_BORDER;
        }
    }

    /**
     * Clears the screen of the console and output everything in the console
     * buffer into PrintWriter and proceed to flush to reflect changes.
     *
     * @throws IOException occurs when ConsoleReader encounters an I/O error
     */
    private void bufferScreen() throws IOException {
        consoleInstance.clearScreen();
        for (String s : consoleBuffer) {
            out.println(s);
        }
        out.flush();
    }
}
