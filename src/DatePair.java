import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

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
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Calendar getStartDate() {
        return this.startDate;
    }

    public Calendar getEndDate() {
        return this.endDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;

    }

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
}
