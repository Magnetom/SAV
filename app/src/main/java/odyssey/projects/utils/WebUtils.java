package odyssey.projects.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by Odyssey on 28.03.2017.
 */

public class WebUtils {

    public boolean isURLAlive(String url_to_chk) {

        HttpURLConnection urlConnection = null;
        URL url = null;
        boolean con_valid = false;
        int ResponseCode = -1;
        String ResponceMessage = null;

        try {

            //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            //StrictMode.setThreadPolicy(policy);

            url = new URL(url_to_chk);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setRequestMethod("GET");
            //urlConnection.setRequestMethod("HEAD");
            //urlConnection.setRequestProperty("UserInfo-Agent", "Mozilla/5.0");

            urlConnection.connect();

            ResponseCode = urlConnection.getResponseCode();

            if (    ResponseCode == 200 || // HTTP_OK
                    ResponseCode == 301 || // Moved Permanently
                    ResponseCode == 302 || // Moved Temporarily or Found
                    ResponseCode == 304 || // Not Modified
                    ResponseCode == 307 ){ //Temporary Redirect

                con_valid = true;
            }
            ResponceMessage = urlConnection.getResponseMessage();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            urlConnection.disconnect();
        }
        return con_valid;
    }

    public static boolean isReachableByPing_dummy(String address) {
        return true;
    }

    /**
     * Checks if the current server address is reachable by ping command.
     * @param address
     * @return TRUE if reachable, otherwise FALSE.
     */
    public static boolean isReachableByPing(String address) {

        Runtime runtime = null;
        InetAddress InAddr = null;
        boolean reachable = false;

        try{
            runtime = Runtime.getRuntime();
            InAddr = InetAddress.getByName(address);
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + InAddr.getHostAddress());
            int mExitValue = mIpAddrProcess.waitFor();

            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }  catch (Exception e){
            e.printStackTrace();
        }
        return true;
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
                if (NetInfo.getType() == TYPE_WIFI) return true;
            }
        }
        return false;
    }

    /** Get WiFi router MAC. */
    public static String getWifiBSSID(final Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();
        if (wifi != null) {
            // Get current router MAC address
            return wifi.getBSSID();
        }
        return null;
    }

    public static boolean isNetworkConnected(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager cm = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("www.google.com"); //You can replace it with your targetName
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }
    }

}
