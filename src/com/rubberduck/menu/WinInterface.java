package com.rubberduck.menu;

import com.rubberduck.command.Command;
import com.rubberduck.logic.Parser;
import com.rubberduck.menu.ColorFormatter.Color;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * This class handles the user interface of the application that is running on
 * Windows platform. This will handle all input from the user and show the
 * required response back to the user.
 */
//@author A0111736M
public class WinInterface extends MenuInterface {

    private static final String COMMAND_SET_CONSOLE =
        "mode.com con cols=81 lines=41";
    protected static final String MESSAGE_ERROR_CMD =
        "Interrupted when executing console setup command.";

    /* Separator Strings to format mock GUI */
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

    /* ConsoleReader instances and Writers */
    private ConsoleReader consoleInstance;
    private PrintWriter out;

    /* Local Buffer Variables */
    private String[] consoleBuffer;
    private String taskViewBuffer;
    private ArrayList<String> taskBuffer;
    private int lastTaskIndex;
    private static final int NOT_IN_BUFFER = -1;

    /**
     * Default constructor of WinInterface that initialises all the class
     * variable and setup the consoleReader instance.
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
     * Handles the interface of the program. It prompts from user and passes to
     * the parser to determine the command to be executed. It then proceed to
     * execute the returned command and print the response to the user.
     */
    @Override
    public void handleInterface() {
        try {
            printOutput(getWelcomeMessage());
            while (true) {
                String line = consoleInstance.readLine(DEFAULT_PROMPT);
                Command userCommand = Parser.getInstance().parse(line);
                Response res = userCommand.safeExecute();
                printOutput(res);
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
    private ConsoleReader setupConsoleReader() throws IOException {
        ConsoleReader cr = new ConsoleReader();
        cr.clearScreen();
        cr.setPrompt(DEFAULT_PROMPT);
        cr.addCompleter(new StringsCompleter(Command.CommandType.getAlias()));
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
        final String pageUp = "\033[5~";
        final String pageDown = "\033[6~";

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
    }

    /**
     * Scroll the task area upwards if possible.
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
                                            consoleInstance.getCursorBuffer().
                                                current());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            }
        }
    }

    /**
     * Scroll the task area downwards if possible.
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
                                            consoleInstance.getCursorBuffer().
                                                current());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            }
        }
    }

    /**
     * Used to get the response which contains the welcome screen that shows the
     * relevant information when user first execute the program.
     *
     * @return Response object that contains the welcome information
     */
    private Response getWelcomeMessage() {
        StringBuilder messages = new StringBuilder();
        messages.append(MESSAGE_WELCOME);
        messages.append(System.lineSeparator());
        messages.append(ColorFormatter.format(MESSAGE_HELP, Color.YELLOW));

        Command userCommand = Parser.getInstance().parse(WELCOME_EXECUTE);
        Response res = userCommand.safeExecute();
        res.setMessages(messages.toString());
        return res;
    }

    /**
     * Update the console buffer based on the user's response and flush the
     * terminal buffer to reflect output changes.
     */
    private void printOutput(Response res) throws IOException {
        updateBuffer(res);
        bufferScreen();
    }

    /**
     * Update console buffer based on the response provided in the arguments.
     *
     * @param res Response object to update with
     */
    private void updateBuffer(Response res) {
        if (res.isOverwrite()) {
            /* Since isOverwrite is true, display only what is in the response */
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
            String[] messages = res.getMessages();
            for (int i = INDEX_MESSAGE_START; i < INDEX_VIEW_COUNT; i++) {
                if (i - INDEX_MESSAGE_START < messages.length) {
                    consoleBuffer[i] = messages[i - INDEX_MESSAGE_START];
                } else {
                    consoleBuffer[i] = "";
                }
            }

            String viewCount = res.getViewCount();
            if (viewCount != null) {
                taskViewBuffer = viewCount;
                consoleBuffer[INDEX_VIEW_COUNT] = viewCount;
            } else if (lastTaskIndex == NOT_IN_BUFFER) {
                consoleBuffer[INDEX_VIEW_COUNT] = taskViewBuffer;
            }

            String viewData = res.getViewData();
            if (viewData != null || lastTaskIndex == NOT_IN_BUFFER) {
                if (viewData != null) {
                    String[] viewSplit =
                        res.getViewData().split(System.lineSeparator());

                    taskBuffer =
                        new ArrayList<String>(Arrays.asList(viewSplit));
                }

                String header =
                    String.format("%-7s%-6s%-43s%-24s", "ID", "Done", "Task",
                                  "Date");

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
     * Get a String representation of the footer depending on the task data.
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
     * Returns a boolean that represents whether a scroll up function can be
     * done.
     *
     * return true if task data can be scrolled up else false
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
     * Clear the screen of the console and output everything in the console
     * buffer into PrintWriter.
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
            printOutput(new Response(sb.toString(), true));
            return consoleInstance.readLine(DEFAULT_PROMPT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            return MESSAGE_ERROR_CR_IOEXCEPTION;
        }
    }
}
