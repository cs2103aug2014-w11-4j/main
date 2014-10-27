package com.rubberduck.menu;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import com.rubberduck.command.Command;
import com.rubberduck.logic.Parser;
import com.rubberduck.menu.ColorFormatter.Color;

/**
 * This class focuses on handling the user interface of the entire application
 * which accepts the user's input, call the parser and calls the correct method
 * in the logic.
 *
 * @author hooitong
 *
 */
public class MenuInterface {
    private static final String MESSAGE_WELCOME = "Welcome to RubberDuck. Here's your agenda for today.";
    private static final String MESSAGE_HELP = "If you need a list of commands, type ? or help.";
    private static final String MESSAGE_ERROR_CR_IOEXCEPTION = "Problem with ConsoleReader (IO).";
    private static final String DEFAULT_PROMPT = ">";

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static MenuInterface menuInstance;

    private ConsoleReader consoleInstance;

    /**
     * Private Constructor for Singleton Implementation.
     */
    private MenuInterface() {
    }

    /**
     * Method that retrieves the singleton instance of the MenuInterface.
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
     * Method that handles the interface of the program. It prompts from user
     * and calls the parser to determine the command to be executed. It then
     * proceed to execute the given command if it is valid.
     *
     * @author Jason Sia
     */
    public void handleInterface() {
        try {
            consoleInstance = setupConsoleReader();
            showWelcome();
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
     * Method used to setup the ConsoleReader from jLine.
     *
     * @return ConsoleReader object
     */
    private ConsoleReader setupConsoleReader() throws IOException {
        ConsoleReader cr = new ConsoleReader();
        cr.clearScreen();
        cr.setPrompt(DEFAULT_PROMPT);
        cr.setPaginationEnabled(true);
        List<Completer> completors = new LinkedList<Completer>();
        completors.add(new StringsCompleter(Command.CommandType.getAlias()));

        for (Completer c : completors) {
            cr.addCompleter(c);
        }

        return cr;
    }

    /**
     * Method that is used to show the welcome screen and relevant information
     * when user first execute the program.
     *
     * @param out PrintWriter object
     */
    private void showWelcome() throws IOException {
        showToUser(MESSAGE_WELCOME);
        Command userCommand = Parser.getInstance().parse("view today");
        String response = userCommand.safeExecute();
        showToUser(response);
        showToUser(ColorFormatter.format(MESSAGE_HELP, Color.YELLOW));
    }

    /**
     * Method that outputs a string object to the PrintWriter object.
     *
     * @param s String object
     * @param out PrintWriter object
     */
    private void showToUser(String s) throws IOException {
        consoleInstance.println(s);
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
