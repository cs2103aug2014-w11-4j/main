package com.rubberduck.command;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Concrete Command Class that can be executed to add a new task (floating,
 * deadline, schedule) into the database.
 */
//@author A0111794E
public class AddCommand extends Command {

    private static final String MESSAGE_ADD =
        "\"%s\" has been successfully added.";
    private static final String MESSAGE_ADD_CONFLICT =
        "\"%s\" has been successfully added.%nPlease note that there are conflicting task(s).";
    private static final String MESSAGE_ADD_PAST =
        "\"%s\" cannot be added as the end date has already passed.";
    private static final String JOURNAL_MESSAGE_ADD =
        "Added task \"%s\"";
    private static final String MESSAGE_ERROR_WRONG_TASK_TYPE =
        "You have input an invalid task type.";

    private String description;
    private ArrayList<DatePair> datePairs;

    /**
     * Getter method for description.
     *
     * @return description as String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter method for datePairs.
     *
     * @return datePairs as ArrayList<DatePair>
     */
    public ArrayList<DatePair> getDatePairs() {
        return datePairs;
    }

    /**
     * Public constructor for AddCommand that accepts description and the list
     * of DatePairs.
     *
     * @param description of the task
     * @param datePairs   list of datePairs if any
     */
    public AddCommand(String description, ArrayList<DatePair> datePairs) {
        this.description = description;
        this.datePairs = datePairs;
    }

    /**
     * Create and add the task to the database.
     *
     * @return the correct response back to the user
     * @throws IOException DBManager has encountered an IO Error
     */
    @Override
    public String execute() throws IOException {
        assert datePairs != null;
        assert description != null;
        assert !description.equals("");

        if (DatePair.isDateBeforeNow(datePairs)) {
            return ColorFormatter.format(
                String.format(MESSAGE_ADD_PAST, description), Color.RED);
        }

        Task task = new Task(description, datePairs);
        if (!task.checkValidity()) {
            return ColorFormatter.format(MESSAGE_ERROR_WRONG_TASK_TYPE,
                                         Color.RED);
        }

        boolean hasConflict = task.checkConflictWithDB(getDbManager());

        long id = getDbManager().modify(null, task,
                                        String.format(JOURNAL_MESSAGE_ADD,
                                                      task.getDescription()));
        assert id >= 0 : "ID should never be a negative number.";

        StringBuilder response = new StringBuilder();
        if (hasConflict) {
            response.append(ColorFormatter.format(
                String.format(MESSAGE_ADD_CONFLICT, description),
                Color.YELLOW));
        } else {
            response.append(ColorFormatter.format(
                String.format(MESSAGE_ADD, description), Color.YELLOW));
        }
        response.append(System.lineSeparator());
        response.append(ColorFormatter.format(task.formatOutput("+"),
                                              Color.GREEN));
        return response.toString();
    }
}
