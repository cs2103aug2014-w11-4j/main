package com.rubberduck.command;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;
import com.rubberduck.menu.Formatter;
import com.rubberduck.menu.Response;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Concrete Command Class that can be executed to add a new task (floating,
 * deadline, schedule and tentative) into the database.
 */
//@author A0111794E
public class AddCommand extends Command {

    private static final String MESSAGE_ADD_TASK_SUCCESS =
        "\"%s\" has been successfully added.";
    private static final String MESSAGE_ADD_DEADLINE_SUCCESS =
        "\"%s\" has been successfully added on %s.";
    private static final String MESSAGE_ADD_TIMED_SUCCESS =
        "\"%s\" has been successfully added from %s.";
    private static final String MESSAGE_ADD_TENTATIVE_SUCCESS =
        "\"%s\" has been successfully added tentatively on your specified dates.";
    private static final String MESSAGE_SCHEDULE_CONFLICT =
        "Please note that there are conflicting schedule(s). Plan well!";
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
    public Response execute() throws IOException {
        assert datePairs != null;
        assert description != null;
        assert !description.equals("");

        if (DatePair.isDateBeforeNow(datePairs)) {
            String errorMessage = ColorFormatter.format(
                String.format(MESSAGE_ADD_PAST, description), Color.RED);
            return new Response(errorMessage, false);
        }

        Task task = new Task(description, datePairs);

        if (!task.checkValidity()) {
            String errorMessage = ColorFormatter.format(
                MESSAGE_ERROR_WRONG_TASK_TYPE, Color.RED);
            return new Response(errorMessage, false);
        }

        
        String recordDesc = Formatter.limitDescription(task.getDescription());

        long id = getDbManager().modify(null, task,
                                        String.format(JOURNAL_MESSAGE_ADD,
                                                      recordDesc));
        assert id >= 0 : "ID should never be a negative number.";

        /* Build Response to the User */
        StringBuilder messages = new StringBuilder();
        messages = getColorFeedback(messages, task, recordDesc);
     
        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
    
    /**
     * 
     * @param messages StringBuilder that modifies the messages to be shown to user
     * @param task the task that is being check for the status
     * @param recordDesc description of the task
     * @return StringBuilder containing the modified message to be shown to user
     * @throws IOException
     */
    
    private static StringBuilder getColorFeedback(StringBuilder messages, Task task,String recordDesc) throws IOException{
        if (task.isFloatingTask()) {
            messages.append(ColorFormatter.format(
                String.format(MESSAGE_ADD_TASK_SUCCESS, recordDesc),
                Color.YELLOW));
        } else if (task.isDeadline()) {
            messages.append(ColorFormatter.format(String.format(
                MESSAGE_ADD_DEADLINE_SUCCESS, recordDesc,
                task.getDateString()), Color.YELLOW));
        } else if (task.isTimedTask()) {
            messages.append(ColorFormatter.format(String.format(
                MESSAGE_ADD_TIMED_SUCCESS, recordDesc,
                task.getDateString()), Color.YELLOW));
        } else if (task.isTentative()) {
            messages.append(ColorFormatter.format(String.format(
                MESSAGE_ADD_TENTATIVE_SUCCESS, recordDesc), Color.YELLOW));
        }        
        messages = conflictColorFeedbackModifier(messages, task);       
        return messages;
    }
    
    
    private static StringBuilder conflictColorFeedbackModifier(StringBuilder messages, Task task) throws IOException{
        boolean hasConflict = task.checkConflictWithDB(getDbManager());

        if (hasConflict) {
            messages.append(System.lineSeparator());
            messages.append(ColorFormatter.format(MESSAGE_SCHEDULE_CONFLICT,
                                                  Color.RED));
        }
        return messages;
    }
    

}
