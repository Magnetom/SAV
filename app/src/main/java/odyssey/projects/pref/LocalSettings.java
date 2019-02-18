package odyssey.projects.pref;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import odyssey.projects.About;
import odyssey.projects.sav.driver.Settings;


/**
 * Created by Odyssey on 25.04.2017.
 */

public class LocalSettings {

    public static final String SP_SW_VERSION          = "sw_version";
    public static final String SP_SW_NAME             = "sw_name";

    public static final String SP_GLOBAL_ENABLE       = "global_enable";
    public static final String SP_MULTI_VEHICLE_MODE  = "multi_vehicle_enable";

    public static final String SP_VEHICLE             = "vehicle"; // Текущее транспортное средство.

    private static final String SP_SERVER_ADDRESS     = "server_address";

    private static final String SP_USE_SSID_FILTER    = "use_ssid_filter";
    private static final String SP_USE_BSSID_FILTER   = "use_bssid_filter";
    public  static final String SP_ALLOWED_WIFI_SSID  = "pref_wifi_ssid";
    private static final String SP_ALLOWED_WIFI_BSSID = "pref_wifi_bssid";
    public  static final String SP_WIFI_CONFIG_RESET  = "wifi_config_reset";
    public  static final String SP_WIFI_AUTO_CONFIGURED = "wifi_auto_configured";

    private static final String SP_NOT_FIRST_JOIN     = "not_first_join";
    public  static final String SP_ALL_DB_REMOVE      = "all_db_remove";

    private static final String SP_USE_VIBRATION      = "use_vibration";
    private static final String SP_USE_MUSIC          = "use_music";
    private static final String SP_USE_SCREEN_WAKEUP  = "use_screen_wakeup";

    private static final String SP_USE_POPUP_INFO     = "use_popup_info";
    private static final String SP_USE_POPUP_WARN     = "use_popup_warn";
    private static final String SP_USE_POPUP_ERROR    = "use_popup_error";

    private static final String SP_USE_DEBUG_LOG        = "use_debug_log";
    private static final String SP_DEBUG_LOG_MAX_LINES  = "debug_log_max_lines";
    public  static final String SP_DEBUG_LOG_INFO       = "debug_log_info";
    public  static final String SP_DEBUG_LOG_WARN       = "debug_log_warn";
    public  static final String SP_DEBUG_LOG_ERROR      = "debug_log_error";

    private static final String APP_DEFAULT_PREFERENCES = "AppSettings";

    private static LocalSettings instance;
    private SharedPreferences sPref;

