package com.rubberduck.common.datatransfer;

import com.rubberduck.storage.DatabaseManager;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * This task class is used to represent a single task object which stores all
 * relevant information about the task and provide the needed getters and
 * setters for retrieval and storage.
 */
//@author A0111794E
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
     * Creates a task with notes field only.
     *
     * @param description notes about the task
     */

    public Task(String description) {
        this(description, new ArrayList<DatePair>());
    }

    /**
     * Creates a task with notes and DatePair fields.
     *
     * @param description about the task
     * @param dateList    of possible DatePair
     */
    public Task(String description, ArrayList<DatePair> dateList) {
        this.description = description;
        this.dateList = dateList;
        this.isDone = false;
        updateLastUpdate();
        resetUuid();
    }

    /**
     * Change the description of the notes.
     *
     * @param description of the task
     */
    public void setDescription(String description) {
        this.description = description;
        updateLastUpdate();
    }

    /**
     * Returns the description of the class.
     *
     * @return the description of the class
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set a new dateList of the Task object.
     *
     * @param dateList of possible DatePair
     */

    public void setDateList(ArrayList<DatePair> dateList) {
        this.dateList = dateList;
        updateLastUpdate();
    }

    /**
     * Check if there is at least a start date or end date.
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
     * Returns an ArrayList of DatePair.
     *
     * @return the dateList of possible DatePair
     */

    public ArrayList<DatePair> getDateList() {
        return dateList;
    }

    /**
     * Add an end date to the task without a start date.
     *
     * @param endDate the dateline of the task
     */

    public void addEndDate(GregorianCalendar endDate) {
        DatePair dp = new DatePair(endDate);
        dateList.add(dp);
        updateLastUpdate();
    }

    /**
     * Adds another DatePair into DatePairList.
     *
     * @param datePair datePair to add
     */
    public void addDatePair(DatePair datePair) {
        dateList.add(datePair);
        updateLastUpdate();
    }

    /**
     * Update the task to set it to complete.
     *
     * @param isDone whether the task is completed
     */

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
        updateLastUpdate();
    }

    /**
     * Check if the status is completed.
     *
     * @return if the task is completed
     */

    public boolean getIsDone() {
        return isDone;
    }

    /**
     * Return UUID of the task.
     *
     * @return UUID as String
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
     * Get the task last updateTime as Calendar.
     *
     * @return Calendar format of last update time of the task
     */
    public Calendar getLastUpdate() {
        return this.lastUpdate;
    }

    /**
     * Update the task last updateTime (For Google Sync).
     */
    public void updateLastUpdate() {
        this.lastUpdate = Calendar.getInstance();
    }

    /**
     * Check if the dateList is empty.
     *
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
     */
    //@author A0119416H
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
     * Test whether current Task object has conflict with given Task object.
     * Only applicable for schedules and tentative schedules.
     *
     * @param t another Task object to compare
     * @return true if conflict else false
     */
    public boolean hasConflictWith(Task t) {
        if (isDeadline() || isFloatingTask()) {
            return false;
        }

        ArrayList<DatePair> dpList = t.getDateList();
        for (DatePair dp : dpList) {
            if (!t.isDeadline() && isWithinPeriod(dp)) {
                return true;
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
     * Check if the task is a floating task.
     *
     * @return if the task is a floating task
     */

    public boolean isFloatingTask() {
        return dateList.isEmpty();
    }

    /**
     * Check if the task is a deadline only task.
     *
     * @return if the task is a deadline only task
     */

    public boolean isDeadline() {
        return (dateList.size() == 1 && dateList.get(0).isDeadline());
    }

    /**
     * Check if the task is a schedule task. Do note that a tentative task is
     * also a schedule task in this implementation.
     *
     * @return if the task is a schedule task or tentative task
     */
    public boolean isSchedule() {
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
     * Check if the task is strictly a tentative task. It must have at least 2
     * date range.
     *
     * @return if the task is strictly a tentative task
     */
    //@author A0111736M
    public boolean isTentative() {
        if (dateList.size() > 1) {
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
     * Return the first DatePair from the task as String. Should not be used
     * with tentative tasks.
     *
     * @return String representation of the DatePair in this Task
     */
    //@author A0111736M
    public String getDateString() {
        if (isFloatingTask()) {
            return "No Date";
        } else {
            SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd-MMM hh:mm aa", Locale.US);
            DatePair dp = dateList.get(0);
            if (dp.hasDateRange()) {
                return dateFormat.format(dp.getStartDate().getTime())
                       + " to " + dateFormat.format(dp.getEndDate().getTime());
            } else {
                return dateFormat.format(dp.getEndDate().getTime());
            }
        }
    }

    /**
     * Check if the task is a valid task.
     *
     * @return if the task is a valid task
     */
    public boolean checkValidity() {
        return isFloatingTask() || isDeadline() || isSchedule() ||
               isTentative();
    }

    /**
     * Get the earliest Date of the task.
     *
     * @return the earliest Date of the task
     */
    public Calendar getEarliestDate() {
        assert !isFloatingTask() : "No date in a floating task.";

        Calendar earliestDate = null;

        for (DatePair dp : dateList) {
            if (dp.hasStartDate() && (earliestDate == null ||
                                      dp.getStartDate().before(earliestDate))) {
                earliestDate = dp.getStartDate();
            }
            if (dp.hasEndDate() && (earliestDate == null ||
                                    dp.getEndDate().before(earliestDate))) {
                earliestDate = dp.getEndDate();
            }
        }

        return earliestDate;
    }

    /**
     * Compare both task by their deadline.
     * <p/>
     * Schedule Task > Deadline Task > Floating Task
     *
     * @param o the task object to be compared with the argument
     * @return int ,  0 = equal, -1 = smaller, 1 = bigger
     */
    //@author A0111794E
    @Override
    public int compareTo(Task o) {
        assert (o != null);

        if (this.isSchedule() && !o.isSchedule()) {
            return -1;
        } else if (!this.isSchedule() && o.isSchedule()) {
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

    /**
     * Method used to check whether a task has any potential conflicting
     * schedule in current database.
     *
     * @param dbManager DatabaseManager object to interact with
     * @return true if there is a conflict else false
     * @throws IOException occurs when dbManager encounters a problem with file
     */
    public boolean checkConflictWithDB(DatabaseManager<Task> dbManager,
                                       long thisTaskId) throws IOException {
        if (isFloatingTask() || isDeadline()) {
            return false;
        }

        ArrayList<Long> validIDList = dbManager.getValidIdList();
        for (Long i : validIDList) {
            Task storedTask = dbManager.getInstance(i);
            if (i != thisTaskId && !storedTask.getIsDone()) {
                if (hasConflictWith(storedTask)) {
                    return true;
                }
            }
        }

        return false;
    }
}
