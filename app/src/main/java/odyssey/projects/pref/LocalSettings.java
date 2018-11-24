package odyssey.projects.pref;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Odyssey on 25.04.2017.
 */

public class LocalSettings {

    // Текущее транспортное средство.
    public static final String SP_VEHICLE   = "vehicle";

    private static final String APP_DEFAULT_PREFERENCES = "AppSettings";

    private static LocalSettings instance;
    private SharedPreferences sPref;

    private LocalSettings(Context context, String prefName){
        sPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    private LocalSettings(Context context){
        sPref = context.getSharedPreferences(APP_DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static LocalSettings getInstance(Context context){
        if (instance == null) return instance = new LocalSettings(context);
        return instance;
    }

    public void saveText (String key, String value){

        SharedPreferences.Editor editor = sPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void saveInt (String key, int value){
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public String getText (String key){
        return sPref.getString(key, "");
    }

    public long getLong (String key){
        return sPref.getLong(key, 0);
    }
    public int  getInt  (String key){
        return sPref.getInt (key, 0);
    }

    public void clearText (String key){
        SharedPreferences.Editor editor = sPref.edit();
        editor.remove(key);
        editor.apply();
    }

    public SharedPreferences getSharedPrefInstance(){
        return sPref;
    }
}
