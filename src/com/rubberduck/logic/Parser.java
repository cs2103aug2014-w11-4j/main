package com.rubberduck.logic;

import com.joestelmach.natty.CalendarSource;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.ParseLocation;
import com.rubberduck.command.AddCommand;
import com.rubberduck.command.ClearCommand;
import com.rubberduck.command.Command;
import com.rubberduck.command.ConfirmCommand;
import com.rubberduck.command.DeleteCommand;
import com.rubberduck.command.ExitCommand;
import com.rubberduck.command.HelpCommand;
import com.rubberduck.command.InvalidCommand;
import com.rubberduck.command.MarkCommand;
import com.rubberduck.command.RedoCommand;
import com.rubberduck.command.SearchCommand;
import com.rubberduck.command.SyncCommand;
import com.rubberduck.command.UndoCommand;
import com.rubberduck.command.UpdateCommand;
import com.rubberduck.command.ViewCommand;
import com.rubberduck.command.ViewCommand.ViewFilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser that reads in raw user input and attempts to translate into the
 * correct command the user wants to execute.
 */
//@author A0111736M
public class Parser {

    /* Global logger to log information and exception. */
    private static final Logger LOGGER =
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /* Parser specific error messages to return */
    private static final String MESSAGE_SEARCH_ERROR_EMPTY =
        "Please enter a keyword to search for.";
    private static final String MESSAGE_ADD_ERROR_NO_DESC =
        "Please enter a task description to add.";
    private static final String MESSAGE_VIEW_ERROR_EMPTY =
        "Please enter a valid date range to view.";
    private static final String MESSAGE_DELETE_ERROR_INVALID =
        "Please enter a task id to delete.";
    private static final String MESSAGE_UPDATE_ERROR_INVALID =
        "Please enter a task id to update.";
    private static final String MESSAGE_CONFIRM_ERROR_INVALID =
        "Please enter a task and date id to confirm task.";
    private static final String MESSAGE_MARK_ERROR_INVALID =
        "Please enter a task id to mark.";
    private static final String MESSAGE_UPDATE_ERROR_EMPTY =
        "Please enter something to update.";
    private static final String MESSAGE_SYNC_INVALID =
        "Please enter a valid sync type.";
    private static final String MESSAGE_INVALID_COMMAND =
        "Please enter a valid command. Press <Tab> or Enter ? for help.";

    /* List of words to remove that appear before a date parsed */
    private static final String[] DATE_WORDS =
        new String[]{"by", "on", "at", "from", "during", "@", "in"};

    private static final int DEFAULT_START_HOUR = 0;
    private static final int DEFAULT_START_MINUTE = 0;
    private static final int DEFAULT_START_SECOND = 0;
    private static final int DEFAULT_START_MILLISECOND = 0;
    private static final int DEFAULT_END_HOUR = 23;
    private static final int DEFAULT_END_MINUTE = 59;
    private static final int DEFAULT_END_SECOND = 0;
    private static final int DEFAULT_END_MILLISECOND = 0;
    private static final int DAYS_IN_WEEK = 7;
    private static final int DESC_NO_OR = 1;
    private static final int NO_DATE_PARSED = -1;

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

        /* Set the base calendar time to the default time declared. */
        today.set(Calendar.HOUR_OF_DAY, DEFAULT_END_HOUR);
        today.set(Calendar.MINUTE, DEFAULT_END_MINUTE);
        today.set(Calendar.SECOND, DEFAULT_END_SECOND);
        today.set(Calendar.MILLISECOND, DEFAULT_END_MILLISECOND);
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
        LOGGER.info("Parsing input: " + input);
        Command.CommandType userCommand = determineCommandType(input);
        LOGGER.info("CommandType requested: " + userCommand.toString());
        String args = removeFirstWord(input).trim();
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
     * @param args        arguments that the user input
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

            case SYNC:
                return parseSync(args);

            case HELP:
                return parseHelp(args);

