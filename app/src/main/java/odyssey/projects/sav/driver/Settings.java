package odyssey.projects.sav.driver;

import com.android.volley.RequestQueue;

public class Settings {

    public static final String MAIN_PROTOCOL    = "http://";
    public static final String DB_SERVER_IP     = "192.168.1.231";
    public static final String WORK_DIRECTORY   = "/";

    private static final String SCRIPT_URL = MAIN_PROTOCOL + DB_SERVER_IP + WORK_DIRECTORY;

    //URL файла скрипта mark.php.
    static final String MARK_URL  = SCRIPT_URL + "mark.php";
    //static final String MARK_URL  = SCRIPT_URL + "test.php";


    // Client remote access token.
    static final String CLIENT_REQUEST_TOKEN =   "F284F583BJB78IF3U8H458WUBFG8V356W8IEUF";
    static final String SERVER_REQUEST_TOKEN =   "K22J82FOWEFO3N1BE7WLOHQWOPP6NCAGDCI8UB";

    // Если сообщение от сервера: "ошибка".
    static final String GENERAL_ERROR    = "error";
    // Если сообщение от сервера: "успех".
    static final String GENERAL_SUCCESS  = "success";
}
