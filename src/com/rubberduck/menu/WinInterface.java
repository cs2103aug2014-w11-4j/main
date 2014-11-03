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
        "MODE.COM CON COLS=81 LINES=41";
    protected static final String MESSAGE_ERROR_CMD =
        "Interrupted when executing console setup command.";

    private static final String SEPARATOR_BORDER =
        "--------------------------------------------------------------------------------";
    private static final String SEPARATOR_PAGEUP =
        "-------------------[Page Up]----------------------------------------------------";
    private static final String SEPARATOR_PAGEDOWN =
        "--------------------------------------------------[Page Down]-------------------";
    private static final String SEPARATOR_PAGEUPDOWN =
        "-------------------[Page Up]----------------------[Page Down]-------------------";

    /* Margins & Sizes for Terminal Buffer */
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

    /**
     * Default constructor of WinInterface that initialises all the class
     * variable and setup the consoleReader instance.
     */
    protected WinInterface() {
        consoleBuffer = new String[LINES_RESPONSE_AREA + LINES_TASK_AREA];
        taskBuffer = new ArrayList<String>();
        taskViewBuffer = "";
        lastTaskIndex = -1;
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
     * Handles the interface of the program. It prompts from user and calls the
     * parser to determine the command to be executed. It then proceed to
     * execute the returned command.
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

            }
        }
    }

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

            }
        }
    }

    /**
     * Used to show the welcome screen and relevant information when user first
     * execute the program.
     *
     * @return String to display as welcome message
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
     * Updated the console buffer based on the user's response and flush the
     * terminal buffer to reflect changes.
     */
    private void printOutput(Response res) throws IOException {
        updateBuffer(res);
        bufferScreen();
    }

    private void updateBuffer(Response res) {
        if (res.isOverwrite()) {
            String[] messages = res.getMessages().split(System.lineSeparator());
            for (int i = INDEX_MESSAGE_START; i < consoleBuffer.length; i++) {
                if (i - INDEX_MESSAGE_START < messages.length) {
                    consoleBuffer[i] = messages[i - INDEX_MESSAGE_START];
                } else {
                    consoleBuffer[i] = "";
                }
            }

            lastTaskIndex = -1;
        } else {
            String[] messages = res.getMessages().split(System.lineSeparator());
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
            } else if (lastTaskIndex == -1) {
                consoleBuffer[INDEX_VIEW_COUNT] = taskViewBuffer;
            }

            String viewData = res.getViewData();
            if (viewData != null || lastTaskIndex == -1) {

                if (viewData != null) {
                    String[] viewSplit =
                        res.getViewData().split(System.lineSeparator());

                    taskBuffer =
                        new ArrayList<String>(Arrays.asList(viewSplit));
                }

                String header =
                    String.format("%-7s%-6s%-43s%-24s", "ID", "Done",
                                  "Task", "Date");
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

                consoleBuffer[INDEX_FOOTER] = getFooter();
            }
        }
    }

    /**
     * @return correct footer based on scrollability
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

    private boolean canScrollUp() {
        return lastTaskIndex != -1 &&
               lastTaskIndex >= INDEX_FOOTER - INDEX_VIEW_START;
    }

    private boolean canScrollDown() {
        return lastTaskIndex != -1 && lastTaskIndex < taskBuffer.size() - 1 &&
               taskBuffer.size() >= INDEX_FOOTER - INDEX_VIEW_START;
    }

    /**
     * Output everything in the console buffer into PrintWriter and flush it to
     * terminal.
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
     *
     */
    @Override
    public void clearScreen() {

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
