/**
 * This task class is used to represent a single task object
 * which stores all relevant information about the task and
 * provide the needed getters and setters for retrieval and
 * storage.
 *
 * @author Sia Wei Kiat Jason
 */

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.UUID;

public class Task implements Serializable, Comparable<Task> {
    private String description;
    private ArrayList<DatePair> dateList;
    private boolean isDone;
    private String uuid;

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
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * Change the description of the notes
     *
     * @param description of the task
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the class
     * @return the description of the class
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set a new dateList
     * @param dateList of possible DatePair
     */

    public void setDateList(ArrayList<DatePair> dateList) {
        this.dateList = dateList;
    }

    /**
     * Check if there is at least a start date or end date
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
     * @return the dateList of possible DatePair
     */

    public ArrayList<DatePair> getDateList() {
        return dateList;
    }

    /**
     * Add a start date to the task without end date
     * @param startDate the starting date of the task
     */

    public void addStartDate(GregorianCalendar startDate) {
        DatePair dp = new DatePair(startDate, null);
        dateList.add(dp);
    }

    /**
     * Add an end date to the task without a start date
     * @param endDate the dateline of the task
     */

    public void addEndDate(GregorianCalendar endDate) {
        DatePair dp = new DatePair(null, endDate);
        dateList.add(dp);
    }

    /**
     * Shortcut to adding another DatePair into DatePairList
     * @param datePair
     */
    public void addDatePair(DatePair datePair) {
        dateList.add(datePair);
    }

    /**
     * Update the task to set it to complete
     * @param isDone whether the task is completed
     */

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
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
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Set UUID of the task, should only be used by Java Bean.
     * @param uuid UUID to be set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @return Tasks information: description, status, list of DatePair
     *
     */

    public String toString() {
        String status = "Done";

        String datePair = "";
        for (DatePair dp : dateList) {
            datePair += ("\n" + dp.toString());
        }

        if (!isDone) {
            status = "Not Done"; // TODO
        }
        return description + " " + status + " " + datePair; // TODO
    }

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
     * Format individual task into output format for display.
     *
     * @param displayingId the id of the task
     * @return the formatted string of the task
     * @author hooitong
     */
    public String formatOutput(long displayingId) {
        final int MAX_DESC_LENGTH = 41;
        StringBuilder stringBuilder = new StringBuilder();
        String description = getDescription();
        ArrayList<DatePair> dates = getDateList();
        char isDone = getIsDone() ? 'Y' : 'N';
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM hh:mm aa");

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

        /* Currently supported for one date in v0.1 */
        /* TODO: Support for multiple dates for tentative tasks */
        if (!dates.isEmpty()) {
            DatePair dp = dates.get(0);
            if (dp.hasDateRange()) {
                dateList.add(dateFormat.format(dp.getStartDate().getTime())
                        + " to");
                dateList.add(dateFormat.format(dp.getEndDate().getTime()));
            } else if (dp.hasEndDate()) {
                dateList.add(dateFormat.format(dp.getEndDate().getTime()));
            }
        }

        /* Format all fragments in desc and date into multiple lines */
        while (!wordWrapList.isEmpty() || !dateList.isEmpty()) {
            String desc = wordWrapList.isEmpty() ? "" : wordWrapList
                    .removeFirst();

            String date = dateList.isEmpty() ? "" : dateList.removeFirst();

            if (stringBuilder.length() != 0) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(String.format("%-7s%-6s%-43s%-23s", "",
                        "", desc, date));
            } else {
                stringBuilder.append(String.format("%-7s%-6s%-43s%-23s",
                        displayingId, isDone, desc, date));
            }

        }

        return stringBuilder.toString();
    }

    public boolean isFloatingTask() {
        return dateList.isEmpty();
    }

    public boolean isDeadline() {
        return (dateList.size() == 1 && dateList.get(0).isDeadline());
    }

    public Calendar getEarliestDate() {
        if (isFloatingTask()) {
            throw new UnsupportedOperationException(
                    "No date in a floating task.");
        }

        Calendar earliestDate;
        if (isDeadline()) {
            earliestDate = dateList.get(0).getEndDate();
        } else {
            earliestDate = dateList.get(0).getStartDate();
        }

        for (DatePair dp : dateList) {
            if (dp.hasStartDate() && dp.getStartDate().before(earliestDate)) {
                earliestDate = dp.getStartDate();
            }
            if (dp.hasEndDate() && dp.getEndDate().before(earliestDate)) {
                earliestDate = dp.getEndDate();
            }
        }

        return earliestDate;
    }

    @Override
    public int compareTo(Task o) {
        assert (o != null);

        if (this.isDeadline() && !o.isDeadline()) {
            return -1;
        } else if (!this.isDeadline() && o.isDeadline()) {
            return 1;
        }

        if (this.isFloatingTask() && o.isFloatingTask()) {
            return 0;
        } else if (this.isFloatingTask()) {
            return 1;
        } else if (o.isFloatingTask()) {
            return -1;
        }

        return this.getEarliestDate().compareTo(o.getEarliestDate());
    }

}
