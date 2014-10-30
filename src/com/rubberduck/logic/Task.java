package com.rubberduck.logic;

/**
 * This task class is used to represent a single task object
 * which stores all relevant information about the task and
 * provide the needed getters and setters for retrieval and
 * storage.
 *
 * @author Sia Wei Kiat Jason
 */

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import com.rubberduck.io.DatabaseManager;
import com.rubberduck.menu.ColorFormatter;
import com.rubberduck.menu.ColorFormatter.Color;

public class Task implements Serializable, Comparable<Task> {

    private String description;
    private ArrayList<DatePair> dateList;
    private boolean isDone;
    private String uuid;
    private Calendar lastUpdate;

    /**
     * Creates a task with no fields. This should only be used by Java Bean.
     */
    public Task() {

    }

    /**
     * Creates a task with notes field only
     *
     * @param description notes about the task
     */

    public Task(String description) {
        this(description, new ArrayList<DatePair>());
    }

    /**
     * Creates a task with notes and DatePair fields
     *
     * @param description about the task
     * @param dateList of possible DatePair
     */
    public Task(String description, ArrayList<DatePair> dateList) {
        this.description = description;
        this.dateList = dateList;
        this.isDone = false;
        updateLastUpdate();
        resetUuid();
    }

    /**
     * Change the description of the notes
     *
     * @param description of the task
     */
    public void setDescription(String description) {
        this.description = description;
        updateLastUpdate();
    }

    /**
     * Returns the description of the class
     *
     * @return the description of the class
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set a new dateList
     *
     * @param dateList of possible DatePair
     */

    public void setDateList(ArrayList<DatePair> dateList) {
        this.dateList = dateList;
        updateLastUpdate();
    }

