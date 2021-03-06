package rubberduck.common.formatter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import rubberduck.common.datatransfer.DatePair;
import rubberduck.common.datatransfer.Task;
import rubberduck.common.formatter.ColorFormatter.Color;
import rubberduck.storage.DatabaseManager;

//@author A0111736M
/**
 * Utility class that contains reusable code to squish and format output that is
 * acceptable by the MenuInterface object.
 */
public class Formatter {

    public static final String FORMAT_TABLE = "%-7s%-6s%-43s%-24s";
    public static final String FORMAT_TENTATIVE = "%-7s%-6s%-43s%-19s%-5s";

    /**
     * Enum to represent the type of header should be allocated for the task.
     */
    private enum HeaderType {
        NONE, FLOATING, DEADLINE, SCHEDULE,
    }

    /* Static constants to represent the header type to print */
    private static final String SEPARATOR_FLOATING =
        "--------------------------------[  FLOATING  ]----------------------------------";
    private static final String SEPARATOR_DEADLINE =
        "--------------------------------[  DEADLINE  ]----------------------------------";
    private static final String SEPARATOR_SCHEDULE =
        "--------------------------------[  SCHEDULE  ]----------------------------------";

    private static final String ANSI_PREFIX = "\u001b[";
    private static final int ANSI_OFFSET = 7;

    private static final int DESC_MAX_WIDTH = 200;
    private static final int DESC_TABLE_MAX_WIDTH = 41;
    private static final int SENTENCE_WIDTH = 80;
    private static final int WORD_LONGER_THAN_MAX = -1;

    private static final String DATE_12HOUR_FORMAT = "dd MMM hh:mm aa";
    private static final String DATE_24HOUR_FORMAT = "dd MMM HH:mm";
    private static final Locale DEFAULT_LOCALE = Locale.US;

    private static String currentTimeFormat = DATE_24HOUR_FORMAT;

    /**
     * Private constructor as Formatter is a utility class and cannot be
     * instantiated.
     */
    private Formatter() {
    }

    /**
     * Toggles the time format within formatter between 12 hours and 24 hours.
     * Default for Formatter will always start at 24 hours.
     */
    public static void toggleTimeFormat() {
        currentTimeFormat = is12HourFormat() ? DATE_24HOUR_FORMAT
                                             : DATE_12HOUR_FORMAT;
    }

    /**
     * Return boolean if formatter is set to 12 hour date format.
     *
     * @return true if formatter is set to 12 hour date format else false.
     */
    public static boolean is12HourFormat() {
        return currentTimeFormat.equals(DATE_12HOUR_FORMAT);
    }

    /**
     * Accepts a String representation of a task description and truncate to the
     * acceptable length to prevent buffer overflow.
     *
     * @param desc description String
     * @return truncated description as String
     */
    public static String limitDescription(String desc) {
        if (desc.length() <= DESC_MAX_WIDTH) {
            return desc;
        }

        int i = desc.lastIndexOf(" ", DESC_MAX_WIDTH);
        if (i == WORD_LONGER_THAN_MAX) {
            i = DESC_MAX_WIDTH;
        }
        return desc.substring(0, i) + "...";
    }

