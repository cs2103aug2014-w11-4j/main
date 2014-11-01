package com.rubberduck.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rubberduck.io.DatabaseManager;
import com.rubberduck.logic.Task;

/**
 * Class that represents a command object where it stores the type of command it
 * is and all its arguments.
 *
 * @author hooitong
 */
public abstract class Command {
    /* Enum type to store all types of command and their possible variations */
    public enum CommandType {
        /*
         * use var args to populate all possible variations for each command
         * type
         */
        VIEW("view", "display"), SEARCH("find", "lookup", "search"), ADD("add",
                "insert", "ins", "new"), DELETE("delete", "remove"), UPDATE(
                "change", "update", "edit"), UNDO("undo", "ud"), REDO("redo",
                "rd"), MARK("mark", "completed", "done"), CONFIRM("confirm"),
        SYNC("sync"), CLEAR("cls", "clear"), EXIT("exit", "quit"), HELP("?",
                "help"), INVALID;

        private List<String> tags;
        private static final Map<String, CommandType> ALIAS_MAP = new HashMap<String, CommandType>();

        /**
         * Private constructor that accept literals and instantiate as List of
         * String.
         *
         * @param tags String literals
         */
        private CommandType(String... tags) {
            this.tags = Arrays.asList(tags);
        }

        /**
         * Initialize and populate the tagMap for other methods.
         */
        static {
            for (CommandType command : CommandType.values()) {
                for (String tag : command.tags) {
                    ALIAS_MAP.put(tag, command);
                }
            }
        }

        /**
         * Return the appropriate CommandType enum based on user input.
         *
         * @param input the input to retrieve command
         * @return the correct CommandType enum based on input
         */
        public static CommandType getCommandType(String input) {
            if (input == null || input.isEmpty()) {
                return CommandType.INVALID;
            }

            CommandType cmd = ALIAS_MAP.get(input.toLowerCase());

            if (cmd == null) {
                return CommandType.INVALID;
            } else {
                return cmd;
            }
        }

        /**
         * Retrieve all available alias found in Command.
         *
         * @return Set object that contains all alias
         */
        public static Set<String> getAlias() {
            return ALIAS_MAP.keySet();
        }

    }

    /* Details about the DataStore/DatabaseManager */
    private static final String MESSAGE_ERROR_DATABASE_IOEXCEPTION = "Exception has occured when accessing local storage.";
    private static final String DATABASE_NAME = "database.xml";
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static ArrayList<Long> displayedTasksList = new ArrayList<Long>();
    private static Command previousDisplayCommand;
    private static DatabaseManager<Task> dbManager;

    /**
     * Start the database, if not found new database will be created.
     *
     * @return states if the database has been started successfully
     */
    public static boolean startDatabase() {
        try {
            dbManager = new DatabaseManager<Task>(CURRENT_DIRECTORY
                    + File.separator + DATABASE_NAME);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return false;
        }
        return true;
    }

    /**
     * Getter method for LOGGER.
     *
     * @return Logger instance
     */
    private static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Getter method for displayedTaskLists.
     *
     * @return ArrayList<Long> instance
     */
    public static ArrayList<Long> getDisplayedTasksList() {
        return displayedTasksList;
    }

    /**
     * Getter method for previousDisplayCommand.
     *
     * @return Command object of type ViewCommand or SearchCommand
     */
    protected static Command getPreviousDisplayCommand() {
        assert previousDisplayCommand != null : "Should not be null";
        return previousDisplayCommand;
    }

    /**
     * Setter method for previousDisplayCommand.
     *
     * @param c ViewCommand or SearchCommand object
     */
    public static void setPreviousDisplayCommand(Command c) {
        if (c instanceof ViewCommand || c instanceof SearchCommand) {
            previousDisplayCommand = c;
        }

        assert false : "Must only be VIEW or SEARCH.";
    }

    /**
     * Getter method for dbManager.
     *
     * @return DatabaseManager<Task> instance
     */
    public static DatabaseManager<Task> getDbManager() {
        return dbManager;
    }

    /**
     * Check whether given task ID is being displayed.
     *
     * @param displayedId the task ID
     * @return true when it is being displayed else false
     * @author Zhao Hang
     */
    public static boolean isValidDisplayedId(int displayedId) {
        return !(displayedId > displayedTasksList.size() || displayedId <= 0 || displayedTasksList.get(displayedId - 1) == -1);
    }

    /**
     * Execute the implemented execute in respective concrete class and catch
     * any exception if occur.
     *
     * @return String response after execution
     */
    public String safeExecute() {
        try {
            return execute();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return MESSAGE_ERROR_DATABASE_IOEXCEPTION;
        }
    }

    /**
     * Abstract method for implementation by concrete class to execute logic.
     *
     * @return String response after execution
     * @throws IOException thrown if DBManager encounter I/O problems
     */
    protected abstract String execute() throws IOException;
}
