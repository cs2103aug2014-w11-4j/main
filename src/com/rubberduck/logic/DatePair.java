package com.rubberduck.logic;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * This DatePair class is used to store a pair of date in the form of Calendar
 * object which represent the possible start date and end date.
 */
//@author A0111794E
public class DatePair implements Serializable {

    private Calendar startDate = null;
    private Calendar endDate = null;

    /**
     * Basic Constructor for DatePair, create a DatePair without any
     * initialization.
     */
    public DatePair() {
        this.startDate = null;
        this.endDate = null;

    }

    /**
     * Overloaded Constructor for DatePair, create a DatePair with only endDate
     * If one date is present, it will be taken as end date.
     *
     * @param endDate the endDate of the task
     */
    public DatePair(Calendar endDate) {
        this.endDate = endDate;
        this.startDate = null;
    }

    /**
     * Overloaded constructor for DatePair, create DatePair with startDate and
     * endDate If start Date entered is later then endDate, it will be swapped.
     * over
     *
     * @param startDate the starting date of the task
     * @param endDate   the ending date of the task
     */
    public DatePair(Calendar startDate, Calendar endDate) {
        if (startDate.equals(endDate)) {
            startDate = null;
            this.endDate = endDate;
        } else if (startDate.after(endDate)) {
            this.startDate = endDate;
            this.endDate = startDate;
        } else {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    /**
     * Get the startDate of the task.
     *
     * @return starting date of the task
     */
    public Calendar getStartDate() {
        return this.startDate;
    }

    /**
     * Get the endDate of the task.
     *
     * @return ending date of the task
     */
    public Calendar getEndDate() {
        return this.endDate;
    }

    /**
     * Set the startDate of the task.
     *
     * @param startDate the date when the task starts
     */
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * Set the endDate of the task.
     *
     * @param endDate the date when the task ends
     */
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     * Check if the task has startDate.
     *
     * @return if the task has startDate
     */
    public boolean hasStartDate() {
        return (this.startDate != null);
    }

    /**
     * Check if the task has endDate.
     *
     * @return if the task has endDate
     */
    public boolean hasEndDate() {
        return (this.endDate != null);
    }

    /**
     * Check if the task has DateRange.
     *
     * @return if the task has DateRange
     */
    public boolean hasDateRange() {
        return (this.startDate != null && this.endDate != null);
    }

    /**
     * Check if the task has endDate.
     *
     * @return if the task has endDate
     */
    public boolean isDeadline() {
        return (!hasStartDate() && hasEndDate());
    }

    /**
     * Overwrite the default to string value of task Formatted for RubberDuck
     * console design.
     */
    @Override
    public String toString() {
        String formattedStartDate, formattedEndDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss",
                                                           Locale.US);

        if (hasStartDate()) {
            formattedStartDate = dateFormat.format(startDate.getTime());
        } else {
            formattedStartDate = "[No Start Date]";
        }

        if (hasEndDate()) {
            formattedEndDate = dateFormat.format(endDate.getTime());
        } else {
            formattedEndDate = "[No End Date]";
        }

        return String.format("%s %s", formattedStartDate, formattedEndDate);

    }

    /**
     * Test if there is overlap between two DatePairs. Null values (no start/end
     * date) are considered as infinitely early/late.
     *
     * @param dateRange another DatePair to be compared with
     * @return true if there is overlap between two DatePairs
     */
    //@author A0119416H
    public boolean isWithinPeriod(DatePair dateRange) {
        Calendar startDateCriteria = dateRange.getStartDate();
        Calendar endDateCriteria = dateRange.getEndDate();

        if ((startDate == null && endDate == null) ||
            (startDateCriteria == null && endDateCriteria == null)) {
            return true;
        }

        if (startDateCriteria == null) {
            return (startDate == null) || (!startDate.after(endDateCriteria));
        }

        if (endDateCriteria == null) {
            return (endDate == null) || (!endDate.before(startDateCriteria));
        }

        if (endDate == null) {
            return (!startDate.after(endDateCriteria));
        }

        if (startDate == null) {
            return (!endDate.after(endDateCriteria));
        }

        return startDate.before(endDateCriteria) &&
               endDate.after(startDateCriteria);
    }

    /**
     * Check if any end date in the DateList has already past the current date
     * and time during execution.
     *
     * @param dateList the ArrayList of DatePair
     * @return true if there is a date that has already past else false
     */
    //@author A0111794E
    public static boolean isDateBeforeNow(ArrayList<DatePair> dateList) {
        if (dateList.size() > 0) {
            for (DatePair dp : dateList) {
                if (dp.getEndDate().before(Calendar.getInstance())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

}