            case CLEAR:
                return parseClear(args);

            case EXIT:
                return parseExit(args);

            case INVALID:
                return new InvalidCommand(MESSAGE_INVALID_COMMAND, true);

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
    private Command parseView(String args) {
        /* Change to lower case for easier parsing */
        args = args.toLowerCase();

        boolean isCompleted = args.contains("complete");

        /* Setup view filter specified by user */
        ArrayList<ViewCommand.ViewFilter> viewList =
            new ArrayList<ViewFilter>();
        if (args.contains("task")) {
            viewList.add(ViewFilter.TASK);
        }
        if (args.contains("deadline")) {
            viewList.add(ViewCommand.ViewFilter.DEADLINE);
        }
        if (args.contains("schedule")) {
            viewList.add(ViewFilter.SCHEDULE);
        }

        /* Create empty DatePair object */
        DatePair date = new DatePair();

        /* If user decides to view overdue tasks */
        if (args.contains("overdue")) {
            return new ViewCommand(ViewCommand.ViewType.OVERDUE, false, date,
                                   viewList);
        }

        /* If user decides to view all tasks */
        if (args.contains("all")) {
            return new ViewCommand(ViewCommand.ViewType.ALL, isCompleted, date,
                                   viewList);
        }

        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Pre-process and expand certain terms for Natty parser */
        input = parseSpecialTerms(input);

        /* Use Natty library to parse date specified by user */
        List<DateGroup> groups = dateParser.parse(input);

        /* If no matched dates, execute previous view/search command */
        if (groups.isEmpty()) {
            return new ViewCommand(ViewCommand.ViewType.PREV, isCompleted, date,
                                   viewList);
        }

        /* Extract up to two dates from user's input */
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();

            /* If date range is parsed */
            if (dates.size() >= 2) {
                Calendar startDate = dateToCalendar(dates.get(0));
                Calendar endDate = dateToCalendar(dates.get(1));

                /* Swap date if necessary */
                if (startDate.after(endDate)) {
                    Calendar temp = endDate;
                    endDate = startDate;
                    startDate = temp;
                }

                /* If no time specified, set default timings */
                if (group.isTimeInferred()) {
                    startDate.set(Calendar.HOUR_OF_DAY,
                                  DEFAULT_START_HOUR);
                    startDate.set(Calendar.MINUTE,
                                  DEFAULT_START_MINUTE);
                    startDate.set(Calendar.SECOND,
                                  DEFAULT_START_SECOND);
                    startDate.set(Calendar.MILLISECOND,
                                  DEFAULT_START_MILLISECOND);

                    endDate.set(Calendar.HOUR_OF_DAY,
                                DEFAULT_END_HOUR);
                    endDate.set(Calendar.MINUTE,
                                DEFAULT_END_MINUTE);
                    endDate.set(Calendar.SECOND,
                                DEFAULT_END_SECOND);
                    endDate.set(Calendar.MILLISECOND,
                                DEFAULT_END_MILLISECOND);
                }

                date.setStartDate(startDate);
                date.setEndDate(endDate);
            } else if (dates.size() == 1) {
                date.setStartDate(dateToCalendar(dates.get(0)));
                date.getStartDate().set(Calendar.HOUR_OF_DAY,
                                        DEFAULT_START_HOUR);
                date.getStartDate().set(Calendar.MINUTE,
                                        DEFAULT_START_MINUTE);
                date.getStartDate().set(Calendar.SECOND,
                                        DEFAULT_START_SECOND);
                date.getStartDate().set(Calendar.MILLISECOND,
                                        DEFAULT_START_MILLISECOND);
                date.setEndDate(dateToCalendar(dates.get(0)));
            }
        }

