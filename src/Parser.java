import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.joestelmach.natty.CalendarSource;
import com.joestelmach.natty.DateGroup;

/**
 * Parser that reads in raw user input and provides instruction on how the UI
 * should call for the correction execution at the logic.
 *
 * @author hooitong
 *
 */
public class Parser {
    /* Parser specific error messages to return */
    private static final String SEARCH_ERROR_EMPTY = "Please enter a keyword to search for.";
    private static final String ADD_ERROR_NO_DESC = "Please enter a task description to add.";
    private static final String VIEW_ERROR_EMPTY = "Please enter a valid date range to view.";
    private static final String DELETE_ERROR_INVALID = "Please enter a task id to delete.";
    private static final String UPDATE_ERROR_INVALID = "Please enter a task id to update.";
    private static final String MARK_ERROR_INVALID = "Please enter a task id to mark.";
    private static final String UPDATE_ERROR_EMPTY = "Please enter something to update.";
    private static final String INVALID_COMMAND = "Please enter a valid command.";

    /* Used specifically to parse date from user's input */
    private static com.joestelmach.natty.Parser dateParser = new com.joestelmach.natty.Parser();

    /**
     * Public method called by interface which accepts a input from the user and
     * return the command after parsing.
     *
     * @param input the raw input user provides
     * @return Command object with the correct argument and type
     */
    public static Command parse(String input) {
        CommandType userCommand = determineCommandType(input);
        String args = removeFirstWord(input);
        return parseCommand(userCommand, args);
    }

    /**
     * Retrieve the command (from the first word) and return the command type
     * the user specifies.
     *
     * @param input the raw input user provides
     * @return the command type of the input
     */
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
                return new Command(userCommand, INVALID_COMMAND);

            default: // all unrecognized command are invalid commands
                throw new AssertionError(userCommand);
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
        /* Create empty DatePair object */
        DatePair date = new DatePair();

        /* If user decides to view all uncompleted tasks */
        if (args.contains("all")) {
            return new Command(CommandType.VIEW, true, date);
        }

        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Pre-process certain terms for Natty parser */
        input = parseSpecialTerms(input);

        /* Use Natty library to parse date specified by user */
        List<DateGroup> groups = dateParser.parse(input);

        /* If no matched dates, return invalid command */
        if (groups.isEmpty()) {
            return new Command(CommandType.INVALID, VIEW_ERROR_EMPTY);
        }

        /* Extract up to two dates from user's input */
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();

            if (dates.size() == 2) {
                date.setStartDate(dateToCalendar(dates.get(0)));
                date.setEndDate(dateToCalendar(dates.get(1)));
            } else if (dates.size() == 1) {
                date.setEndDate(dateToCalendar(dates.get(0)));
            }
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

        /* Pre-process certain terms for Natty parser */
        input = parseSpecialTerms(input);

        /* Use Natty library to parse date specified by user */
        List<DateGroup> groups = dateParser.parse(input);
        DatePair date = new DatePair();

        /* Extract up to 2 dates from user's input */
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();

            if (dates.size() == 2) {
                date.setStartDate(dateToCalendar(dates.get(0)));
                date.setEndDate(dateToCalendar(dates.get(1)));
            } else if (dates.size() == 1) {
                date.setEndDate(dateToCalendar(dates.get(0)));
            }

            input = input.replace(group.getText(), "");
        }

        ArrayList<DatePair> datePairs = new ArrayList<DatePair>();
        /* TODO: No support for more than 2 dates at the moment */
        if (date.hasEndDate()) {
            datePairs.add(date);
        }

        String desc = input.trim();

        if (desc.isEmpty()) {
            return new Command(CommandType.INVALID, ADD_ERROR_NO_DESC);
        } else {
            return new Command(CommandType.ADD, desc, datePairs);
        }
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

            /* Pre-process certain terms for Natty parser */
            input = parseSpecialTerms(input);

            /* Use Natty library to parse date specified by user */
            List<DateGroup> groups = dateParser.parse(input);
            DatePair date = new DatePair();
            for (DateGroup group : groups) {
                List<Date> dates = group.getDates();

                if (dates.size() == 2) {
                    date.setStartDate(dateToCalendar(dates.get(0)));
                    date.setEndDate(dateToCalendar(dates.get(1)));
                } else if (dates.size() == 1) {
                    date.setEndDate(dateToCalendar(dates.get(0)));
                }

                input = input.replace(group.getText(), "");
            }

            String desc = input.trim();

            ArrayList<DatePair> datePairs = new ArrayList<DatePair>();
            /* TODO: No support for more than 2 dates at the moment */
            if (date.hasEndDate()) {
                datePairs.add(date);
            }

            if (!(date.hasEndDate() || !desc.isEmpty())) {
                return new Command(CommandType.INVALID, UPDATE_ERROR_EMPTY);
            }

            return new Command(CommandType.UPDATE, deleteId, desc, datePairs);
        } catch (NumberFormatException e) {
            return new Command(CommandType.INVALID, UPDATE_ERROR_INVALID);
        }

    }

    /**
     * Parse undo command from user. Arguments are ignored and not considered as
     * error.
     *
     * @param args the arguments the user input
     * @return UNDO command
     */
    public static Command parseUndo(String args) {
        return new Command(CommandType.UNDO);
    }

    /**
     * Parse redo command from user. Arguments are ignored and not considered as
     * error.
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

    private static String parseSpecialTerms(String input) {
        /* Check if any usage of until */
        String untilTerm = "\\b(until)\\b";
        Pattern textPattern = Pattern.compile(untilTerm);
        Matcher textMatcher = textPattern.matcher(input);

        while (textMatcher.find()) {
            input = input.replace(textMatcher.group().trim(), "today to");
        }

        /* Check if any usage of from */
        String fromTerm = "\\b(from)\\b";
        textPattern = Pattern.compile(fromTerm);
        textMatcher = textPattern.matcher(input);

        /* Remove all from term as not supported by Natty lib */
        while (textMatcher.find()) {
            input = input.replace(textMatcher.group().trim(), "");
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
