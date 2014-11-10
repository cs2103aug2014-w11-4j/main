package rubberduck.logic.command;

import java.util.Set;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

/**
 * Concrete Command Class that can be executed to show the list of available
 * commands and their description.
 */
//@author A0111736M
public class HelpCommand extends Command {

    private static final String HELP_ALL_HEADER =
        "Here are for the available commands in RubberDuck.";
    private static final String HELP_ALL_FORMAT =
        "%-15s%-65s";
    private static final String HELP_SPECIFIC_HEADER =
        "More information about your queried command.";
    private static final String HELP_SPECIFIC_FORMAT =
        "%-15s%-65s";
    private static final String HELP_SPECIFIC_ALIAS =
        " \nSupported Alias";
    private static final String[][] COMMANDS = {
        {"view", "View your agenda given a date range or \"all\".",
         "[date | all | overdue] [deadline] [float] [schedule]"},
        {"search", "Search for tasks related to the given keyword.",
         "[keyword]"},
        {"add", "Add a new task of provided description with optional date.",
         "<description> [date | date range] | [date range or...]"},
        {"delete", "Delete a task from the system given task ID.",
         "<task id>"},
        {"update", "Update task given task ID and new information.",
         "<task id> [description] [date | date range] | [date range or...]"},
        {"undo", "Undo your previous action.",
         "-"},
        {"redo", "Redo your undone action.",
         "-"},
        {"mark", "Mark any task to complete/incomplete given task ID.",
         "<task id>"},
        {"confirm", "Confirm any tentative task given task ID and date ID.",
         "<task id> <date id>"},
        {"sync", "Initiate 2-way synchronisation with Google.",
         "[force] [push | pull] | [logout]"},
        {"clear", "Clear the screen of RubberDuck.", "-"},
        {"exit", "Exit from RubberDuck.", "-"},
        {"help", "Get help information on commands available and specifics.",
         "[command]"}
    };

    private static final int COMMANDS_NAME = 0;
    private static final int COMMANDS_INFO = 1;
    private static final int COMMANDS_ARG = 2;

    private boolean isSpecific;
    private String type;

    /**
     * Public constructor for HelpCommand that accepts a boolean and a command
     * type as String.
     *
     * @param isSpecific if true, users want a specific command information
     * @param type       null if isSpecific is false, else must not be null if
     *                   isSpecific is true
     */
    public HelpCommand(boolean isSpecific, String type) {
        this.isSpecific = isSpecific;
        this.type = type;
    }

    /**
     * Shows either the list of available commands or specific details about a
     * command in RubberDuck.
     *
     * @return a Response object containing the command information requested
     */
    @Override
    public Response execute() {
        StringBuilder sb = new StringBuilder();
        if (isSpecific) {
            showSpecific(sb);
        } else {
            showAll(sb);
        }
        return new Response(sb.toString(), true);
    }

    /**
     * Retrieves specific details about a Command in RubberDuck and append to
     * provided StringBuilder.
     *
     * @param sb StringBuilder object
     */
    private void showSpecific(StringBuilder sb) {
        assert (sb != null);
        CommandType ct = CommandType.getCommandType(type);
        if (ct == CommandType.INVALID) {
            sb.append("No such command/alias.");
        } else {
            sb.append(ColorFormatter.format(HELP_SPECIFIC_HEADER,
                                            Color.YELLOW));
            sb.append(System.lineSeparator());
            sb.append(String.format(HELP_SPECIFIC_FORMAT, "Command Type",
                                    "Parameters"));
            sb.append(System.lineSeparator());
            sb.append(String.format(HELP_SPECIFIC_FORMAT,
                                    COMMANDS[ct.ordinal()][COMMANDS_NAME],
                                    COMMANDS[ct.ordinal()][COMMANDS_ARG]));
            sb.append(System.lineSeparator());
            sb.append(HELP_SPECIFIC_ALIAS);
            sb.append(System.lineSeparator());

            Set<String> aliasSet = CommandType.getAlias(ct);
            StringBuilder aliasBuilder = new StringBuilder();
            for (String alias : aliasSet) {
                aliasBuilder.append(alias);
                aliasBuilder.append("   ");
            }
            sb.append(aliasBuilder.toString());
        }
    }

    /**
     * Retrieves the entire list of commands and their respective description
     * and append to provided StringBuilder.
     *
     * @param sb StringBuilder object
     */
    private void showAll(StringBuilder sb) {
        assert (sb != null);
        sb.append(ColorFormatter.format(HELP_ALL_HEADER, Color.YELLOW));
        sb.append(System.lineSeparator());
        for (int i = 0; i < COMMANDS.length; i++) {
            String cmdLine = String.format(HELP_ALL_FORMAT,
                                           COMMANDS[i][COMMANDS_NAME],
                                           COMMANDS[i][COMMANDS_INFO]);
            sb.append(cmdLine);
            if (i != COMMANDS.length - 1) {
                sb.append(System.lineSeparator());
            }
        }
    }
}
