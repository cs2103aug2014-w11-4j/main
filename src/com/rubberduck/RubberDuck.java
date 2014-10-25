package com.rubberduck;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
 * This class is used to start and initialize the program.
 *
 * @author hooitong
 *
 */
public class RubberDuck {
    public static void main(String[] args) {
        setupGlobalLogger();
        Command.startDatabase();
        MenuInterface.getInstance().handleInterface();
    }

    public static void setupGlobalLogger() {
        /* Get global logger from application */
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        Logger rootLogger = Logger.getLogger("");
        /* Suppress logger to Console */
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        /* Handle minimum INFO logs */
        logger.setLevel(Level.INFO);

        /* Get today's date and time */
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String currentTime = dateFormat.format(cal.getTime());

        /* Setup file handler */
        try {
            new File("logs").mkdirs();
            FileHandler fileHandler = new FileHandler("logs/" + currentTime
                    + ".log");
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
