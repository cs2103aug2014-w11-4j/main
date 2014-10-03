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
    private Calendar startDate = null;
    private Calendar endDate = null;

    public DatePair() {
        this.startDate = null;
        this.endDate = null;

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
