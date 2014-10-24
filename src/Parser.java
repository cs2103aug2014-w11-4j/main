import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
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
    /* Global logger to log information and exception. */
    private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /* Parser specific error messages to return */
    private static final String MESSAGE_SEARCH_ERROR_EMPTY = "Please enter a keyword to search for.";
    private static final String MESSAGE_ADD_ERROR_NO_DESC = "Please enter a task description to add.";
    private static final String MESSAGE_VIEW_ERROR_EMPTY = "Please enter a valid date range to view.";
    private static final String MESSAGE_DELETE_ERROR_INVALID = "Please enter a task id to delete.";
    private static final String MESSAGE_UPDATE_ERROR_INVALID = "Please enter a task id to update.";
    private static final String MESSAGE_CONFIRM_ERROR_INVALID = "Please enter a task and date id to confirm task.";
    private static final String MESSAGE_MARK_ERROR_INVALID = "Please enter a task id to mark.";
    private static final String MESSAGE_UPDATE_ERROR_EMPTY = "Please enter something to update.";
    private static final String MESSAGE_INVALID_COMMAND = "Please enter a valid command.";

    /* Static member that holds the single instance */
    private static Parser parserInstance;

    /* Used specifically to parse date from user's input */
    private com.joestelmach.natty.Parser dateParser;

    /**
     * Private Constructor for Singleton Implementation.
     */
    private Parser() {
        dateParser = new com.joestelmach.natty.Parser();
        Calendar today = Calendar.getInstance();

        /* Default time of all dates parsed are 2359 of that day */
        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        CalendarSource.setBaseDate(today.getTime());
    }

    /**
     * Retrieves the singleton instance of the Parser.
     *
     * @return instance of Parser
     */
    public static Parser getInstance() {
        if (parserInstance == null) {
            parserInstance = new Parser();
        }
        return parserInstance;
    }

    /**
     * Accepts a input from the user and return the command after parsing.
     *
     * @param input the raw input user provides
     * @return Command object with the correct argument and type
     */
    public Command parse(String input) {
        logger.info("Parsing input: " + input);
        Command.CommandType userCommand = determineCommandType(input);
        logger.info("CommandType requested: " + userCommand.toString());
        String args = removeFirstWord(input);
        return parseCommand(userCommand, args);
    }

    /**
     * Retrieves the command (from the first word) and return the command type
     * the user specifies.
     *
     * @param input the raw input user provides
     * @return the command type of the input
     */
    private Command.CommandType determineCommandType(String input) {
        String command = getFirstWord(input);
        return Command.CommandType.getCommandType(command);
    }

    /**
     * Parses command arguments from user input and return the correct command
     * object with its valid arguments.
     *
     * @param userCommand the type of command the user initiated
     * @param args arguments that the user input
     * @return the correct command object intended by user
     */
    private Command parseCommand(Command.CommandType userCommand, String args) {
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

            case CONFIRM:
                return parseConfirm(args);

            case HELP:
                return parseHelp(args);

            case CLEAR:
                return parseClear(args);

            case EXIT:
                return parseExit(args);

            case INVALID:
                return new InvalidCommand(userCommand, MESSAGE_INVALID_COMMAND);

            default: /* all unrecognized command are invalid commands */
                throw new AssertionError(userCommand);
        }
    }

    /**
     * Parses view command from user with natural language support. Current
     * limitation is restricted to only one DatePair.
     *
     * @param args the arguments the user input
     * @return either a VIEW command or INVALID command
     */
    public Command parseView(String args) {
        boolean isCompleted = args.toLowerCase().contains("completed");

        /* Create empty DatePair object */
        DatePair date = new DatePair();

        /* If user decides to view all uncompleted tasks */
        if (args.contains("all")) {
            return new ViewCommand(Command.CommandType.VIEW, true, isCompleted,
                    date);
        }

        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Pre-process certain terms for Natty parser */
        input = parseSpecialTerms(input);

        /* Use Natty library to parse date specified by user */
        List<DateGroup> groups = dateParser.parse(input);

        /* If no matched dates, return invalid command */
        if (groups.isEmpty()) {
            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_VIEW_ERROR_EMPTY);
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
        return new ViewCommand(Command.CommandType.VIEW, false, isCompleted, date);
    }

    /**
     * Parses search command from user on the basis of keywords.
     *
     * @param args user given arguments
     * @return either a SEARCH or INVALID command
     */
    public Command parseSearch(String args) {
        if (args.trim().isEmpty()) {
            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_SEARCH_ERROR_EMPTY);
        } else {
            return new SearchCommand(Command.CommandType.SEARCH, args);
        }
    }

    /**
     * Parses add command from user with natural language support.
     *
     * @param args the arguments the user input
     * @return either a ADD command or INVALID command
     */
    public Command parseAdd(String args) {
        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Pre-process certain terms for Natty parser */
        input = parseSpecialTerms(input);

        /* Support tentative task by splitting with 'or' */
        String[] tentatives = input.split("\\bor\\b");

        /* ArrayList to store all possible DatePair from input */
        ArrayList<DatePair> datePairs = new ArrayList<DatePair>();

        String desc = "";

        /* For each possible tentative date */
        for (String tentative : tentatives) {
            String tokens = tentative;
            /* Continue parsing tokens until retrieved valid date */
            while (true) {
                /* Use Natty library to parse date specified by user */
                List<DateGroup> groups = dateParser.parse(tokens);
                DatePair date = new DatePair();

                for (DateGroup group : groups) {
                    List<Date> dates = group.getDates();

                    if (group.getText().length() <= 2) {
                        /* remove parsed token from tokens */
                        tokens = tokens.replace(group.getText(), "");
                        continue;
                    }

                    if (dates.size() == 2) {
                        Calendar startDate = dateToCalendar(dates.get(0));
                        Calendar endDate = dateToCalendar(dates.get(1));

                        if (group.isTimeInferred()) {
                            startDate.set(Calendar.HOUR_OF_DAY, 0);
                            startDate.set(Calendar.MINUTE, 0);
                            startDate.set(Calendar.SECOND, 0);

                            endDate.set(Calendar.HOUR_OF_DAY, 23);
                            endDate.set(Calendar.MINUTE, 59);
                            endDate.set(Calendar.SECOND, 0);
                        }

                        date.setStartDate(startDate);
                        date.setEndDate(endDate);
                    } else if (dates.size() == 1) {
                        date.setEndDate(dateToCalendar(dates.get(0)));
                    }

                    desc += tentative.replace(group.getText(), "");
                }

                if (groups.isEmpty()) {
                    desc += tentative;
                }

                if (date.hasEndDate()) {
                    datePairs.add(date);
                }

                break;
            }
        }

        desc = desc.trim();

        if (desc.isEmpty()) {
            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_ADD_ERROR_NO_DESC);
        } else {
            return new AddCommand(Command.CommandType.ADD, desc, datePairs);
        }
    }

    /**
     * Parses delete command from user by getting the deleteId.
     *
     * @param args the arguments the user input
     * @return either a DELETE command or INVALID command
     */
    public Command parseDelete(String args) {
        try {
            int deleteId = Integer.parseInt(getFirstWord(args).trim());
            return new DeleteCommand(Command.CommandType.DELETE, deleteId);
        } catch (NumberFormatException e) {

            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_DELETE_ERROR_INVALID);
        }
    }

    /**
     * Parses update command from user with natural language support.
     *
     * @param args the arguments the user input
     * @return either a UPDATE command or INVALID command
     */
    public Command parseUpdate(String args) {
        try {
            /* Get Task ID to update */
            int deleteId = Integer.parseInt(getFirstWord(args));
            args = removeFirstWord(args);

            /* Parse all US Date to SG Date Formal Format */
            String input = parseUStoSGDate(args);

            /* Pre-process certain terms for Natty parser */
            input = parseSpecialTerms(input);

            /* Support tentative task by splitting with 'or' */
            String[] tentatives = input.split("\\bor\\b");

            /* ArrayList to store all possible DatePair from input */
            ArrayList<DatePair> datePairs = new ArrayList<DatePair>();

            String desc = "";

            /* For each possible tentative date */
            for (String tentative : tentatives) {
                String tokens = tentative;
                /* Continue parsing tokens until retrieved valid date */
                while (true) {
                    /* Use Natty library to parse date specified by user */
                    List<DateGroup> groups = dateParser.parse(tokens);
                    DatePair date = new DatePair();

                    for (DateGroup group : groups) {
                        List<Date> dates = group.getDates();

                        if (group.getText().length() <= 2) {
                            /* remove parsed token from tokens */
                            tokens = tokens.replace(group.getText(), "");
                            continue;
                        }

                        if (dates.size() == 2) {
                            Calendar startDate = dateToCalendar(dates.get(0));
                            Calendar endDate = dateToCalendar(dates.get(1));

                            if (group.isTimeInferred()) {
                                startDate.set(Calendar.HOUR_OF_DAY, 0);
                                startDate.set(Calendar.MINUTE, 0);
                                startDate.set(Calendar.SECOND, 0);

                                endDate.set(Calendar.HOUR_OF_DAY, 23);
                                endDate.set(Calendar.MINUTE, 59);
                                endDate.set(Calendar.SECOND, 0);
                            }

                            date.setStartDate(startDate);
                            date.setEndDate(endDate);
                        } else if (dates.size() == 1) {
                            date.setEndDate(dateToCalendar(dates.get(0)));
                        }

                        desc += tentative.replace(group.getText(), "");
                    }

                    if (groups.isEmpty()) {
                        desc += tentative;
                    }

                    if (date.hasEndDate()) {
                        datePairs.add(date);
                    }

                    break;
                }
            }

            desc = desc.trim();

            if (!(!datePairs.isEmpty() || !desc.isEmpty())) {
                return new InvalidCommand(Command.CommandType.INVALID,
                        MESSAGE_UPDATE_ERROR_EMPTY);
            }

            return new UpdateCommand(Command.CommandType.UPDATE, deleteId, desc,
                    datePairs);
        } catch (NumberFormatException e) {
            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_UPDATE_ERROR_INVALID);
        }

    }

    /**
     * Parses undo command from user. Arguments are ignored and not considered
     * as error.
     *
     * @param args the arguments the user input
     * @return UNDO command
     */
    public Command parseUndo(String args) {
        return new UndoCommand(Command.CommandType.UNDO);
    }

    /**
     * Parses redo command from user. Arguments are ignored and not considered
     * as error.
     *
     * @param args the arguments the user input
     * @return REDO command
     */
    public Command parseRedo(String args) {
        return new RedoCommand(Command.CommandType.REDO);
    }

    /**
     * Parses mark command from user by getting markId from input.
     *
     * @param args the arguments the user input
     * @return either a MARK or INVALID command
     */
    public Command parseMark(String args) {
        try {
            int markId = Integer.parseInt(getFirstWord(args).trim());
            return new MarkCommand(Command.CommandType.MARK, markId);
        } catch (NumberFormatException e) {
            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_MARK_ERROR_INVALID);
        }
    }

    /**
     * Parses confirm command from user by getting taskId and dateId from input.
     *
     * @param args the arguments the user input
     * @return either a CONFIRM command or INVALID command
     */
    private Command parseConfirm(String args) {
        try {
            String[] substrings = args.split("\\s+");
            if (substrings.length < 2) {
                return new InvalidCommand(Command.CommandType.INVALID,
                        MESSAGE_CONFIRM_ERROR_INVALID);
            }

            int confirmId = Integer.parseInt(substrings[0]);
            int dateId = Integer.parseInt(substrings[1]);
            return new ConfirmCommand(Command.CommandType.CONFIRM, confirmId, dateId);
        } catch (NumberFormatException e) {
            return new InvalidCommand(Command.CommandType.INVALID,
                    MESSAGE_CONFIRM_ERROR_INVALID);
        }
    }

    /**
     * Parses help command from user.
     *
     * @param args the arguments the user input
     * @return HELP command
     */
    public Command parseHelp(String args) {
        return new HelpCommand(Command.CommandType.HELP);
    }

    /**
     * Parses clear command from user.
     *
     * @param args the arguments the user input
     * @return CLEAR command
     */
    public Command parseClear(String args) {
        return new ClearCommand(Command.CommandType.CLEAR);
    }

    /**
     * Parses exit command from user.
     *
     * @param args the arguments the user input
     * @return EXIT command
     */
    public Command parseExit(String args) {
        return new ExitCommand(Command.CommandType.EXIT);
    }

    /* Helper Methods for Parser */

    /**
     * Parses any form of valid US date of mmddyyyy to the UK/SG standard of
     * ddmmyyyy for convention and locale purposes.
     *
     * @param input the input from the user
     * @return a modified string if there is a US date in the string
     */
    private String parseUStoSGDate(String input) {
        /* Extract mmddyyyy formal date format from user's input */
        String dateRegex = "(0[1-9]|[12][0-9]|3[01])[-\\s\\/.](0[1-9]|1[012])[-\\s\\/.]?((?:19|20)\\d\\d)?";
        Pattern datePattern = Pattern.compile(dateRegex);
        Matcher dateMatcher = datePattern.matcher(input);

        /* Swap to SG Format of ddmmyyyy */
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

    /**
     * Parses special occurrences of terms from the user input so that the
     * resulting output parsed into Natty lib will be more accurate and correct.
     *
     * @param input the input from the user
     * @return a modified string if there is any occurrence of identified terms.
     */
    private String parseSpecialTerms(String input) {
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

        /* Check if any usage of next week */
        String weekTerm = "\\b(next\\s+week)\\b";
        textPattern = Pattern.compile(weekTerm);
        textMatcher = textPattern.matcher(input);

        /* Expand next week to a DatePair with the range of next week */
        while (textMatcher.find()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            Calendar nextWeekDate = Calendar.getInstance(Locale.UK);
            nextWeekDate.add(Calendar.DATE, 7);
            int firstDayOfWeek = nextWeekDate.getFirstDayOfWeek();

            Calendar startDate = Calendar.getInstance(Locale.UK);
            startDate.setTime(nextWeekDate.getTime());
            int days = (startDate.get(Calendar.DAY_OF_WEEK) + 7 - firstDayOfWeek) % 7;
            startDate.add(Calendar.DATE, -days);

            Calendar endDate = Calendar.getInstance(Locale.UK);
            endDate.setTime(startDate.getTime());
            endDate.add(Calendar.DATE, 6);

            input = input.replace(textMatcher.group().trim(),
                    dateFormat.format(startDate.getTime()) + " 00:00 to "
                            + dateFormat.format(endDate.getTime()) + " 23:59");
        }

        /* Check if any usage of next month */
        String monthTerm = "\\b(next\\s+month)\\b";
        textPattern = Pattern.compile(monthTerm);
        textMatcher = textPattern.matcher(input);

        /* Expand next month to a DatePair with the range of next month */
        while (textMatcher.find()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

            Calendar startDate = Calendar.getInstance(Locale.UK);
            startDate.add(Calendar.MONTH, 1);
            startDate.set(Calendar.DAY_OF_MONTH,
                    startDate.getActualMinimum(Calendar.DAY_OF_MONTH));

            Calendar endDate = Calendar.getInstance(Locale.UK);
            endDate.add(Calendar.MONTH, 1);
            endDate.set(Calendar.DAY_OF_MONTH,
                    endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

            input = input.replace(textMatcher.group().trim(),
                    dateFormat.format(startDate.getTime()) + " 00:00 to "
                            + dateFormat.format(endDate.getTime()) + " 23:59");
        }

        /* Check if any usage of next year */
        String yearTerm = "\\b(next\\s+year)\\b";
        textPattern = Pattern.compile(yearTerm);
        textMatcher = textPattern.matcher(input);

        /* Expand next year to a DatePair with the range of next year */
        while (textMatcher.find()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            Calendar yearCalendar = Calendar.getInstance(Locale.UK);
            yearCalendar.add(Calendar.YEAR, 1);

            String year = dateFormat.format(yearCalendar.getTime());

            input = input.replace(textMatcher.group().trim(), "1 Jan " + year
                    + " 00:00 to " + "31 Dec " + year + " 23:59");
        }

        return input;
    }

    /**
     * Gets the first word from a given String object.
     * @param input String object
     * @return a String object containing the first word
     */
    private static String getFirstWord(String input) {
        return input.split("\\s+", 2)[0];
    }

    /**
     * Removes the first word from a given String object.
     *
     * @param input String object
     * @return a String object without the first word
     */
    private static String removeFirstWord(String input) {
        String[] splitWord = input.split("\\s+", 2);
        return splitWord.length == 1 ? "" : splitWord[1];
    }

    /**
     * Converts a Date object passed in and returns a Calendar object.
     *
     * @param date the date object to convert
     * @return the calendar object after conversion
     */
    private static Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
