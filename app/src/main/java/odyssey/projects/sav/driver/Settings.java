package odyssey.projects.sav.driver;

public class Settings {

    public static final String MAIN_PROTOCOL             = "http://";
    public static final String DB_SERVER_DEFAULT_ADDRESS = "192.168.1.231";
    public static final String WORK_DIRECTORY            = "/";

    // Имя скрипта для отметки клиента.
    public static final String MARK_SCRIPT = "mark.php";

    // SSID адрес маршрутизатора по-умолчанию.
    public static final String ALLOWED_WIFI_DEFAULT_SSID = "C0:4A:00:DA:A2:82";

    // Client remote access token.
    static final String CLIENT_REQUEST_TOKEN =   "F284F583BJB78IF3U8H458WUBFG8V356W8IEUF";
    static final String SERVER_REQUEST_TOKEN =   "K22J82FOWEFO3N1BE7WLOHQWOPP6NCAGDCI8UB";

    // Если сообщение от сервера: "ошибка".
    static final String GENERAL_ERROR    = "error";
    // Если сообщение от сервера: "успех".
    static final String GENERAL_SUCCESS  = "success";

    public static final String ACTION_TYPE_CMD = "cmd";
}
