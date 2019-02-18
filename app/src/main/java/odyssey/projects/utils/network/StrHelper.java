package odyssey.projects.utils.network;

import android.support.annotation.NonNull;

public class StrHelper {

    @NonNull
    public static String trimAll(String str) {
        if (!isEmpty(str)) {
            return trimQuotes(trimSpaces(str));
        }
        return str;
    }

    @NonNull
    public static String trimSpaces(String str) {
        if (!isEmpty(str)) {
            return str.replaceAll("^ *", "").replaceAll(" *$", "");
        }
        return str;
    }

    @NonNull
    public static String trimQuotes(String str) {
        if (!isEmpty(str)) {
            return str.replaceAll("^\"*", "").replaceAll("\"*$", "");
        }
        return str;
    }

    private static boolean isEmpty(CharSequence str) {
        return str == null || str.toString().isEmpty();
    }
}
