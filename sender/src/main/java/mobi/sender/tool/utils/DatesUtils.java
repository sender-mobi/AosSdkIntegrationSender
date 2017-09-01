package mobi.sender.tool.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Zver on 11.10.2016.
 */
public class DatesUtils {

    /**
     * Return date in specified format.
     *
     * @param milliSeconds Date in milliseconds
     * @return String representing date in specified format
     */
    public static Date getDate(long milliSeconds) {
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return calendar.getTime();
    }

    public static boolean isThatOneDay(long first, long second) {

        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(first);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(second);

        cal1.add(Calendar.DATE, 0);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE);

    }
}
