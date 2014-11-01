package com.rubberduck;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.rubberduck.command.Command;
import com.rubberduck.menu.MenuInterface;

/**
 * RubberDuck is a CLI Task Manager that stores user's task into a text file and
 * helps the user to handle all his/her tasks via this application.
 *
 * This class is the main class used to start and initialize the program.
 */
//@author A0111736M
public class RubberDuck {

    /* Static variables used to store information about logging */
    private static final String DATESTAMP_FORMAT = "dd-MM-yyyy_HH-mm-ss";
    private static final String LOG_DIRECTORY = "logs/";
    private static final String LOG_FILENAME = "%s.log";

    /**
     * Main method of application as well as the entry point.
     */
    public static void main() {
        setupGlobalLogger();
        Command.startDatabase();
        MenuInterface.getInstance().handleInterface();
    }

    /**
     * Grabs the global logger and setup a FileHandler to create a log file. It
     * will then set it as the default logging file by the Logger.
     */
    public static void setupGlobalLogger() {
        /* Suppress logger to Console using rootLogger */
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        /* Setup file handler */
        try {
            new File(LOG_DIRECTORY).mkdirs();
            DateFormat dateFormat = new SimpleDateFormat(DATESTAMP_FORMAT,
                    Locale.US);
            Calendar cal = Calendar.getInstance();
            String currentTime = dateFormat.format(cal.getTime());
            FileHandler fileHandler = new FileHandler(LOG_DIRECTORY
                    + String.format(LOG_FILENAME, currentTime));
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
