package rubberduck.logic.command;

import java.io.IOException;
import java.util.ArrayList;

import rubberduck.common.datatransfer.DatePair;
import rubberduck.common.datatransfer.Response;
import rubberduck.common.datatransfer.Task;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.common.formatter.Formatter;

//@author A0111794E
/**
 * Concrete Command Class that can be executed to add a new task (floating,
 * deadline, schedule and tentative) into the database.
 */
public class AddCommand extends Command {

    private static final String MESSAGE_ADD_FLOAT_SUCCESS =
        "\"%s\" has been successfully added.";
    private static final String MESSAGE_ADD_DEADLINE_SUCCESS =
        "\"%s\" has been successfully added on %s.";
    private static final String MESSAGE_ADD_TIMED_SUCCESS =
        "\"%s\" has been successfully added from %s.";
    private static final String MESSAGE_ADD_TENTATIVE_SUCCESS =
        "\"%s\" has been successfully added tentatively on your specified dates.";
    private static final String MESSAGE_ADD_PAST =
        "\"%s\" cannot be added as the end date has already passed the current time.";
    private static final String MESSAGE_ERROR_WRONG_TASK_TYPE =
        "Tentative task must be strictly for schedule(s) only. No deadline(s) are allowed.";
    private static final String MESSAGE_SCHEDULE_CONFLICT =
        "Please note that there are conflicting schedule(s). Plan well!";
    private static final String MESSAGE_ADD_START =
        "Adding tasks into database...";
    private static final String JOURNAL_MESSAGE_ADD =
        "Added task \"%s\"";

    private String description;
    private ArrayList<DatePair> datePairs;

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
     * Getter method for description.
     *
     * @return description as String
     */
    protected String getDescription() {
        return description;
    }

    /**
     * Getter method for datePairs.
     *
     * @return datePairs as ArrayList<DatePair>
     */
    protected ArrayList<DatePair> getDatePairs() {
        return datePairs;
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

        LOGGER.info(MESSAGE_ADD_START);
        long id = getDbManager().modify(null, task,
                                        String.format(JOURNAL_MESSAGE_ADD,
                                                      recordDesc));
        assert id >= 0 : "ID should never be a negative number.";

        boolean hasConflict = task.checkConflictWithDB(getDbManager(), id);

        /* Build Response to the User */
        StringBuilder messages = new StringBuilder();
        if (task.isTentative()) {
            messages.append(ColorFormatter.format(String.format(
                MESSAGE_ADD_TENTATIVE_SUCCESS, recordDesc), Color.YELLOW));
        } else if (task.isFloatingTask()) {
            messages.append(ColorFormatter.format(
                String.format(MESSAGE_ADD_FLOAT_SUCCESS, recordDesc),
                Color.YELLOW));
        } else if (task.isDeadline()) {
            messages.append(ColorFormatter.format(String.format(
                MESSAGE_ADD_DEADLINE_SUCCESS, recordDesc,
                task.getDateString()), Color.YELLOW));
        } else if (task.isSchedule()) {
            messages.append(ColorFormatter.format(String.format(
                MESSAGE_ADD_TIMED_SUCCESS, recordDesc,
                task.getDateString()), Color.YELLOW));
        }

        LOGGER.info(messages.toString());

        if (hasConflict) {
            messages.append(System.lineSeparator());
            messages.append(ColorFormatter.format(MESSAGE_SCHEDULE_CONFLICT,
                                                  Color.RED));
        }

        Response res = getPreviousDisplayCommand().execute();
        res.setMessages(messages.toString());
        return res;
    }
}