    /**
     * Check if there is at least a start date or end date
     *
     * @return if there exist at least a date
     */
    public boolean hasDate() {
        for (DatePair dp : dateList) {
            if (dp.hasEndDate() || dp.hasStartDate()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an ArrayList of DatePair
     *
     * @return the dateList of possible DatePair
     */

    public ArrayList<DatePair> getDateList() {
        return dateList;
    }

    /**
     * Add an end date to the task without a start date
     *
     * @param endDate the dateline of the task
     */

    public void addEndDate(GregorianCalendar endDate) {
        DatePair dp = new DatePair(endDate);
        dateList.add(dp);
        updateLastUpdate();
    }

    /**
     * Shortcut to adding another DatePair into DatePairList
     *
     * @param datePair
     */
    public void addDatePair(DatePair datePair) {
        dateList.add(datePair);
        updateLastUpdate();
    }

    /**
     * Update the task to set it to complete
     *
     * @param isDone whether the task is completed
     */

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
        updateLastUpdate();
    }

    /**
     * Check if the status is completed
     *
     * @return if the task is completed
     */

    public boolean getIsDone() {
        return isDone;
    }

    /**
     * Return UUID of the task.
     *
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Set UUID of the task.
     *
     * @param uuid UUID to be set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
        updateLastUpdate();
    }

    /**
     * Get the task last update
     * @return Calendar format of last update time of the task
     */
    public Calendar getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * Update the task last updateTime (For Google Sync)
     */
    public void updateLastUpdate() {
        this.lastUpdate = Calendar.getInstance();
    }

    /**
     * A String representation of the Task object.
     *
     * @return Tasks information: description, status, list of DatePair
     * @deprecated Currently outdated and should not be used
     */
    @Override
    public String toString() {
        String status = "Done";

        String datePair = "";
        for (DatePair dp : dateList) {
            datePair += ("\n" + dp.toString());
        }

        if (!isDone) {
            status = "Not Done";
        }
        return description + " " + status + " " + datePair;
    }

    /**
     * Check if the dateList is empty
     * @return if the dateList is empty
     */
    public boolean isDateListEmpty() {
        return dateList.isEmpty();
    }

    /**
     * Test if there is overlap with a given DatePair.
     *
     * @param dateRange the DatePair to be compared with
     * @return true if there is overlap with the given DatePair
     * @author Huang Yue
     */
    public boolean isWithinPeriod(DatePair dateRange) {
        if (dateList.isEmpty()) {
            return true;
        }
        for (DatePair datePair : dateList) {
            if (datePair.isWithinPeriod(dateRange)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test whether current Task object has conflict with given Task object
     *
     * @param t another Task object to compare
     * @return true if conflict else false
     */
    public boolean hasConflictWith(Task t) {
        ArrayList<DatePair> dp = t.getDateList();
        for (int j = 0; j < dp.size(); j++) {
            if (!isDeadline()) {
                if (!t.isDeadline() && isWithinPeriod(dp.get(j))) {
                    return true;
                }
            } else {
                if (getEarliestDate().compareTo(dp.get(j).getEndDate()) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Reset the UUID to an empty string.
     */
    public void resetUuid() {
        this.uuid = "";
        updateLastUpdate();
    }

    /**
     * Format individual task into output format for display.
     *
     * @param displayingId the id of the task
     * @return the formatted string of the task
     * @author Hooi Tong
     */
    public String formatOutput(String displayingId) {
        boolean overdue = false;
        final int MAX_DESC_LENGTH = 41;
        StringBuilder stringBuilder = new StringBuilder();
        String description = getDescription();
        ArrayList<DatePair> dates = getDateList();
        char isDone = getIsDone() ? 'Y' : 'N';
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM hh:mm aa");
        boolean isTentative = dates.size() > 1;

        if (isTentative) {
            description += " (tentative)";
        }

        /* Used to store fragments of description and dates */
        LinkedList<String> wordWrapList = new LinkedList<String>();
        LinkedList<String> dateList = new LinkedList<String>();

        /* Break sentences into multiple lines and add into list */
        while (!description.isEmpty()) {
            if (description.length() <= MAX_DESC_LENGTH) {
                wordWrapList.add(description);
                description = "";
            } else {
                int i = description.lastIndexOf(" ", MAX_DESC_LENGTH);
                /* if there's a word with more than 41 characters long */
                if (i == -1) {
                    i = MAX_DESC_LENGTH;
                }
                wordWrapList.add(description.substring(0, i));
                description = description.substring(i + 1);
            }
        }

        /* Support for multiple dates for tentative tasks */
        for (int i = 0; i < dates.size(); i++) {
            DatePair dp = dates.get(i);
            if (dp.hasDateRange()) {
                dateList.add(dateFormat.format(dp.getStartDate().getTime())
                        + " to");
                dateList.add(dateFormat.format(dp.getEndDate().getTime()));
            } else if (dp.hasEndDate()) {
                dateList.add(dateFormat.format(dp.getEndDate().getTime()));
            }
            if (dp.getEndDate().before(Calendar.getInstance())) {
                overdue = true;
            }
        }

        /* Format all fragments in desc and date into multiple lines */

        int dateId = 1;
        boolean rangeTicker = true;
        while (!wordWrapList.isEmpty() || !dateList.isEmpty()) {
            String desc = wordWrapList.isEmpty() ? ""
                    : wordWrapList.removeFirst();

            String date = dateList.isEmpty() ? "" : dateList.removeFirst();
            if (isTentative && rangeTicker) {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(String.format(
                            "%-7s%-6s%-43s%-19s%-5s", "", "", desc, date, "["
                                    + dateId++ + "]"));
                } else {
                    stringBuilder.append(String.format(
                            "%-7s%-6s%-43s%-19s%-5s", displayingId, isDone,
                            desc, date, "[" + dateId++ + "]"));
                }

                if (date.contains("to")) {
                    rangeTicker = false;
                }
            } else {
                if (stringBuilder.length() != 0) {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(String.format("%-7s%-6s%-43s%-24s",
                            "", "", desc, date));
                } else {
                    stringBuilder.append(String.format("%-7s%-6s%-43s%-24s",
                            displayingId, isDone, desc, date));
                }

                rangeTicker = true;
            }
        }

        String output = stringBuilder.toString().trim();
        if (overdue) {
            output = ColorFormatter.format(output, Color.RED);
        }
        return output;
    }

    /**
     * Check if the task is a floating task
     * @return if the task is a floating task
     */

    public boolean isFloatingTask() {
        return dateList.isEmpty();
    }

    /**
     * Check if the task is a deadline only task
     * @return if the task is a deadline only task
     */

    public boolean isDeadline() {
        return (dateList.size() == 1 && dateList.get(0).isDeadline());
    }

    /**
     * Check if the task is a timed task
     * @return if the task is a timed task
     */
    public boolean isTimedTask() {
        if (dateList.size() > 0) {
            for (DatePair dp : dateList) {
                if (!dp.hasDateRange()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Check if the task is a valid task
     * @return if the task is a valid task
     */
    public boolean checkValidity() {
        return isFloatingTask() || isDeadline() || isTimedTask();
    }

    /**
     * Get the earliest Date of the task
     * @return the earliest Date of the task
     */
    public Calendar getEarliestDate() {
        if (isFloatingTask()) {
            throw new UnsupportedOperationException(
                    "No date in a floating task.");
        }

        Calendar earliestDate = null;

        for (DatePair dp : dateList) {
            if (dp.hasStartDate()
                    && (earliestDate == null || dp.getStartDate().before(
                            earliestDate))) {
                earliestDate = dp.getStartDate();
            }
            if (dp.hasEndDate()
                    && (earliestDate == null || dp.getEndDate().before(
                            earliestDate))) {
                earliestDate = dp.getEndDate();
            }
        }

        return earliestDate;
    }

    /**
     * Compare both task by their deadline
     */
    @Override
    public int compareTo(Task o) {
        assert (o != null);
 
        if(this.isTimedTask() && !o.isTimedTask()){
            return -1;
        } else if (!this.isTimedTask() && o.isTimedTask()){
            return 1;
        }
        
        if(this.isFloatingTask() && o.isFloatingTask()){
            return 0;
        } else if(this.isFloatingTask()){
            return 1;
        } else if(o.isFloatingTask()){
            return -1;
        }
                        
        return this.getEarliestDate().compareTo(o.getEarliestDate());
        
    }

    /**
     * Method used to check whether a task has any potential conflict in current
     * database.
     *
     * @param t the Task object
     * @return true if there is a conflict else false
     * @throws IOException
     */
    public boolean checkConflictWithDB(DatabaseManager<Task> dbManager)
            throws IOException {
        boolean isConflict = false;
        if (isFloatingTask()) {
            return isConflict;
        }
        ArrayList<Long> validIDList = dbManager.getValidIdList();
        for (int i = 0; i < validIDList.size(); i++) {
            Task storedTask = dbManager.getInstance(validIDList.get(i));
            if (!storedTask.getIsDone() && !storedTask.isFloatingTask()) {
                isConflict = hasConflictWith(storedTask);
            }
        }

        return isConflict;
    }
}
