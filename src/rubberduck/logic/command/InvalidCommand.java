package rubberduck.logic.command;

import rubberduck.common.datatransfer.Response;
import rubberduck.common.formatter.ColorFormatter;
import rubberduck.common.formatter.ColorFormatter.Color;

/**
 * Concrete Command Class that can be executed to return an invalid response
 * back to the user.
 */
//@author A0111736M
public class InvalidCommand extends Command {

    private String errorMessage;
    private boolean showPrev;

    /**
     * Public constructor of InvalidCommand that accepts an error message. By
     * default, the system will not show the previous view table.
     *
     * @param errorMessage that is to be displayed to the user
     */
    public InvalidCommand(String errorMessage) {
        this(errorMessage, false);
    }

    /**
     * Public constructor of InvalidCommand that accepts an error message and a
     * boolean to indicate whether to show previous view table.
     *
     * @param errorMessage that is to be displayed to the user
     * @param showPrev     true if system to display prev view
     */
    public InvalidCommand(String errorMessage, boolean showPrev) {
        this.errorMessage = errorMessage;
        this.showPrev = showPrev;
    }

    /**
     * Returns error response provided by application back to user.
     *
     * @return Response object containing the error message
     */
    @Override
    public Response execute() {
        String errResponse = (ColorFormatter.format(errorMessage, Color.RED));
        LOGGER.info(errorMessage);
        return new Response(errResponse, !showPrev);
    }
}
