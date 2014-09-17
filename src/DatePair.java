import java.io.Serializable;
import java.util.GregorianCalendar;

/**
 * This DatePair class is used to store a pair of date
 * in the form of GregorianCalendar object which represent
 * the possible start date and end date.
 *
 * @author Sia Wei Kiat Jason
 */
public class DatePair implements Serializable {
    /* Default date: 01 January 1970 */
    private GregorianCalendar startDate = new GregorianCalendar();
    private GregorianCalendar endDate = new GregorianCalendar();

    public DatePair() {

    }

    public DatePair(GregorianCalendar startDate) {
        this.startDate = startDate;
    }

    public DatePair(GregorianCalendar startDate, GregorianCalendar endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public GregorianCalendar getStartDate() {
        return this.startDate;
    }

    public GregorianCalendar getEndDate() {
        return this.endDate;
    }

    public void setStartDate(GregorianCalendar startDate) {
        this.startDate = startDate;

    }

    public void setEndDate(GregorianCalendar endDate) {
        this.endDate = endDate;
    }

}
