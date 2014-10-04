import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This DatePair class is used to store a pair of date
 * in the form of Calendar object which represent
 * the possible start date and end date.
 *
 * @author Sia Wei Kiat Jason
 */
public class DatePair implements Serializable {
    private Calendar startDate = null;
    private Calendar endDate = null;

    public DatePair() {
        this.startDate = null;
        this.endDate = null;

    }

    public DatePair(Calendar endDate) {
        this.endDate = endDate;
        this.startDate = null;
    }

    public DatePair(Calendar startDate, Calendar endDate) {
        if (startDate != null && startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date later than end date."); // TODO: Refactor this
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Calendar getStartDate() {
        return this.startDate;
    }

    public Calendar getEndDate() {
        return this.endDate;
    }

    // This should never be called manually, only for Java Bean.
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    // This should never be called manually, only for Java Bean.
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public String toString() {
        String formattedStartDate, formattedEndDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-YYYY HH:ss");

        if (startDate != null) {
            formattedStartDate = dateFormat.format(startDate.getTime());
        } else {
            formattedStartDate = "[No Start Date]";
        }

        if (endDate != null) {
            formattedEndDate = dateFormat.format(endDate.getTime());
        } else {
            formattedEndDate = "[No End Date]";
        }

        return String.format("%s %s", formattedStartDate, formattedEndDate);

    }

    /**
     * Test if there is overlap between two DatePairs. Null values (no start/end date) are considered as infinitely early/late.
     *
     * @param dateRange another DatePair to be compared with
     * @return true if there is overlap between two DatePairs
     * @author Huang Yue
     */
    public boolean isWithinPeriod(DatePair dateRange) { // TODO: THIS METHOD IS NOT TESTED! add test for this (V important!)

        Calendar startDateCriteria = dateRange.getStartDate();
        Calendar endDateCriteria = dateRange.getEndDate();

        if ((startDate == null && endDate == null) || (startDateCriteria == null && endDateCriteria == null)) {
            return true;
        }

        if (startDateCriteria == null) {
            return (startDate != null && startDate.before(endDateCriteria)) || (endDate.before(endDateCriteria));
        }

        if (endDateCriteria == null) {
            return (startDate != null && startDate.after(startDateCriteria)) || (endDate.after(startDateCriteria));
        }

        if (endDate == null) {
            return (!startDate.after(endDateCriteria));
        }

        if (startDate == null) {
            return (!endDate.before(startDateCriteria));
        }

        return !(startDate.after(endDateCriteria) || endDate.before(startDateCriteria));

    }
}
