package com.rubberduck.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rubberduck.DatabaseManager;
import com.rubberduck.Task;

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
                "rd"), MARK("mark", "completed", "done"), CONFIRM("confirm"), HELP(
                "?", "help"), CLEAR("cls", "clear"), EXIT("exit", "quit"), INVALID;

        private List<String> tags;
        private static final Map<String, CommandType> tagMap = new HashMap<String, CommandType>();

        private CommandType(String... tags) {
            this.tags = Arrays.asList(tags);
        }

        /**
         * Method to initialize/populate the tagMap for other methods.
         */
        static {
            for (CommandType command : CommandType.values()) {
                for (String tag : command.tags) {
                    tagMap.put(tag, command);
                }
            }
        }

        /**
         * Method used to return the command type based on user input
         *
         * @param input the input to retrieve command
         * @return the correct command type based on input
         */
        public static CommandType getCommandType(String input) {
            if (input == null || input.isEmpty()) {
                return CommandType.INVALID;
            }

            CommandType cmd = tagMap.get(input.toLowerCase());

            if (cmd == null) {
                return CommandType.INVALID;
            } else {
                return cmd;
            }
        }

    }

    /* Details about the DataStore/DatabaseManager */
    private static final String MESSAGE_ERROR_DATABASE_IOEXCEPTION = "Exception has occured when accessing local storage.";
    private static final String DATABASE_NAME = "database.xml";
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static ArrayList<Long> displayedTasksList = new ArrayList<Long>();
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

    public static Logger getLogger() {
        return LOGGER;
    }

    public static ArrayList<Long> getDisplayedTasksList() {
        return displayedTasksList;
    }

    public static boolean isValidDisplayedId(int displayedId) {
        return !(displayedId > displayedTasksList.size() || displayedId <= 0 || displayedTasksList.get(displayedId - 1) == -1);
    }

    public static DatabaseManager<Task> getDbManager() {
        return dbManager;
    }

    public String safeExecute() {
        try {
            return execute();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, MESSAGE_ERROR_DATABASE_IOEXCEPTION, e);
            return MESSAGE_ERROR_DATABASE_IOEXCEPTION;
        }
    }

    protected abstract String execute() throws IOException;

}
