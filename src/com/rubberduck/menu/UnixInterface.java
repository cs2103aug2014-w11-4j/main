package com.rubberduck.menu;

import com.rubberduck.command.Command;
import com.rubberduck.logic.Parser;
import com.rubberduck.menu.ColorFormatter.Color;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import java.io.IOException;
import java.util.logging.Level;

/**
 * This class handles the user interface of the application that is running on
 * Mac/Linux. This will handle all input from the user and show the required
 * response back to the user.
 */
//@author A0111736M
public class UnixInterface extends MenuInterface {

    private static final String MESSAGE_PROMPT =
        "Press [Enter] to continue...";

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
     * execute the returned command and print the returned string to the user.
     */
    @Override
    public void handleInterface() {
        try {
            //showToUser(getWelcomeMessage());
            while (true) {
                String line = consoleInstance.readLine(DEFAULT_PROMPT);
                Command userCommand = Parser.getInstance().parse(line);
                Response res = userCommand.safeExecute();
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
        cr.addCompleter(new StringsCompleter(Command.CommandType.getAlias()));
        return cr;
    }

    /**
     * Used to show the welcome screen and relevant information when user first
     * execute the program.
     *
     * @return String to display as welcome message
     */
    //@author A0111736M
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
    private void showToUser(Response s) throws IOException {
        consoleInstance.clearScreen();
        /*
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
        */
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
            //showToUser(sb.toString());
            return consoleInstance.readLine(DEFAULT_PROMPT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, MESSAGE_ERROR_CR_IOEXCEPTION, e);
            return MESSAGE_ERROR_CR_IOEXCEPTION;
        }
    }
}
