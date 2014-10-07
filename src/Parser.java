import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.CalendarSource;

/**
 * Parser that reads in raw user input and provides instruction on how the UI
 * should call for the correction execution at the logic.
 *
 * @author hooitong
 *
 */
public class Parser {
    private static final String SEARCH_ERROR_EMPTY = "Please enter a keyword to search for.";
    private static final String DELETE_ERROR_INVALID = "Please enter a valid id to delete.";
    private static final String MARK_ERROR_INVALID = "Please enter a valid id to mark";

    /* Used specifically to parse date from user's input */
    private static com.joestelmach.natty.Parser dateParser = new com.joestelmach.natty.Parser();

    public static Command parse(String input) {
        CommandType userCommand = determineCommandType(input);
        String args = removeFirstWord(input);
        return parseCommand(userCommand, args);
    }

    private static CommandType determineCommandType(String input) {
        String command = getFirstWord(input);
        return CommandType.getCommandType(command);
    }

    /**
     * Parse command arguments from user input and return the correct command
     * object with its valid arguments.
     *
     * @param userCommand the type of command the user initiated
     * @param args arguments that the user input
     * @return the correct command object intended by user
     */
    private static Command parseCommand(CommandType userCommand, String args) {
        switch (userCommand) {
            case VIEW:
                return parseView(args);

            case SEARCH:
                return parseSearch(args);

            case ADD:
                return parseAdd(args);

            case DELETE:
                return parseDelete(args);

            case UPDATE:
                return parseUpdate(args);

            case UNDO:
                return parseUndo(args);

            case REDO:
                return parseRedo(args);

            case MARK:
                return parseMark(args);

            case EXIT:
                return parseExit(args);

            case INVALID:
                return new Command(userCommand);

            default:
                throw new UnsupportedOperationException(
                        "Command not integrated yet");
        }

    }

    /**
     * Parse view command from user with natural language support. Current
     * limitation is restricted to only one DatePair.
     *
     * @param args the arguments the user input
     * @return either a VIEW command or INVALID command
     */
    public static Command parseView(String args) {
        /* If user decides to view all uncompleted tasks */
        if (args.contains("all")) {
            return new Command(CommandType.VIEW, true, null);
        }

        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Use Natty library to parse date specified by user */
        List<DateGroup> groups = dateParser.parse(input);
        DatePair date = new DatePair();
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();

            if (dates.size() == 2) {
                date.setEndDate(dateToCalendar(dates.get(1)));
            }

            date.setStartDate(dateToCalendar(dates.get(0)));
        }

        /* Return view command with retrieved arguments */
        return new Command(CommandType.VIEW, false, date);
    }

    /**
     * Support parsing for search on basis of keywords.
     *
     * @param args user given arguments
     * @return either a SEARCH or INVALID command
     */
    public static Command parseSearch(String args) {
        if (args.trim().isEmpty()) {
            return new Command(CommandType.INVALID, SEARCH_ERROR_EMPTY);
        } else {
            return new Command(CommandType.SEARCH, args);
        }
    }

    /**
     * Parse add command from user with natural language support. Current
     * limitation is restricted to only one DatePair.
     *
     * @param args the arguments the user input
     * @return either a ADD command or INVALID command
     */
    public static Command parseAdd(String args) {
        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Use Natty library to parse date specified by user */
        List<DateGroup> groups = dateParser.parse(input);
        DatePair date = new DatePair();
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();

            if (dates.size() == 2) {
                date.setEndDate(dateToCalendar(dates.get(1)));
            }

            date.setStartDate(dateToCalendar(dates.get(0)));

            input = input.replace(group.getText(), "");
        }

        String desc = input.trim();
        return new Command(CommandType.ADD, desc, date);
    }

    /**
     * Parse delete command from user by getting the deleteId.
     *
     * @param args the arguments the user input
     * @return either a DELETE command or INVALID command
     */
    public static Command parseDelete(String args) {
        try {
            int deleteId = Integer.parseInt(args.trim());
            return new Command(CommandType.DELETE, deleteId);
        } catch (NumberFormatException e) {
            return new Command(CommandType.INVALID, DELETE_ERROR_INVALID);
        }
    }

    /**
     * Parse update command from user with natural language support.
     *
     * @param args the arguments the user input
     * @return either a UPDATE command or INVALID command
     */
    public static Command parseUpdate(String args) {
        try {
            /* Get Task ID to update */
            int deleteId = Integer.parseInt(getFirstWord(args));
            args = removeFirstWord(args);

            /* Parse all US Date to SG Date Formal Format */
            String input = parseUStoSGDate(args);

            /* Use Natty library to parse date specified by user */
            List<DateGroup> groups = dateParser.parse(input);
            DatePair date = new DatePair();
            for (DateGroup group : groups) {
                List<Date> dates = group.getDates();

                if (dates.size() == 2) {
                    date.setEndDate(dateToCalendar(dates.get(1)));
                }

                date.setStartDate(dateToCalendar(dates.get(0)));

                input = input.replace(group.getText(), "");
            }

            String desc = input.trim();
            return new Command(CommandType.UPDATE, deleteId, desc, date);
        } catch (NumberFormatException e) {
            return new Command(CommandType.INVALID, DELETE_ERROR_INVALID);
        }

    }

    /**
     * Parse undo command from user.
     *
     * @param args the arguments the user input
     * @return UNDO command
     */
    public static Command parseUndo(String args) {
        return new Command(CommandType.UNDO);
    }

    /**
     * Parse redo command from user.
     *
     * @param args the arguments the user input
     * @return REDO command
     */
    public static Command parseRedo(String args) {
        return new Command(CommandType.REDO);
    }

    /**
     * Parse mark command from user by getting markId.
     *
     * @param args the arguments the user input
     * @return either a MARK or INVALID command
     */
    public static Command parseMark(String args) {
        try {
            int markId = Integer.parseInt(args.trim());
            return new Command(CommandType.MARK, markId);
        } catch (NumberFormatException e) {
            return new Command(CommandType.INVALID, MARK_ERROR_INVALID);
        }
    }

    /**
     * Parse exit command from user.
     *
     * @param args the arguments the user input
     * @return EXIT command
     */
    public static Command parseExit(String args) {
        return new Command(CommandType.EXIT);
    }

    /* Helper Methods for Parser */
    private static String parseUStoSGDate(String input) {
        /* Extract MMDDYYYY formal date format from user's input */
        String dateRegex = "(0[1-9]|[12][0-9]|3[01])[-\\s\\/.](0[1-9]|1[012])[-\\s\\/.]?((?:19|20)\\d\\d)?";
        Pattern datePattern = Pattern.compile(dateRegex);
        Matcher dateMatcher = datePattern.matcher(input);

        /* Swap to SG Format of DDMMYYYY */
        while (dateMatcher.find()) {
            if (dateMatcher.group(3) != null) {
                input = input.replace(dateMatcher.group().trim(),
                        dateMatcher.group(2) + "/" + dateMatcher.group(1) + "/"
                                + dateMatcher.group(3));
            } else {
                input = input.replace(dateMatcher.group().trim(),
                        dateMatcher.group(2) + "/" + dateMatcher.group(1));
            }
        }

        return input;
    }

    private static String getFirstWord(String input) {
        return input.split("\\s+", 2)[0];
    }

    private static String removeFirstWord(String input) {
        String[] splitWord = input.split("\\s+", 2);
        return splitWord.length == 1 ? "" : splitWord[1];
    }

    private static Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static void initParser() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        CalendarSource.setBaseDate(today.getTime());
    }
}