    private LocalSettings(Context context, String prefName){
        sPref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    private LocalSettings(Context context){
        //sPref = context.getSharedPreferences(APP_DEFAULT_PREFERENCES, Context.MODE_PRIVATE);
        sPref = PreferenceManager.getDefaultSharedPreferences(context);
        // Предварительные настройки.
        setup ();
        // Настройки, которые будут применены при первом запуске.
        firstJoin(false);
        // Обновляем настройки в кеше настроек.
        updateCacheSettings();
    }

    private void setup() {
        saveText(SP_SW_VERSION, About.SW_VERSION);
        saveText(SP_SW_NAME,    About.SW_NAME);
    }

    public static LocalSettings getInstance(Context context){
        if (instance == null) return instance = new LocalSettings(context);
        return instance;
    }

    public void restAllSettings(){
        firstJoin(true);
        updateCacheSettings();
    }

    private void firstJoin(boolean inAnyCase){
        if (!getBoolean(SP_NOT_FIRST_JOIN) || inAnyCase){

            saveBoolean(SP_NOT_FIRST_JOIN, true);

            // Применяем настройки по-умолчанию (при первом запуске програмы).
            saveBoolean(SP_GLOBAL_ENABLE, false);   // Глобальное разрешение работы приложение - ЗАПРЕЩЕНО.
            saveBoolean(SP_USE_BSSID_FILTER, false); // Использовать BSSID-фильтрацию.
            saveBoolean(SP_USE_SSID_FILTER, true);  // Использовать SSID-фильтрацию.

            saveBoolean(SP_USE_VIBRATION, false);     // Использовать виро-оповещение об успешной отметке.
            saveBoolean(SP_USE_MUSIC, false);         // Использовать аудио-оповещение об успешной отметке.
            saveBoolean(SP_USE_SCREEN_WAKEUP, false); // Использовать пробуждение экрана после удачной отметки на сервере.

            saveText(SP_ALLOWED_WIFI_BSSID, Settings.ALLOWED_WIFI_DEFAULT_BSSID); // Одобренный BSSID по-умолчанию.
            saveText(SP_ALLOWED_WIFI_SSID,  Settings.ALLOWED_WIFI_DEFAULT_SSID);  // Одобренный SSID по-умолчанию.
            saveText(SP_SERVER_ADDRESS,     Settings.DB_SERVER_DEFAULT_ADDRESS);  // Адрес/имя удаленного сервера.

            saveBoolean(SP_USE_POPUP_INFO,  false); // Использовать всплывающие информационные сообщения.
            saveBoolean(SP_USE_POPUP_WARN,  false); // Использовать всплывающие предупреждения.
            saveBoolean(SP_USE_POPUP_ERROR, false); // Использовать всплывающие сообщения об ошибках.

            saveBoolean(SP_USE_DEBUG_LOG, false);     // Использовать логирование отладочной информации в специальный ListView.
            saveBoolean(SP_DEBUG_LOG_INFO, true);
            saveBoolean(SP_DEBUG_LOG_WARN, true);
            saveBoolean(SP_DEBUG_LOG_ERROR, true);
            saveText(SP_DEBUG_LOG_MAX_LINES, "1000"); // Максимальное количество элементов списка оладочного лога событий.
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

    private String getText(String key){
        return sPref.getString(key, "");
    }
    private String getText(String key, String defaultValue){
        return sPref.getString(key, defaultValue);
    }

    public long getLong (String key){
        return sPref.getLong(key, 0);
    }

    public int  getInt  (String key){
        return sPref.getInt (key, 0);
    }
    public int  getInt  (String key, int defaultValue){ return sPref.getInt (key, defaultValue);}

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

    public void setSpWifiAutoConfigured(){
        saveBoolean(SP_WIFI_AUTO_CONFIGURED, true);
    }

    public void updateCacheSettings(){

        SettingsCache.GLOBAL_ENABLE      = getBoolean(SP_GLOBAL_ENABLE);

        SettingsCache.VEHICLE            = getText(SP_VEHICLE,    "");

        SettingsCache.SERVER_ADDRESS     = getText(SP_SERVER_ADDRESS,    Settings.DB_SERVER_DEFAULT_ADDRESS);
        SettingsCache.USE_BSSID_FILTER   = getBoolean(SP_USE_BSSID_FILTER);
        SettingsCache.ALLOWED_WIFI_BSSID = getText(SP_ALLOWED_WIFI_BSSID, Settings.ALLOWED_WIFI_DEFAULT_BSSID);
        SettingsCache.USE_SSID_FILTER    = getBoolean(SP_USE_SSID_FILTER);
        SettingsCache.ALLOWED_WIFI_SSID  = getText(SP_ALLOWED_WIFI_SSID, Settings.ALLOWED_WIFI_DEFAULT_SSID);
        SettingsCache.WIFI_CONFIG_RESET  = getBoolean(SP_WIFI_CONFIG_RESET);
        SettingsCache.WIFI_AUTO_CONFIGURED = getBoolean(SP_WIFI_AUTO_CONFIGURED);

        SettingsCache.USE_VIBRO         = getBoolean(SP_USE_VIBRATION);
        SettingsCache.USE_MUSIC         = getBoolean(SP_USE_MUSIC);
        SettingsCache.USE_SCREEN_WAKEUP = getBoolean(SP_USE_SCREEN_WAKEUP);

        SettingsCache.USE_POPUP_INFO  = getBoolean(SP_USE_POPUP_INFO);
        SettingsCache.USE_POPUP_WARN  = getBoolean(SP_USE_POPUP_WARN);
        SettingsCache.USE_POPUP_ERROR = getBoolean(SP_USE_POPUP_ERROR);

        SettingsCache.USE_DEBUG_LOG       = getBoolean(SP_USE_DEBUG_LOG);
        SettingsCache.DEBUG_LOG_MAX_LINES = Integer.valueOf(getText(SP_DEBUG_LOG_MAX_LINES,"1000"));
        SettingsCache.DEBUG_LOG_INFO      = getBoolean(SP_DEBUG_LOG_INFO);
        SettingsCache.DEBUG_LOG_WARN      = getBoolean(SP_DEBUG_LOG_WARN);
        SettingsCache.DEBUG_LOG_ERROR     = getBoolean(SP_DEBUG_LOG_ERROR);

    }
}
