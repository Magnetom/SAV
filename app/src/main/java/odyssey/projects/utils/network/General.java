package odyssey.projects.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;

import odyssey.projects.utils.network.wifi.WifiHelper;

/**
 * Created by Odyssey on 28.03.2017.
 */

public class General {

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

    public static boolean isReachableByPing_wifi(Context context, String address, boolean dummy) {

        if (dummy) return true;
        try{
            InetAddress pingAddr  = InetAddress.getByName(address);
            NetworkInterface iFace = WifiHelper.getActiveWifiInterface(context);
            return pingAddr.isReachable(iFace, 128, 6000);
        }  catch (Exception e){
            e.printStackTrace();
        }
        return true;

    }

    /**
     * Checks if the current server address is reachable by ping command.
     * @param address
     * @return TRUE if reachable, otherwise FALSE.
     */
    public static boolean isReachableByPing_old(String address) {

        Runtime runtime = null;
        InetAddress InAddr = null;
        boolean reachable = false;

        try{
            runtime = Runtime.getRuntime();
            InAddr = InetAddress.getByName(address);
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 -w 2 " + InAddr.getHostAddress());
            int mExitValue = mIpAddrProcess.waitFor();

            return mExitValue == 0;
        }  catch (Exception e){
            e.printStackTrace();
        }
        return true;
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

    public static String getIpv6Address() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // Log.e(Constants.LOG_TAG, e.getMessage(), e);
        }
        return null;
    }
}
