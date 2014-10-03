import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.joestelmach.natty.DateGroup;

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
     * v0.2 - Parse view command from user with natural language support.
     * Current limitation is restricted to only one DatePair
     *
     * @param args the arguments the user input
     * @return either a VIEW command or INVALID command
     */
    public static Command parseView(String args) {
        /* If user decides to view all uncompleted tasks */
        if (args.contains("all")) {
            return new Command(CommandType.VIEW, true, null);
        }

        List<DateGroup> groups = dateParser.parse(args);
        DatePair date = new DatePair();
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();

            if (dates.size() == 2) {
                date.setEndDate(dateToCalendar(dates.get(1)));
                System.out.println(date.getEndDate().getTime());
            }

            date.setStartDate(dateToCalendar(dates.get(0)));
            System.out.println(date.getStartDate().getTime());
        }

        return new Command(CommandType.VIEW, false, date);
    }

    /**
     * Support parsing for search on basis of keywords.
     *
     * @param args user given arguments
     * @return either invalid or search command
     */
    public static Command parseSearch(String args) {
        if (args.trim().isEmpty()) {
            return new Command(CommandType.INVALID, SEARCH_ERROR_EMPTY);
        } else {
            return new Command(CommandType.SEARCH, args);
        }
    }

    /**
     * v0.2 - Parse add command from user with natural language support.
     *
     * @param args the arguments the user input
     * @return either a ADD command or INVALID command
     */
    public static Command parseAdd(String args) {
        /* Extract all date from user's input */
        String dateRegex = "(((0[1-9]|[12][0-9]|3[01])([/])(0[13578]|10|12)([/])(\\d{4}))|(([0][1-9]|[12][0-9]|30)([/])(0[469]|11)([/])(\\d{4}))|((0[1-9]|1[0-9]|2[0-8])([/])(02)([/])(\\d{4}))|((29)(\\.|-|\\/)(02)([/])([02468][048]00))|((29)([/])(02)([/])([13579][26]00))|((29)([/])(02)([/])([0-9][0-9][0][48]))|((29)([/])(02)([/])([0-9][0-9][2468][048]))|((29)([/])(02)([/])([0-9][0-9][13579][26])))(?:\\s?(?:((?:[01][0-9]|2[0-3])[:.]?(?:[0-5][0-9]))(?!\\s*[apAP][mM])|((?:[0-1]?[0-9]|[2][0-3])(?:[:.](?:[0-5][0-9]))?(?:\\s*)(?:[apAP][mM])\\s*))(?:\\b(?:to|-)\\b\\s*)?(?:((?:[01][0-9]|2[0-3])[:.]?(?:[0-5][0-9]))(?!\\s*[apAP][mM])|((?:[0-1]?[0-9]|[2][0-3])(?:[:.](?:[0-5][0-9]))?(?:\\s*)(?:[apAP][mM])\\s*))?)?";
        Pattern datePattern = Pattern.compile(dateRegex);
        Matcher dateMatcher = datePattern.matcher(args);
        DatePair date = null;

        if (dateMatcher.find()) {
            Calendar startDate = parseDate(dateMatcher.group());
            args = args.replace(dateMatcher.group(), "");
            if (dateMatcher.find()) {
                Calendar endDate = parseDate(dateMatcher.group());
                date = new DatePair(startDate, endDate);
                args = args.replace(dateMatcher.group(), "");
            } else {
                date = new DatePair(startDate);
            }
        }

        String desc = args.trim();
        return new Command(CommandType.ADD, desc, date);
    }

    /**
     * Currently quite strict date parsing. There must be space between the
     * minute and am/pm.
     *
     * @param date
     * @return Calendar object
     */
    private static Calendar parseDate(String date) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yy h:mm aa",
                Locale.ENGLISH);
        try {
            cal.setTime(sdf.parse(date));
        } catch (ParseException e) {
            System.out.println(date);
            // TODO: Throw a better error
            return null;
        }

        return cal;
    }

    public static Command parseDelete(String args) {
        try {
            int deleteId = Integer.parseInt(args.trim());
            return new Command(CommandType.DELETE, deleteId);
        } catch (NumberFormatException e) {
            return new Command(CommandType.INVALID, DELETE_ERROR_INVALID);
        }
    }

    public static Command parseUpdate(String args) {
        try {
            int taskId = Integer.parseInt(getFirstWord(args.trim()));

            args = removeFirstWord(args);

            /* Extract all date from user's input */
            String dateRegex = "(((0[1-9]|[12][0-9]|3[01])([/])(0[13578]|10|12)([/])(\\d{4}))|(([0][1-9]|[12][0-9]|30)([/])(0[469]|11)([/])(\\d{4}))|((0[1-9]|1[0-9]|2[0-8])([/])(02)([/])(\\d{4}))|((29)(\\.|-|\\/)(02)([/])([02468][048]00))|((29)([/])(02)([/])([13579][26]00))|((29)([/])(02)([/])([0-9][0-9][0][48]))|((29)([/])(02)([/])([0-9][0-9][2468][048]))|((29)([/])(02)([/])([0-9][0-9][13579][26])))(?:\\s?(?:((?:[01][0-9]|2[0-3])[:.]?(?:[0-5][0-9]))(?!\\s*[apAP][mM])|((?:[0-1]?[0-9]|[2][0-3])(?:[:.](?:[0-5][0-9]))?(?:\\s*)(?:[apAP][mM])\\s*))(?:\\b(?:to|-)\\b\\s*)?(?:((?:[01][0-9]|2[0-3])[:.]?(?:[0-5][0-9]))(?!\\s*[apAP][mM])|((?:[0-1]?[0-9]|[2][0-3])(?:[:.](?:[0-5][0-9]))?(?:\\s*)(?:[apAP][mM])\\s*))?)?";
            Pattern datePattern = Pattern.compile(dateRegex);
            Matcher dateMatcher = datePattern.matcher(args);
            DatePair date = null;

            if (dateMatcher.find()) {
                Calendar startDate = parseDate(dateMatcher.group());
                args = args.replace(dateMatcher.group(), "");
                if (dateMatcher.find()) {
                    Calendar endDate = parseDate(dateMatcher.group());
                    date = new DatePair(startDate, endDate);
                    args = args.replace(dateMatcher.group(), "");
                } else {
                    date = new DatePair(startDate);
                }
            }

            return new Command(CommandType.UPDATE, taskId, args.trim(), date);
        } catch (NumberFormatException e) {
            return new Command(CommandType.INVALID, DELETE_ERROR_INVALID);
        }
    }

    public static Command parseUndo(String args) {
        return new Command(CommandType.UNDO);
    }

    public static Command parseRedo(String args) {
        return new Command(CommandType.REDO);
    }

    public static Command parseExit(String args) {
        return new Command(CommandType.EXIT);
    }

    /* Helper Methods for Parser */
    private static String getFirstWord(String input) {
        return input.split("\\s+", 2)[0];
    }

    private static String removeFirstWord(String input) {
        String[] splitWord = input.split("\\s+", 2);
        return splitWord.length == 1 ? splitWord[0] : splitWord[1];
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
