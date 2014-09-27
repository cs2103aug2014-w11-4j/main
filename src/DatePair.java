import java.io.Serializable;
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
    /* Default date: 01 January 1970 */
    private Calendar startDate = new GregorianCalendar();
    private Calendar endDate = new GregorianCalendar();

    public DatePair() {

    }

    public DatePair(Calendar startDate) {
        this.startDate = startDate;
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
}
