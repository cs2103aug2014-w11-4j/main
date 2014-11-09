package com.rubberduck.menu;

import java.util.logging.Logger;

//@author A0111736M

/**
 * This abstract class acts as the Factory class of the different interfaces
 * object. It handles the instantiation of a concrete MenuInterface class
 * dependent on the operating system the user is on. It also provides the
 * necessary attributes and methods for concrete classes to implement and handle
 * user input and output.
 */
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
     * Handles the interface of RubberDuck involving handling inputs and outputs
     * for the user as required.
     */
    public abstract void handleInterface();

    /**
     * Prompts user for an input and return the input back to caller.
     *
     * @param prompts String literals to prompt the user
     * @return user input
     */
    public abstract String requestPrompt(String... prompts);
}
