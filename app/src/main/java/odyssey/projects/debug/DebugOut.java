package odyssey.projects.debug;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import odyssey.projects.debug.LogItemType;
import odyssey.projects.debug.LogViewer;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.pref.SettingsCache;

public class DebugOut {

    private static final String TAG = "DEBUG_OUT";


    public static void generalPrintInfo(Context context, String info, String tag){
        if (SettingsCache.USE_POPUP_INFO)
        toastPrint(context, "Информация:", info, tag);

        // Выводим сообщение в Debug ListView
        logPrint(tag, LogItemType.TYPE_INFO, info);
    }

    public static void generalPrintWarning(Context context, String warn, String tag){
        if (SettingsCache.USE_POPUP_WARN)
        toastPrint(context, "Внимание!", warn, tag);

        // Выводим сообщение в Debug ListView
        logPrint(tag, LogItemType.TYPE_WARNING, warn);
    }

    public static void generalPrintError(Context context, String error, String tag){
        if (SettingsCache.USE_POPUP_ERROR)
        toastPrint(context, "Ошибка", error, tag);

        // Выводим сообщение в Debug ListView
        logPrint(tag, LogItemType.TYPE_ERROR, error);
    }

    // Вывести текстовую информацию во всплывающее сообщение типа Toast.
    private static void toastPrint(Context context, String category, String message, String tag){
        if (tag == null) tag = TAG;

        if (message != null) Log.v(tag, message);
        if (context != null && message != null) Toast.makeText(context, category+"\r\n"+message, Toast.LENGTH_LONG).show();
    }

    // Вывести текстовую информацию в отладочный лог событий на основе ListView.
    private static void logPrint(String tag, LogItemType type, String message){
        if (LogViewer.getListener()!=null) {
            if (SettingsCache.USE_DEBUG_LOG){
                if (    (type == LogItemType.TYPE_INFO    && SettingsCache.DEBUG_LOG_INFO) ||
                        (type == LogItemType.TYPE_WARNING && SettingsCache.DEBUG_LOG_WARN) ||
                        (type == LogItemType.TYPE_ERROR   && SettingsCache.DEBUG_LOG_ERROR) ){
                    LogViewer.getListener().addToLog(tag, type, message);
                }
            }
        }
    }

    public static void debugPrintException(Context context, Exception e, String tag){
        if (tag == null) tag = TAG;

        if (e.getMessage() != null) Log.e(tag, e.getMessage());
        if (context != null) Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();

        // Выводим сообщение во Debug ListView
        //logPrint(tag, LogItemType.TYPE_ERROR, e.toString());
    }

    public static void debugPrintVolleyError(Context context, Object obj, String tag){
        if (tag == null) tag = TAG;

        String errorString;

        if (obj != null) {

            if (obj instanceof String) {
                errorString = "onError string: " + obj;
            } else
            if (obj instanceof VolleyError) {
                errorString = "onError VolleyError: "+((VolleyError)obj).getMessage();
            } else {
                errorString = "Unknown Volley error ...";
            }

            if (context != null) Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
            Log.e(tag, errorString);

            // Выводим сообщение во Debug ListView
            //logPrint(tag, LogItemType.TYPE_ERROR, errorString);
        }
    }
}
