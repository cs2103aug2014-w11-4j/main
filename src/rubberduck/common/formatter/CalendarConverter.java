package rubberduck.common.formatter;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A simple XStream converter that converts Calendar objects to a human readable
 * format.
 */
//@author A0119416H
public class CalendarConverter implements Converter {

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US);

    /**
     * Tells the caller that it can convert anything as long it extends
     * Calendar.
     *
     * @param clazz T Any class object
     * @return true if it can be converted, else false
     */
    public boolean canConvert(Class clazz) {
        return Calendar.class.isAssignableFrom(clazz);
    }

    /**
     * Converts Calendar object into localized String.
     *
     * @param value   Calendar object
     * @param writer  HierarchicalStreamWriter object
     * @param context MarshallingContext object
     */
    public void marshal(Object value, HierarchicalStreamWriter writer,
                        MarshallingContext context) {
        Calendar calendar = (Calendar) value;
        writer.setValue(DATE_FORMAT.format(calendar.getTime()));
    }

    /**
     * Retrieves and converts the localized DateFormat instance and parses the
     * String into a Date and puts this date into original GregorianCalendar
     * object
     *
     * @param reader  HierarchicalStreamReader object
     * @param context UnmarshallingContext object
     * @return Calendar object
     */
    public Object unmarshal(HierarchicalStreamReader reader,
                            UnmarshallingContext context) {
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            calendar.setTime(DATE_FORMAT.parse(reader.getValue()));
        } catch (ParseException e) {
            throw new ConversionException(e.getMessage(), e);
        }
        return calendar;
    }
}
