package com.rubberduck.menu;

import com.rubberduck.command.Command;
import com.rubberduck.logic.Parser;
import com.rubberduck.menu.ColorFormatter.Color;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class focuses on handling the user interface of the entire application
 * which accepts the user's input, call the parser and execute the command
 * returned from the parser.
 */
//@author A0111736M
public class MenuInterface {

    /* Global logger to log information and exception. */
    private static final Logger LOGGER =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String MESSAGE_WELCOME =
        "Welcome to RubberDuck. Here's your agenda for today.";
    private static final String MESSAGE_HELP =
        "If you need a list of commands, type ? or help.";
    private static final String MESSAGE_ERROR_CR_IOEXCEPTION =
        "Problem with ConsoleReader (IO).";
    private static final String MESSAGE_PROMPT =
        "Press [Enter] to continue...";
    private static final String DEFAULT_PROMPT =
        ">";
    private static final String WELCOME_EXECUTE =
        "view today";

    private static MenuInterface menuInstance;

    private ConsoleReader consoleInstance;

    /**
     * Private Constructor of MenuInterface for Singleton Pattern.
     */
    private MenuInterface() {
    }

    /**
     * Retrieves the singleton instance of the MenuInterface.
     *
     * @return instance of Parser
     */
    public static MenuInterface getInstance() {
        if (menuInstance == null) {
            menuInstance = new MenuInterface();
        }

        return menuInstance;
    }

    /**
     * Handles the interface of the program. It prompts from user and calls the
     * parser to determine the command to be executed. It then proceed to
     * execute the returned command.
     */
    public void handleInterface() {
        try {
            consoleInstance = setupConsoleReader();
            showToUser(getWelcomeMessage());
            while (true) {
                String line = consoleInstance.readLine(DEFAULT_PROMPT);
                Command userCommand = Parser.getInstance().parse(line);
                String response = userCommand.safeExecute();
                showToUser(response);
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
        return cr;
    }

    /**
     * Used to show the welcome screen and relevant information when user first
     * execute the program.
     *
     * @return String to display as welcome message
     */
    private String getWelcomeMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(MESSAGE_WELCOME);
        sb.append(System.lineSeparator());

        Command userCommand = Parser.getInstance().parse(WELCOME_EXECUTE);
        sb.append(userCommand.safeExecute());
        sb.append(System.lineSeparator());

        sb.append(ColorFormatter.format(MESSAGE_HELP, Color.YELLOW));

        return sb.toString();
    }

    /**
     * Outputs a string object to the ConsoleReader instance which will be
     * visible to the user.
     *
     * @param s String object to be displayed
     * @throws IOException occurs when ConsoleReader has problem with output
     */
    private void showToUser(String s) throws IOException {
        consoleInstance.clearScreen();
        String[] buffer = s.split(System.lineSeparator());
        int bufferHeight = consoleInstance.getTerminal().getHeight() - 2;
        for (int i = 0; i < buffer.length; i++) {
            consoleInstance.println(buffer[i]);
            if (i >= bufferHeight) {
                consoleInstance.readLine(ColorFormatter.format(MESSAGE_PROMPT,
                                                               Color.CYAN));
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
    public String requestPrompt(String... prompt) {
        try {
            StringBuilder sb = new StringBuilder();
            for (String p : prompt) {
                if (sb.length() > 0) {
                    sb.append(System.lineSeparator());
                }
                sb.append(p);
            }
            showToUser(sb.toString());
            return consoleInstance.readLine(DEFAULT_PROMPT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            return MESSAGE_ERROR_CR_IOEXCEPTION;
        }
    }

    /**
     * Return the ConsoleReader instance back to caller.
     *
     * @return ConsoleReader object
     */
    public ConsoleReader getConsoleInstance() {
        return consoleInstance;
    }
}
