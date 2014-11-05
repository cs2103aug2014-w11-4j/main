package com.rubberduck.menu;

import com.rubberduck.logic.DatePair;
import com.rubberduck.logic.Task;
import com.rubberduck.menu.ColorFormatter.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Utility class that contains reusable code to squish and format output that is
 * acceptable by the MenuInterface object.
 */
//@author A0111736M
public class Formatter {

    private static final int DESC_MAX_WIDTH = 200;
    private static final int DESC_TABLE_MAX_WIDTH = 41;
    private static final int SENTENCE_WIDTH = 80;
    private static final int WORD_LONGER_THAN_MAX = -1;

    protected static final String FORMAT_TABLE = "%-7s%-6s%-43s%-24s";
    protected static final String FORMAT_TENTATIVE = "%-7s%-6s%-43s%-19s%-5s";

    private static final String ANSI_PREFIX = "\u001b[";

    /**
     * Private constructor as Formatter is a utility class and cannot be
     * instantiated.
     */
    private Formatter() {
    }


    /**
     * Accepts a String representation of a task description and truncate to the
     * acceptable length to prevent buffer overflow.
     *
     * @param desc String of description
     * @return truncated String of description
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
     * Accepts a String representation of messages and format it such that it is
     * buffer-friendly.
     *
     * @param messages String of message
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
     * Format the task information provided into a buffer-friendly and organised
     * output.
     *
     * @param t            the Task object to format
     * @param displayingId the display ID it should display
     * @return Buffer acceptable String of Task
     */
    public static String formatTask(Task t, String displayingId) {
        boolean overdue = false;
        StringBuilder stringBuilder = new StringBuilder();
        String description = t.getDescription();
        ArrayList<DatePair> dates = t.getDateList();
        char isDone = t.getIsDone() ? 'Y' : 'N';
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM hh:mm aa",
                                                           Locale.US);

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
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.
                        append(
                            String.format(FORMAT_TENTATIVE, "", "",
                                          desc, date, "[" + dateId++ + "]"));
                } else {
                    stringBuilder.
                        append(String.format(FORMAT_TENTATIVE, displayingId,
                                             isDone, desc, date,
                                             "[" + dateId++ + "]"));
                }

                if (date.contains("to")) {
                    rangeFlag = false;
                }
            } else {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(String.format(FORMAT_TABLE, "", "",
                                                       desc, date));
                } else {
                    stringBuilder.append(String.format(FORMAT_TABLE,
                                                       displayingId, isDone,
                                                       desc, date));
                }

                rangeFlag = true;
            }
        }

        String output = stringBuilder.toString().trim();

        if (overdue) {
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
                ansiLength += ANSI_PREFIX.length();
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
