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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");
        String datePair = "";
        for (DatePair dp : dateList) {
            datePair = "\n" + datePair;
            String startDate = "";
            String endDate = "";
            if (dp.getStartDate() != null && dp.getEndDate() != null) {
                dateFormat.setCalendar(dp.getStartDate());
                startDate = dateFormat.format(dp.getStartDate().getTime());
                dateFormat.setCalendar(dp.getEndDate());
                endDate = dateFormat.format(dp.getEndDate().getTime());
                datePair = datePair + startDate + " " + endDate;
            } else if (dp.getStartDate() != null) {
                dateFormat.setCalendar(dp.getStartDate());
                startDate = dateFormat.format(dp.getStartDate().getTime());
                datePair = datePair + startDate + "[No End Date]";
            } else {
                dateFormat.setCalendar(dp.getEndDate());
                endDate = dateFormat.format(dp.getEndDate().getTime());
                datePair = datePair + "[No Start Date]" + endDate;

            }

        }

        if (!isDone) {
            status = "Not Done";
        }
        return description + " " + status + " " + datePair;
    }

}