        /* Return view command with retrieved arguments */
        return new ViewCommand(ViewCommand.ViewType.DATE, isCompleted, date,
                               viewList);
    }

    /**
     * Parses search command from user on the basis of keywords.
     *
     * @param args user given arguments
     * @return either a SEARCH or INVALID command
     */
    private Command parseSearch(String args) {
        if (args.trim().isEmpty()) {
            return new InvalidCommand(MESSAGE_SEARCH_ERROR_EMPTY, true);
        } else {
            return new SearchCommand(args);
        }
    }

    /**
     * Parses add command from user with natural language support.
     *
     * @param args the arguments the user input
     * @return either a ADD command or INVALID command
     */
    private Command parseAdd(String args) {
        /* Parse all US Date to SG Date Formal Format */
        String input = parseUStoSGDate(args);

        /* Pre-process certain terms for Natty parser */
        input = parseSpecialTerms(input);

        /* ArrayList to store all possible DatePair from input */
        ArrayList<DatePair> datePairs = new ArrayList<DatePair>();

        String descString = extractDateFromDesc(input, datePairs);

        if (descString.isEmpty()) {
            return new InvalidCommand(MESSAGE_ADD_ERROR_NO_DESC, true);
        } else {
            return new AddCommand(descString, datePairs);
        }

    }

    /**
     * Parses delete command from user by getting the deleteId.
     *
     * @param args the arguments the user input
     * @return either a DELETE command or INVALID command
     */
    private Command parseDelete(String args) {
        try {
            int deleteId = Integer.parseInt(getFirstWord(args).trim());
            return new DeleteCommand(deleteId);
        } catch (NumberFormatException e) {
            return new InvalidCommand(MESSAGE_DELETE_ERROR_INVALID, true);
        }
    }

    /**
     * Parses update command from user with natural language support.
     *
     * @param args the arguments the user input
     * @return either a UPDATE command or INVALID command
     */
    private Command parseUpdate(String args) {
        try {
            /* Get Task ID to update */
            int updateId = Integer.parseInt(getFirstWord(args));
            args = removeFirstWord(args);

            /* Parse all US Date to SG Date Formal Format */
            String input = parseUStoSGDate(args);

            /* Pre-process certain terms for Natty parser */
            input = parseSpecialTerms(input);

            /* ArrayList to store all possible DatePair from input */
            ArrayList<DatePair> datePairs = new ArrayList<DatePair>();

            String descString = extractDateFromDesc(input, datePairs);

            if (!(!datePairs.isEmpty() || !descString.isEmpty())) {
                return new InvalidCommand(MESSAGE_UPDATE_ERROR_EMPTY, true);
            }

            return new UpdateCommand(updateId, descString, datePairs);
        } catch (NumberFormatException e) {
            return new InvalidCommand(MESSAGE_UPDATE_ERROR_INVALID, true);
        }

    }

    /**
     * Parses undo command from user. Arguments are ignored and not considered
     * as error.
     *
     * @param args the arguments the user input
     * @return UNDO command
     */
    private Command parseUndo(String args) {
        return new UndoCommand();
    }

    /**
     * Parses redo command from user. Arguments are ignored and not considered
     * as error.
     *
     * @param args the arguments the user input
     * @return REDO command
     */
    private Command parseRedo(String args) {
        return new RedoCommand();
    }

    /**
     * Parses mark command from user by getting markId from input.
     *
     * @param args the arguments the user input
     * @return either a MARK or INVALID command
     */
    private Command parseMark(String args) {
        try {
            int markId = Integer.parseInt(getFirstWord(args).trim());
            return new MarkCommand(markId);
        } catch (NumberFormatException e) {
            return new InvalidCommand(MESSAGE_MARK_ERROR_INVALID, true);
        }
    }

    /**
     * Parses confirm command from user by getting taskId and dateId from
     * input.
     *
     * @param args the arguments the user input
     * @return either a CONFIRM command or INVALID command
     */
    private Command parseConfirm(String args) {
        try {
            String[] substrings = args.split("\\s+");
            if (substrings.length < 2) {
                return new InvalidCommand(MESSAGE_CONFIRM_ERROR_INVALID, true);
            }

            int confirmId = Integer.parseInt(substrings[0].trim());
            int dateId = Integer.parseInt(substrings[1].trim());
            return new ConfirmCommand(confirmId, dateId);
        } catch (NumberFormatException e) {
            return new InvalidCommand(MESSAGE_CONFIRM_ERROR_INVALID, true);
        }
    }

    /**
     * Parses sync command from user.
     *
     * @param args the arguments the user input
     * @return SYNC command
     */
    private Command parseSync(String args) {
        args = args.trim();

        if (args.contains("push") && args.contains("pull")) {
            return new InvalidCommand(MESSAGE_SYNC_INVALID);
        } else if (args.contains("push")) {
            if (args.contains("force")) {
                return new SyncCommand(SyncCommand.SyncType.FORCE_PUSH);
            } else {
                return new SyncCommand(SyncCommand.SyncType.PUSH);
            }
        } else if (args.contains("pull")) {
            if (args.contains("force")) {
                return new SyncCommand(SyncCommand.SyncType.FORCE_PULL);
            } else {
                return new SyncCommand(SyncCommand.SyncType.PULL);
            }
        } else if (args.isEmpty()) {
            return new SyncCommand(SyncCommand.SyncType.TWO_WAY);
        } else {
            return new InvalidCommand(MESSAGE_SYNC_INVALID);
        }
    }

    /**
     * Parses help command from user.
     *
     * @param args the arguments the user input
     * @return HELP command
     */
    private Command parseHelp(String args) {
        if (args.isEmpty()) {
            return new HelpCommand(false, null);
        } else {
            return new HelpCommand(true, getFirstWord(args));
        }
    }

    /**
     * Parses clear command from user.
     *
     * @param args the arguments the user input
     * @return CLEAR command
     */
    private Command parseClear(String args) {
        return new ClearCommand();
    }

    /**
     * Parses exit command from user.
     *
     * @param args the arguments the user input
     * @return EXIT command
     */
    private Command parseExit(String args) {
        return new ExitCommand();
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
        String dateRegex =
            "(0[1-9]|[12][0-9]|3[01])[-\\s\\/.](0[1-9]|1[012])[-\\s\\/.]?((?:19|20)\\d\\d)?";
        Pattern datePattern = Pattern.compile(dateRegex);
        Matcher dateMatcher = datePattern.matcher(input);
        final int yearGroupIndex = 3;
        final int dayGroupIndex = 2;
        final int monthGroupIndex = 1;

        /* Swap to SG Format of ddmmyyyy */
        while (dateMatcher.find()) {
            if (dateMatcher.group(yearGroupIndex) != null) {
                input = input.replace(
                    dateMatcher.group().trim(),
                    dateMatcher.group(dayGroupIndex) + "/" +
                    dateMatcher.group(monthGroupIndex) + "/" +
                    dateMatcher.group(yearGroupIndex));
            } else {
                input = input.replace(
                    dateMatcher.group().trim(),
                    dateMatcher.group(dayGroupIndex) + "/" +
                    dateMatcher.group(monthGroupIndex));
            }
        }

        return input;
    }

    /**
     * Parses and expan special occurrences of terms from the user input so that
     * the resulting output parsed into Natty lib will be more accurate and
     * correct.
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

        /* Check if any usage of next week */
        String weekTerm = "\\b(next\\s+week)\\b";
        String weekFormat = "%s to %s";
        textPattern = Pattern.compile(weekTerm);
        textMatcher = textPattern.matcher(input);

        /* Expand next week to a DatePair with the range of next week */
        while (textMatcher.find()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy",
                                                               Locale.US);
            Calendar nextWeekDate = Calendar.getInstance(Locale.UK);
            nextWeekDate.add(Calendar.DATE, DAYS_IN_WEEK);
            int firstDayOfWeek = nextWeekDate.getFirstDayOfWeek();

            Calendar startDate = Calendar.getInstance(Locale.UK);
            startDate.setTime(nextWeekDate.getTime());
            int days = (startDate.get(Calendar.DAY_OF_WEEK) + DAYS_IN_WEEK -
                        firstDayOfWeek) % DAYS_IN_WEEK;
            startDate.add(Calendar.DATE, -days);

            Calendar endDate = Calendar.getInstance(Locale.UK);
            endDate.setTime(startDate.getTime());
            endDate.add(Calendar.DATE, DAYS_IN_WEEK - 1);

            input = input.
                replace(textMatcher.group().trim(),
                        String.format(weekFormat,
                                      dateFormat.format(startDate.getTime()),
                                      dateFormat.format(endDate.getTime())));
        }

        /* Check if any usage of next month */
        String monthTerm = "\\b(next\\s+month)\\b";
        String monthFormat = "%s to %s";
        textPattern = Pattern.compile(monthTerm);
        textMatcher = textPattern.matcher(input);

        /* Expand next month to a DatePair with the range of next month */
        while (textMatcher.find()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy",
                                                               Locale.US);

            Calendar startDate = Calendar.getInstance(Locale.UK);
            startDate.add(Calendar.MONTH, 1);
            startDate.set(Calendar.DAY_OF_MONTH,
                          startDate.getActualMinimum(Calendar.DAY_OF_MONTH));

            Calendar endDate = Calendar.getInstance(Locale.UK);
            endDate.add(Calendar.MONTH, 1);
            endDate.set(Calendar.DAY_OF_MONTH,
                        endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

            input = input.
                replace(textMatcher.group().trim(),
                        String.format(monthFormat,
                                      dateFormat.format(startDate.getTime()),
                                      dateFormat.format(endDate.getTime())));
        }

        /* Check if any usage of next year */
        String yearTerm = "\\b(next\\s+year)\\b";
        String yearFormat = "1 Jan %s to 31 Dec %s";
        textPattern = Pattern.compile(yearTerm);
        textMatcher = textPattern.matcher(input);

        /* Expand next year to a DatePair with the range of next year */
        while (textMatcher.find()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy",
                                                               Locale.US);
            Calendar yearCalendar = Calendar.getInstance(Locale.UK);
            yearCalendar.add(Calendar.YEAR, 1);

            String year = dateFormat.format(yearCalendar.getTime());

            input = input.replace(textMatcher.group().trim(),
                                  String.format(yearFormat, year, year));
        }

        return input;
    }

    /**
     * Remove any valid word that is before a parsed date. For example, "by",
     * "on", etc.
     *
     * @param input     the input to parse
     * @param dateIndex the index of the parsed date
     * @return Parsed string that removed any word from the date phrase list
     */
    private static String removeWordBeforeDate(String input, int dateIndex) {
        String textBeforeDate = input.substring(0, dateIndex).trim();
        String textAfter = input.substring(dateIndex, input.length());
        String lastWord = textBeforeDate.
            substring(textBeforeDate.lastIndexOf(" ") + 1);
        for (String preposition : DATE_WORDS) {
            if (lastWord.equalsIgnoreCase(preposition)) {
                textBeforeDate = textBeforeDate.
                    substring(0, textBeforeDate.length() - lastWord.length());
                break;
            }
        }

        return textBeforeDate + " " + textAfter;
    }

    /**
     * Separate the date and the actual description from the input.
     *
     * @param input the String to parse
     * @param dp    ArrayList to store any parsed date
     * @return the actual description as String
     */
    private String extractDateFromDesc(String input, ArrayList<DatePair> dp) {
        /* Support tentative task by splitting with 'or' */
        String[] tentatives = input.split("\\bor\\b");
        StringBuilder sb = new StringBuilder();
        int firstDateIndex = NO_DATE_PARSED;

        /* For each possible tentative date */
        for (int i = 0; i < tentatives.length; i++) {
            String tentative = tentatives[i];
            String tokens = tentative;
            boolean replaceOr = tentatives.length != DESC_NO_OR && i != 0;
            /* Continue parsing tokens until retrieved valid date */
            while (true) {
                /* Use Natty library to parse date specified by user */
                List<DateGroup> groups = dateParser.parse(tokens);
                boolean skipIteration = false;

                for (DateGroup group : groups) {
                    List<Date> dates = group.getDates();

                    /* Restrict parsing of natty */
                    Map<String, List<ParseLocation>> map =
                        group.getParseLocations();

                    boolean haveDate = map.get("date") != null;
                    boolean haveAMPM = map.get("meridian_indicator") != null;
                    boolean haveMinutes = map.get("minutes") != null;
                    boolean haveHour = map.get("hours") != null;

                    if (!haveDate) {
                        tokens = tokens.replaceFirst(group.getText(), "");
                        skipIteration = true;
                        break;
                    } else if (!(haveAMPM || haveMinutes) && haveHour) {
                        List<ParseLocation> hoursList = map.get("hours");
                        if (!hoursList.isEmpty()) {
                            String ignoredText = hoursList.get(0).getText();
                            tokens = tokens.replaceFirst(ignoredText, "");
                        }
                        skipIteration = true;
                        break;
                    }

                    if (dates.size() == 2) {
                        Calendar startDate = dateToCalendar(dates.get(0));
                        Calendar endDate = dateToCalendar(dates.get(1));

                        /* Swap date if necessary */
                        if (startDate.after(endDate)) {
                            Calendar temp = endDate;
                            endDate = startDate;
                            startDate = temp;
                        }

                        /* If no time specified, set default timings */
                        if (group.isTimeInferred()) {
                            startDate.set(Calendar.HOUR_OF_DAY,
                                          DEFAULT_START_HOUR);
                            startDate.set(Calendar.MINUTE,
                                          DEFAULT_START_MINUTE);
                            startDate.set(Calendar.SECOND,
                                          DEFAULT_START_SECOND);
                            startDate.set(Calendar.MILLISECOND,
                                          DEFAULT_START_MILLISECOND);

                            endDate.set(Calendar.HOUR_OF_DAY,
                                        DEFAULT_END_HOUR);
                            endDate.set(Calendar.MINUTE,
                                        DEFAULT_END_MINUTE);
                            endDate.set(Calendar.SECOND,
                                        DEFAULT_END_SECOND);
                            endDate.set(Calendar.MILLISECOND,
                                        DEFAULT_END_MILLISECOND);
                        }

                        dp.add(new DatePair(startDate, endDate));
                    } else if (dates.size() == 1) {
                        dp.add(new DatePair(dateToCalendar(dates.get(0))));
                    }

                    if (i == 0) {
                        firstDateIndex = group.getPosition();
                    }

                    tentative = tentative.replace(group.getText(), "");
                    if (!tentative.trim().isEmpty() && replaceOr) {
                        sb.append("or");
                    }
                    sb.append(tentative);
                }

                if (skipIteration) {
                    continue;
                }

                /* If token does not have any date parsed, just append to sb */
                if (groups.isEmpty()) {
                    if (replaceOr) {
                        sb.append("or");
                    }
                    sb.append(tentative);
                }
                break;
            }
        }

        if (firstDateIndex != NO_DATE_PARSED) {
            return removeWordBeforeDate(sb.toString(), firstDateIndex).trim();
        } else {
            return sb.toString().trim();
        }
    }

    /**
     * Gets the first word from a given String object.
     *
     * @param input String object
     * @return a String object containing the first word
     */
    private static String getFirstWord(String input) {
        return input.trim().split("\\s+", 2)[0];
    }

    /**
     * Removes the first word from a given String object.
     *
     * @param input String object
     * @return a String object without the first word
     */
    private static String removeFirstWord(String input) {
        String[] splitWord = input.trim().split("\\s+", 2);
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
