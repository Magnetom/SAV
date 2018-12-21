package odyssey.projects.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import odyssey.projects.sav.driver.Settings;

/**
 * Created by Odyssey on 25.04.2017.
 */

public class LocalSettings {

    // Текущее транспортное средство.
    public static final String SP_VEHICLE           = "vehicle";
    public static final String SP_SERVER_ADDRESS    = "server_address";
    public static final String SP_USE_SSID_FILTER   = "use_ssid_filter";
    public static final String SP_ALLOWED_WIFI_SSID = "pref_wifi_ssid";
    public static final String SP_NOT_FIRST_JOIN    = "not_first_join";
    public static final String SP_ALL_DB_REMOVE     = "all_db_remove";
    public static final String SP_GLOBAL_ENABLE     = "global_enable";
    public static final String SP_USE_VIBRO         = "use_vibro";
    public static final String SP_USE_MUSIC         = "use_music";
    public static final String SP_USE_SCREEN_WAKEUP = "use_screen_wakeup";

    public static final String SP_USE_POPUP_INFO  = "use_popup_info";
    public static final String SP_USE_POPUP_WARN  = "use_popup_warn";
    public static final String SP_USE_POPUP_ERROR = "use_popup_error";


    private static final String APP_DEFAULT_PREFERENCES = "AppSettings";

    private static LocalSettings instance;
    private SharedPreferences sPref;

    private LocalSettings(Context context, String prefName){
        sPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    private LocalSettings(Context context){
        //sPref = context.getSharedPreferences(APP_DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Настройки, которые будут применены при первом запуске.
        firstJoin();
    }

    public static LocalSettings getInstance(Context context){
        if (instance == null) return instance = new LocalSettings(context);
        return instance;
    }

    private void firstJoin(){
        if (!getBoolean(SP_NOT_FIRST_JOIN)){
            saveBoolean(SP_NOT_FIRST_JOIN, true);

            // Применяем настройки по-умолчанию (при первом запуске програмы).
            saveBoolean(SP_GLOBAL_ENABLE, false); // Глобальное разрешение работы приложение - ЗАПРЕЩЕНО.
            saveBoolean(SP_USE_SSID_FILTER, true); // Использовать SSID-фильтрацию.
            saveBoolean(SP_USE_VIBRO, true); // Использовать виро-оповещение об успешной отметке.
            saveBoolean(SP_USE_MUSIC, true); // Использовать аудио-оповещение об успешной отметке.
            saveBoolean(SP_USE_SCREEN_WAKEUP, true); // Использовать пробуждение экрана после удачной отметки на сервере.
            saveText(SP_ALLOWED_WIFI_SSID, Settings.ALLOWED_WIFI_DEFAULT_SSID); // Одобренный SSID по-умолчанию.
            saveText(SP_SERVER_ADDRESS,    Settings.DB_SERVER_DEFAULT_ADDRESS); // Адрес/имя удаленного сервера.

            saveBoolean(SP_USE_POPUP_INFO,  true); // Использовать всплывающие информационные сообщения.
            saveBoolean(SP_USE_POPUP_WARN,  true); // Использовать всплывающие предупреждения.
            saveBoolean(SP_USE_POPUP_ERROR, true); // Использовать всплывающие сообщения об ошибках.

        }
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

    public boolean getBoolean(String key){
        return sPref.getBoolean(key, false);
    }

    public void saveBoolean(String key, boolean value){
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public SharedPreferences getSharedPrefInstance(){
        return sPref;
    }

    public String getScriptUrl(String script_name){
        return Settings.MAIN_PROTOCOL + getText(SP_SERVER_ADDRESS) + Settings.WORK_DIRECTORY + script_name;
    }
}
