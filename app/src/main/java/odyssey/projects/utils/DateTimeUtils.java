package odyssey.projects.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Odyssey on 27.05.2017.
 */

public class DateTimeUtils {

    public static long ONE_MINUTE = 60L*1000L;
    public static long ONE_HOUR   = 60L*ONE_MINUTE;
    public static long ONE_DAY    = 24L*ONE_HOUR;

    private final static String MMSS_TIME_FORMAT     = "mm:ss";
    private final static String KKMM_TIME_FORMAT     = "kk:mm";
    private final static String DDMMYYYY_DATA_FORMAT = "dd-MM-yyyy";
    private final static String YYYYMMDD_DATA_FORMAT = "yyyy-MM-dd";
    private final static long   MILLIS_TO_DATA  = 25L*24L*60L*60L*1000L;

    private final SimpleDateFormat simpleTimeFormat;
    private static DateTimeUtils instance = null;

    private DateTimeUtils() {
        this.simpleTimeFormat = new SimpleDateFormat(MMSS_TIME_FORMAT, Locale.getDefault());
        instance = this;
    }

    private static DateTimeUtils getInstance(){
        if (instance == null) return new DateTimeUtils();
        return instance;
    }

    private static Boolean isTimeOver(long timestamp){
        return (System.currentTimeMillis() + 999) >= timestamp;
    }
    public static long getMillisRemain(long timestamp){
        if (isTimeOver(timestamp)) return 0;
        return (timestamp-System.currentTimeMillis());
    }
    public static String extractMMSS(long timeMillis){
        if (timeMillis == 0) return getInstance().simpleTimeFormat.format(0);
        return getInstance().simpleTimeFormat.format(new Date(timeMillis+MILLIS_TO_DATA));
    }
    private static String getFormattedDataTime(long timestamp, final String format){
        SimpleDateFormat simpleFormat = new SimpleDateFormat(format, Locale.getDefault());
        if (timestamp == 0) return simpleFormat.format(0);
        return simpleFormat.format(new Date(timestamp));
    }
    public static String getKKMM(long timestamp){
        return getFormattedDataTime(timestamp, KKMM_TIME_FORMAT);
    }
    public static String getDDMMYYYY(long timestamp){
        return getFormattedDataTime(timestamp, DDMMYYYY_DATA_FORMAT);
    }

    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return dateFormat.format(new Date(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long dateTimeToTimestamp1 (String dateTime){

        String.format("%tF %<tT.%<tL", dateTime);
        return 1;
    }

    public static long dateTimeToTimestamp (String dateTime){
        long timeInMilliseconds = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date mDate = sdf.parse(dateTime);
            timeInMilliseconds = mDate.getTime();
            //System.out.println("Date in milli :: " + timeInMilliseconds);
            return timeInMilliseconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        finally {
            return timeInMilliseconds;
        }
    }


    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getTimeStamp(long timeMillis){

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return dateFormat.format(new Date(timeMillis+MILLIS_TO_DATA));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String timestampToStringYYYYMMDD(long timestamp){
        return getFormattedDataTime(timestamp, YYYYMMDD_DATA_FORMAT);
    }

    public static String getHHMMFromStringTimestamp (String strTimestamp){
        String tmp = getTimeFromStringTimestamp(strTimestamp);
        String[] a = tmp.toString().split(":");
        return a[0]+":"+a[1];
    }

    public static String getTimeFromStringTimestamp (String strTimestamp){
        String[] a = strTimestamp.toString().split(" ");
        return a[1];
    }

    public static String getDataFromStringTimestamp (String strTimestamp){
        String[] a = strTimestamp.toString().split(" ");
        return a[0];
    }

    public static String getCurrentDataYYYYMMDD(){
        return DateTimeUtils.getDataFromStringTimestamp(getCurrentTimeStamp());
    }

    public static String getLastDataYYYYMMDD(){
        return timestampToStringYYYYMMDD(System.currentTimeMillis()-ONE_DAY);
    }
}
