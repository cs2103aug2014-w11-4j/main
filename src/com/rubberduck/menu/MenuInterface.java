package com.rubberduck.menu;

import java.util.logging.Logger;

/**
 * This class focuses on handling the user interface of the entire application
 * which accepts the user's input, call the parser and execute the command
 * returned from the parser.
 */
//@author A0111736M
public abstract class MenuInterface {

    /* Global logger to log information and exception. */
    protected static final Logger LOGGER =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    protected static final String MESSAGE_WELCOME =
        "Welcome to RubberDuck. Here's your agenda for today.";
    protected static final String MESSAGE_HELP =
        "If you need a list of commands, type ? or help.";
    protected static final String MESSAGE_ERROR_CR_IOEXCEPTION =
        "Problem with ConsoleReader Operation (I/O).";
    protected static final String MESSAGE_ERROR_CR_SETUP =
        "Problem setting up ConsoleReader as it encounters I/O issues.";
    protected static final String DEFAULT_PROMPT =
        ">";
    protected static final String WELCOME_EXECUTE =
        "view today";

    public static final int CONSOLE_MAX_WIDTH = 80;

    private static MenuInterface menuInstance;

    /**
     * Retrieves the singleton instance of the MenuInterface. The MenuInterface
     * will be of type WinInterface if running on Windows, else it will be of
     * type UnixInterface.
     *
     * @return instance of MenuInterface
     */
    public static MenuInterface getInstance() {
        if (menuInstance == null) {
            String userOS = System.getProperty("os.name");
            if (userOS.toLowerCase().contains("win")) {
                menuInstance = new WinInterface();
            } else {
                menuInstance = new UnixInterface();
            }
        }
        return menuInstance;
    }

    /**
     * Clear the screen of the terminal.
     */
    public abstract void clearScreen();

    /**
     * Handle the interface of RubberDuck involving receiving inputs and outputs
     * as required.
     */
    public abstract void handleInterface();

    /**
     * Prompt user for an input and return the input back to caller.
     *
     * @param prompt String literals to prompt the user
     * @return user input
     */
    public abstract String requestPrompt(String... prompt);
}
