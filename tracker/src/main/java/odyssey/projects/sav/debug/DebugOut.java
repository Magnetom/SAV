package odyssey.projects.sav.debug;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;


public class DebugOut {

    private static final String TAG = "DEBUG_OUT";


    public static void generalPrintInfo(Context context, String info, String tag){
        //if (SettingsCache.USE_POPUP_INFO)
        toastPrint(context, "Информация:", info, tag);
    }

    public static void generalPrintWarning(Context context, String warn, String tag){
        //if (SettingsCache.USE_POPUP_WARN)
        toastPrint(context, "Внимание!", warn, tag);
    }

    public static void generalPrintError(Context context, String error, String tag){
        //if (SettingsCache.USE_POPUP_ERROR)
        toastPrint(context, "Ошибка", error, tag);
    }

    // Вывести текстовую информацию во всплывающее сообщение типа Toast.
    private static void toastPrint(Context context, String category, String message, String tag){
        if (tag == null) tag = TAG;

        if (message != null) Log.v(tag, message);
        if (context != null && message != null) Toast.makeText(context, category+"\r\n"+message, Toast.LENGTH_LONG).show();
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
        }
    }
}
