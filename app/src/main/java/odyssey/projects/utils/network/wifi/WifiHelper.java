package odyssey.projects.utils.network.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import static android.content.Context.WIFI_SERVICE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static java.lang.String.format;

public class WifiHelper {

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

    public static boolean areSsidEqual(String SSID, String anotherSSID) {
        return TextUtils.equals(trimAll(SSID), trimAll(anotherSSID));
    }

    private static boolean areBssidEqual(String BSSID, String anotherBSSID) {
        return TextUtils.equals(trimAll(BSSID).toLowerCase(), trimAll(anotherBSSID).toLowerCase());
    }

    static String formatSSID(String wifiSSID) {
        return format("\"%s\"", wifiSSID);
    }
/*
    static boolean isWifiParamsEquals(WifiManager wifiManager, String SSID, String BSSID){

        final boolean useSsidFilter  =  SSID != null;
        final boolean useBssidFilter = BSSID != null;

        return  ((!useSsidFilter)  || WifiHelper.areSsidEqual (wifiManager.getConnectionInfo().getSSID(),   SSID)) &&
                ((!useBssidFilter) || WifiHelper.areBssidEqual(wifiManager.getConnectionInfo().getBSSID(), BSSID));
    }
*/
    static boolean isWifiParamsEquals(@NonNull WifiPointInfo pattern, @NonNull WifiPointInfo B){

        final boolean useSsidFilter  = (B.getSSID()  != null) && (pattern.getSSID()  != null);
        final boolean useBssidFilter = (B.getBSSID() != null) && (pattern.getBSSID() != null);

        return  ((!useSsidFilter)  || WifiHelper.areSsidEqual (pattern.getSSID(),  B.getSSID())) &&
                ((!useBssidFilter) || WifiHelper.areBssidEqual(pattern.getBSSID(), B.getBSSID()));
    }

    public static boolean isWiFiNetwork(final Context context) {
        // Получаем контекст менеджера соединений.
        final ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        // Если есть активное соединение с какой-либо сетью ...
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()){
            // Получаем информацию о текущем сетевом соединении.
            NetworkInfo NetInfo = cm.getActiveNetworkInfo();
            // Если это сеть Wi-Fi - возвращаем TRUE.
            if (NetInfo != null){
                return NetInfo.getType() == TYPE_WIFI;
            }
        }
        return false;
    }

    /** Получить SSID имя WiFi соединения через Connectivity Manager. */
    public static String getWifiSSID_Connectivity(final Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // Проверка условий.
        if (networkInfo == null || networkInfo.getExtraInfo() == null) return null;
        return networkInfo.getExtraInfo();
    }

    /** Get WiFi network SSID (name). */
    public static String getWifiSSID(final Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info != null) return info.getSSID();
        return  null;
    }

    /** Get WiFi router BSSID (MAC-address). */
    public static String getWifiBSSID(final Context context) {
        WifiInfo info = getWifiInfo(context);
        if (info != null) return info.getBSSID();
        return  null;
    }

    private static WifiInfo getWifiInfo(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    public static boolean setSsidAndPassword(Context context, String ssid, String ssidPassword) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            Method getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);

            wifiConfig.SSID = ssid;
            wifiConfig.preSharedKey = ssidPassword;

            Method setConfigMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setConfigMethod.invoke(wifiManager, wifiConfig);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static NetworkInterface getActiveWifiInterface(Context context) throws SocketException {
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //Return dynamic information about the current Wi-Fi connection, if any is active.
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo == null) return null;
        InetAddress address = intToInet(wifiInfo.getIpAddress());
        return NetworkInterface.getByInetAddress(address);
    }

    public static int getActiveWifiNetworkId(Context context) {
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //Return dynamic information about the current Wi-Fi connection, if any is active.
        return wifiManager.getConnectionInfo().getNetworkId();
    }

    private static byte byteOfInt(int value, int which) {
        int shift = which * 8;
        return (byte)(value >> shift);
    }

    private static InetAddress intToInet(int value) {
        byte[] bytes = new byte[4];
        for(int i = 0; i<4; i++) {
            bytes[i] = byteOfInt(value, i);
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // This only happens if the byte array has a bad length
            return null;
        }
    }
}
