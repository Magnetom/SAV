package odyssey.projects.pref;

public final class SettingsCache {

    public static boolean GLOBAL_ENABLE     = false;

    public static String  VEHICLE            = "";
    public static String  SERVER_ADDRESS     = "";
    public static boolean USE_BSSID_FILTER   = true;
    public static boolean USE_SSID_FILTER    = true;
    public static String  ALLOWED_WIFI_BSSID = "";
    public static String  ALLOWED_WIFI_SSID  = "";
           static boolean WIFI_CONFIG_RESET = false;
    public static boolean WIFI_AUTO_CONFIGURED = false;


    public static boolean USE_VIBRO         = true;
    public static boolean USE_MUSIC         = true;
    public static boolean USE_SCREEN_WAKEUP = true;

    public static boolean USE_POPUP_INFO  = true;
    public static boolean USE_POPUP_WARN  = true;
    public static boolean USE_POPUP_ERROR = true;

    public static boolean USE_DEBUG_LOG        = false;
    public static int     DEBUG_LOG_MAX_LINES  = 1000;
    public static boolean DEBUG_LOG_INFO       = false;
    public static boolean DEBUG_LOG_WARN       = false;
    public static boolean DEBUG_LOG_ERROR      = false;


}
