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
import java.util.GregorianCalendar;

public class Task implements Serializable {
    private String description = "";
    private ArrayList<DatePair> dateList = new ArrayList<DatePair>();
    private boolean isDone = false;

    /**
     * Creates a task with no fields
     */
    public Task() {

    }

    /**
     * Creates a task with notes field only
     * 
     * @param description notes about the task
     */

    public Task(String description) {
        this.description = description;
        this.isDone = false;
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
     * 
     * @return Tasks information:
     * description, status, list of DatePair
     * 
     */

    public String toString() {
        String status = "Done";

        String datePair = "";
        for (DatePair dp : dateList) {
            if (dp != null) { // TODO: why it can be null?
                datePair += ("\n" + dp.toString());
            }
        }

        if (!isDone) {
            status = "Not Done";  // TODO
        }
        return description + " " + status + " " + datePair; // TODO
    }

    /**
     * Test if there is overlap with a given DatePair.
     *
     * @param dateRange the DatePair to be compared with
     * @return true if there is overlap with the given DatePair
     * @author Huang Yue
     */
    public boolean isWithinPeriod(DatePair dateRange) { // TODO: THIS METHOD IS NOT TESTED! write tests for this
        if (dateList.size() == 0) {
            return true;
        }
        for (DatePair datePair : dateList) {
            if (datePair != null && datePair.isWithinPeriod(dateRange)) { // TODO: why it can be null?
                return true;
            }
        }
        return false;
    }

}