    /**
     * Accepts a String object and attempts to parse and return using the
     * current date format in Formatter.
     *
     * @param date the date as String
     * @return date in either 12/24 hour format based on Formatter
     */
    public static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(currentTimeFormat,
                                                           DEFAULT_LOCALE);
        return dateFormat.format(date);
    }

    /**
     * Accepts a String representation of messages and format it such that it is
     * buffer-friendly.
     *
     * @param messages messages String
     * @return Buffer acceptable String array of messages
     */
    public static String[] formatMessage(String messages) {
        String[] lines = messages.split(System.lineSeparator());
        LinkedList<String> formattedList = new LinkedList<String>();
        for (String line : lines) {
            formattedList.addAll(splitWords(line, SENTENCE_WIDTH));
        }
        String[] formattedArray = new String[formattedList.size()];
        return formattedList.toArray(formattedArray);
    }

    /**
     * Format the list of tasks into a String output and return.
     *
     * @param dataTable ArrayList containing all the instanceId in data table
     * @param db        DatabaseManager instance
     * @return the formatted string of all tasks involved
     * @throws IOException occurs when DatabaseManager encounters an I/O error
     */
    public static String formatTaskList(ArrayList<Long> dataTable,
                                        DatabaseManager<Task> db)
        throws IOException {
        Collections.sort(dataTable, db.getInstanceIdComparator());

        StringBuilder taskData = new StringBuilder();
        HeaderType prevType = HeaderType.NONE;
        for (int i = 0; i < dataTable.size(); i++) {
            if (taskData.length() > 0) {
                taskData.append(System.lineSeparator());
            }

            Task task = db.getInstance(dataTable.get(i));

            HeaderType currentType = getHeaderType(task);
            if (currentType != prevType) {
                switch (currentType) {
                    case FLOATING:
                        taskData.append(SEPARATOR_FLOATING);
                        break;

                    case DEADLINE:
                        taskData.append(SEPARATOR_DEADLINE);
                        break;

                    case SCHEDULE:
                        taskData.append(SEPARATOR_SCHEDULE);
                        break;

                    default:
                        break;
                }
                taskData.append(System.lineSeparator());
            }
            prevType = currentType;
            taskData.append(formatTask(task, i + 1 + ""));
        }
        return taskData.toString();
    }

    /**
     * Gets the corresponding HeaderType based on the given Task type.
     *
     * @param t Task object
     * @return HeaderType
     */
    private static HeaderType getHeaderType(Task t) {
        if (t.isFloatingTask()) {
            return HeaderType.FLOATING;
        } else if (t.isDeadline()) {
            return HeaderType.DEADLINE;
        } else {
            return HeaderType.SCHEDULE;
        }
    }

    /**
     * Format the task information provided into a buffer-friendly and organised
     * output.
     *
     * @param t  the Task object to format
     * @param id the display ID it should display
     * @return Buffer acceptable String of Task
     */
    private static String formatTask(Task t, String id) {
        /* Setup variables and information about the task provided */
        StringBuilder taskBuilder = new StringBuilder();
        boolean overdue = false;
        String description = t.getDescription();
        ArrayList<DatePair> dates = t.getDateList();
        char isDone = t.getIsDone() ? 'Y' : 'N';
        SimpleDateFormat dateFormat = new SimpleDateFormat(currentTimeFormat,
                                                           DEFAULT_LOCALE);
        if (t.isTentative()) {
            description += " (tentative)";
        }

        /* Split the description into separate lines based on max width*/
        LinkedList<String> wordWrapList = splitWords(description,
                                                     DESC_TABLE_MAX_WIDTH);


        /* Break all dates in the task into their respective format */
        LinkedList<String> dateList = new LinkedList<String>();
        for (DatePair dp : dates) {
            if (dp.hasDateRange()) {
                dateList.add(dateFormat.format(dp.getStartDate().getTime())
                             + " to");
                dateList.add(dateFormat.format(dp.getEndDate().getTime()));
            } else if (dp.hasEndDate()) {
                dateList.add(dateFormat.format(dp.getEndDate().getTime()));
            }
            /* If end date has passed current time, set flag to overdue */
            if (dp.getEndDate().before(Calendar.getInstance())) {
                overdue = true;
            }
        }

        /* Format all fragments in desc and date into multiple lines */
        int dateId = 1;
        boolean rangeFlag = true;
        while (!wordWrapList.isEmpty() || !dateList.isEmpty()) {
            String desc = wordWrapList.isEmpty() ? ""
                                                 : wordWrapList.removeFirst();

            String date = dateList.isEmpty() ? "" : dateList.removeFirst();

            if (t.isTentative() && rangeFlag) {
                if (taskBuilder.length() != 0) {
                    taskBuilder.append(System.lineSeparator());
                    taskBuilder.
                        append(
                            String.format(FORMAT_TENTATIVE, "", "",
                                          desc, date, "[" + dateId++ + "]"));
                } else {
                    taskBuilder.
                        append(String.format(FORMAT_TENTATIVE, id, isDone, desc,
                                             date, "[" + dateId++ + "]"));
                }

                if (date.contains("to")) {
                    rangeFlag = false;
                }
            } else {
                if (taskBuilder.length() != 0) {
                    taskBuilder.append(System.lineSeparator());
                    taskBuilder.append(String.format(FORMAT_TABLE, "", "", desc,
                                                     date));
                } else {
                    taskBuilder.append(String.format(FORMAT_TABLE,
                                                     id, isDone, desc, date));
                }

                rangeFlag = true;
            }
        }

        String output = taskBuilder.toString().trim();

        if (overdue && !t.getIsDone()) {
            output = ColorFormatter.format(output, Color.RED);
        }

        return output;
    }

    /**
     * Split a String into multiple lines where each line can only have a max
     * length that is provided.
     *
     * @param words     the String to split
     * @param maxLength maximum length per line
     * @return List of Strings that were split
     */
    private static LinkedList<String> splitWords(String words, int maxLength) {
    /* Break sentences into multiple lines and add into list */
        LinkedList<String> wordWrapList = new LinkedList<String>();
        while (!words.isEmpty()) {
            int ansiLength = 0;
            if (words.startsWith(ANSI_PREFIX)) {
                ansiLength = ANSI_OFFSET;
            }
            if (words.length() <= maxLength + ansiLength) {
                wordWrapList.add(words);
                words = "";
            } else {
                int i = words.lastIndexOf(" ", maxLength + ansiLength);
                if (i == WORD_LONGER_THAN_MAX) {
                    i = maxLength + ansiLength;
                }
                wordWrapList.add(words.substring(0, i));
                words = words.substring(i + 1);
            }
        }
        return wordWrapList;
    }
}
