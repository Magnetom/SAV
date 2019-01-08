package odyssey.projects.sav.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;


public class DebugUtils {

    public static final String TAG = "DEBUG_UTILS";

    public static void debugPrintErrorStd1(Context context, String tag){
        toastPrintError(context, "При обращении к серверу произошла ошибка! Обратитесь к системному администратору.", tag);
    }

    public static void debugPrintErrorStd2(Context context, String tag){
        toastPrintError(context, "Объект не является экземпляров класса Context.", tag);
    }

    public static void toastPrintInfo(Context context, String info, String tag){
        toastPrint(context, "Информация:", info, tag);
    }

    public static void toastPrintWarning(Context context, String warn, String tag){
        toastPrint(context, "Внимание!", warn, tag);
    }

    public static void toastPrintError(Context context, String error, String tag){
        toastPrint(context, "Ошибка", error, tag);
    }

    public static void toastPrint(Context context, String category, String error, String tag){
        if (tag == null) tag = TAG;

        if (error != null) Log.e(tag, error);
        if (context != null && error != null) Toast.makeText(context, category+"\r\n"+error, Toast.LENGTH_LONG).show();
    }


    public static void debugPrintException(Context context, Exception e, String tag){
        if (tag == null) tag = TAG;

        if (e.getMessage() != null) Log.e(tag, e.getMessage());
        if (context != null) Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
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
