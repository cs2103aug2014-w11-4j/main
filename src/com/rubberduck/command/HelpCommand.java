package com.rubberduck.command;

import java.io.IOException;

import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

/**
 * Concrete Command Class that can be executed to show the list of available
 * commands and their description.
 *
 * @author hooitong
 */
public class HelpCommand extends Command {
    private static final String HELP_ALL_HEADER = "Here are for the available commands in RubberDuck.";
    private static final String HELP_ALL_FORMAT = "%-15s%-65s";
    private static final String HELP_SPECIFIC_HEADER = "More information about your queried command.";
    private static final String HELP_SPECIFIC_FORMAT = "%-15s%-65s";
    private static final String[][] COMMANDS = {
        { "view", "View your agenda given a date range or \"all\".", "[date | all]" },
        { "search", "Search for tasks related to the given keyword.", "[keyword]" },
        { "add", "Add a new task of provided description with optional date.", "<description> [date...]" },
        { "delete", "Delete a task from the system given task ID.", "<task id>" },
        { "update", "Update task given task ID and new information.", "<task id> [description] [date...]" },
        { "undo", "Undo your previous action.", "-" },
        { "redo", "Redo your undone action.", "-" },
        { "mark", "Mark any task to complete/incomplete given task ID.", "<task id>" },
        { "confirm", "Confirm any tentative task given task ID and date ID.", "<task id> <date id>" },
        { "clear", "Clear the screen of RubberDuck.", "-" },
        { "exit", "Exit from RubberDuck.", "-"},
        { "help", "Get help information on commands available and specifics.", "[command]" }
    };
    private static final int COMMANDS_NAME = 0;
    private static final int COMMANDS_INFO = 1;
    private static final int COMMANDS_ARG = 2;

    private boolean isSpecific;
    private String type;

    /**
     *
     * @param isSpecific
     * @param type
     */
    public HelpCommand(boolean isSpecific, String type) {
        this.isSpecific = isSpecific;
        this.type = type;
    }

    /**
     * Shows the available commands for the end user in the system.
     *
     * @return a String object containing all the commands available
     */
    @Override
    public String execute() throws IOException {
        StringBuilder sb = new StringBuilder();

        if (isSpecific) {
            CommandType ct = Command.CommandType.getCommandType(type);
            if (ct == CommandType.INVALID) {
                sb.append("No such command/alias.");
            } else {
                sb.append(ColorFormatter.format(HELP_SPECIFIC_HEADER,
                        Color.YELLOW));
                sb.append(System.lineSeparator());
                sb.append(String.format(HELP_SPECIFIC_FORMAT,
                        COMMANDS[ct.ordinal()][COMMANDS_NAME],
                        COMMANDS[ct.ordinal()][COMMANDS_ARG]));
            }
        } else {
            sb.append(ColorFormatter.format(HELP_ALL_HEADER, Color.YELLOW));
            sb.append(System.lineSeparator());
            for (int i = 0; i < COMMANDS.length; i++) {
                String cmdLine = String.format(HELP_ALL_FORMAT,
                        COMMANDS[i][COMMANDS_NAME], COMMANDS[i][COMMANDS_INFO]);
                sb.append(cmdLine);
                if (i != COMMANDS.length - 1) {
                    sb.append(System.lineSeparator());
                }
            }
        }
        return sb.toString();
    }
}
