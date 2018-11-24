package odyssey.projects.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.util.Random;

/**
 * Created by Odyssey on 18.05.2017.
 */

public class UidUtils {

    private static final String GOOGLE_ACCOUNT_TAG = "com.google";

    /*
    brief: Generating Unique Identification string based on a HEX numbers.
           For example: 09df-3adb-962e-17fa
    */
    public static String getStrUid(){

        final Boolean USE_SEPARATOR = false;

        int rand;
        final int    min            = 0;
        final int    max            = 255;
        final int    uid_len        = 8;
        final int    separator_step = 2;
        final String separator      = "-";

        String uid = "";
        String tmp;
        Random r   = new Random();

        for (int ii=0;ii<uid_len;ii++){
            rand = r.nextInt(max - min + 1) + min;
            tmp = Integer.toString(rand, 16);
            if (tmp.length() < 2) tmp = "0"+tmp;
            uid = uid+tmp;
            if (USE_SEPARATOR) {if ( (ii<(uid_len-1)) && ( ((ii+1)%separator_step)==0 )) uid += separator;}
        }
        return uid;
    }

    /*
    brief: Generating Unique Identification as integer.
           For example: 1853601
    */
    public static int getIntUid(){

        final int min = 0x0000;
        final int max = 0xFFFFFF;

        Random r = new Random();
        return (r.nextInt(max - min + 1) + min);
    }
}
