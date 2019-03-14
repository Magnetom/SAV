package odyssey.projects.sav;

public class Settings {

    public static final String DEFAULT_MAIN_PROTOCOL             = "http://";
    public static final String DEFAULT_DB_SERVER_DEFAULT_ADDRESS = "192.168.1.231";
    public static final String DEFAULT_WORK_DIRECTORY            = "/";

    public static final String DEFAULT_REMOTE_ADDRESS = DEFAULT_MAIN_PROTOCOL+DEFAULT_DB_SERVER_DEFAULT_ADDRESS+DEFAULT_WORK_DIRECTORY;


    // Имя скрипта для отметки клиента.
    public static final String UPLOAD_SCRIPT = "upload.php";
    public static final String UPLOAD_SCRIPT_DEFAULT_ADDRESS = DEFAULT_REMOTE_ADDRESS+UPLOAD_SCRIPT;

    // Client remote access token.
    public static final String CLIENT_REQUEST_TOKEN =   "F284F583BJB78IF3U8H458WUBFG8V356W8IEUF";
    static final String SERVER_REQUEST_TOKEN =   "K22J82FOWEFO3N1BE7WLOHQWOPP6NCAGDCI8UB";

    // Если сообщение от сервера: "ошибка".
    static final String GENERAL_ERROR    = "error";
    // Если сообщение от сервера: "успех".
    static final String GENERAL_SUCCESS  = "success";

    public static final String ACTION_TYPE_CMD = "cmd";


}
