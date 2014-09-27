import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
     * v0.1 - Parse simple view command from user input:
     * Current acceptable format
     *  - view this week
     *  - view today
     *  - view tommorrow
     *  - view [date]
     *  - view [date] - [date]
     *
     * @param args
     * @return
     */
    public static Command parseView(String args) {
        /* Extract all date from user's input */
        String dateRegex = "(((0[1-9]|[12][0-9]|3[01])([/])(0[13578]|10|12)([/])(\\d{4}))|(([0][1-9]|[12][0-9]|30)([/])(0[469]|11)([/])(\\d{4}))|((0[1-9]|1[0-9]|2[0-8])([/])(02)([/])(\\d{4}))|((29)(\\.|-|\\/)(02)([/])([02468][048]00))|((29)([/])(02)([/])([13579][26]00))|((29)([/])(02)([/])([0-9][0-9][0][48]))|((29)([/])(02)([/])([0-9][0-9][2468][048]))|((29)([/])(02)([/])([0-9][0-9][13579][26])))(?:\\s?(?:((?:[01][0-9]|2[0-3])[:.]?(?:[0-5][0-9]))(?!\\s*[apAP][mM])|((?:[0-1]?[0-9]|[2][0-3])(?:[:.](?:[0-5][0-9]))?(?:\\s*)(?:[apAP][mM])\\s*))(?:\\b(?:to|-)\\b\\s*)?(?:((?:[01][0-9]|2[0-3])[:.]?(?:[0-5][0-9]))(?!\\s*[apAP][mM])|((?:[0-1]?[0-9]|[2][0-3])(?:[:.](?:[0-5][0-9]))?(?:\\s*)(?:[apAP][mM])\\s*))?)?";
        Pattern datePattern = Pattern.compile(dateRegex);
        Matcher dateMatcher = datePattern.matcher(args);
        DatePair date = null;

        if (dateMatcher.find()) {
            Calendar startDate = parseDate(dateMatcher.group());
            if (dateMatcher.find()) {
                Calendar endDate = parseDate(dateMatcher.group());
                date = new DatePair(startDate, endDate);
            } else {
                date = new DatePair(startDate);
            }
        }

        /* String Search if no provided date */
        if (date == null) {
            String keyword = args.trim();
            if (keyword.equalsIgnoreCase("today")) {
                Calendar startDate = Calendar.getInstance();
                date = new DatePair(startDate);
            } else if (keyword.equalsIgnoreCase("this week")) {
                Calendar startDate = Calendar.getInstance();
                Calendar endDate = Calendar.getInstance();
                endDate.add(Calendar.DAY_OF_WEEK,
                        (endDate.get(Calendar.DAY_OF_WEEK) - 1));
                date = new DatePair(startDate, endDate);
            } else if (keyword.equalsIgnoreCase("tommorrow")) {
                Calendar startDate = Calendar.getInstance();
                startDate.add(Calendar.DATE, 1);
                date = new DatePair(startDate);
            } else {
                /* Invalid Command */
            }
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
     * v0.1 - Parse simple add command from user input:
     * Current acceptable format
     * - add [desc]
     * - add [desc] [date] <time | 0000>
     * - add [desc] [date] <time | 0000> [date] <time | 0000>
     *
     * @param args user given arguments
     * @return add command
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
     * Currently quite strict date parsing. There must be space
     * between the minute and am/pm.
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
}
