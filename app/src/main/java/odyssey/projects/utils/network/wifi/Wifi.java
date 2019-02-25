package odyssey.projects.utils.network.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

import odyssey.projects.sav.driver.Settings;

import static android.content.Context.WIFI_SERVICE;

public class Wifi {

    public static final String TAG = "AURA_WIFI";

    // Включение модуля Wi-Fi.
    public static void enableWifi(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    // Отключение модуля Wi-Fi.
    public static void disableWifi(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    public static boolean isWifiEnabled(final Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }



    public static void connectToNetworkId(final Context context, int networkId) {
        if (networkId < 0) return;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiManager.disconnect();
        wifiManager.enableNetwork(networkId, true);
        wifiManager.reconnect();
    }

    private static void _connectToWiFi(final Context context, String wifiName) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(configuration);
        List<WifiConfiguration>
                list = wifiManager.getConfiguredNetworks(); //возвращает список сетей
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + wifiName + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();
                break;
            }
        }
    }

    /* Подсоединиться к предпочтительной WiFi сети с нужным SSID. Версия 2. */
    public static boolean connectToWifi(final Context context, final String SSID, final String BSSID) {

        Log.i(TAG, "Received request to connect to the WiFi network with:\r\nSSID: "+(SSID!=null?SSID:"null (any network)")+"\r\nBSSID:"+(BSSID!=null?BSSID:"null (any network)"));

        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        final WifiPointInfo searchPointInfo = new WifiPointInfo(SSID, BSSID);

        ///////////////////////////////////////////////////////////////
        // Шаг #1: Если не требуется подключение к определенной WiFi //
        //         сети, то просто включаем модуль.                  //
        ///////////////////////////////////////////////////////////////
        if (SSID == null && BSSID == null) {
            Log.i(TAG, "SSID and BSSID is equal NULL -> just enable WiFi module.");
            // Включаем модуль WiFi.
            return wifiManager.setWifiEnabled(true);
        }

        // Получаем состояние модуля WiFi: ВКЛ./ОТКЛ.
        if ( wifiManager.isWifiEnabled() ){

            Log.i(TAG, "WiFi module is already enabled.");

            String currSSID  = wifiManager.getConnectionInfo().getSSID();
            String currBSSID = wifiManager.getConnectionInfo().getBSSID();

            // Если требуемое соединение уже установлено, завершаем работу функции
            if (WifiHelper.isWifiParamsEquals(new WifiPointInfo(currSSID, currBSSID) , searchPointInfo)) {
                Log.i(TAG, "Required WiFi connection already in use. No need to reconnect.");
                return true;
            }

            /////////////////////////////////////////////////////////////////
            // Шаг #2: Если модуль WiFi уже включен, то проверяем          //
            // существование уже имеющихся настроек для такого соединения  //
            // в менеджере соединений.                                     //
            /////////////////////////////////////////////////////////////////
            Log.i(TAG, "Going through the all predefined WiFi configurations ...");
            List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
            if (wifiConfigurations != null) {
                for (WifiConfiguration wifiConfiguration : wifiConfigurations) {

                    currSSID  = wifiConfiguration.SSID;
                    currBSSID = wifiConfiguration.BSSID;

                    if (WifiHelper.isWifiParamsEquals(new WifiPointInfo(currSSID, currBSSID), searchPointInfo)) {
                        Log.i(TAG, "Ok, required configuration is found. It will enable now ...");

                        ////////////////////////////////////////////////////////////////////////////
                        /* ToDo: проверить текущий пароль и изменить его принеобходимости. */
                        //wifiConfiguration.preSharedKey = "\""+ Settings.DEFAULT_WIFI_PASSWORD+"999\"";
                        ////////////////////////////////////////////////////////////////////////////

                        // Подключаемся к выбранной сети
                        wifiNetworkReconnect(wifiManager, wifiConfiguration.networkId);
                        return true;
                    }
                }
            }
            Log.i(TAG, "Suitable configuration not fount! Trying get WiFi Scan Results.");

            //////////////////////////////////////////////////////////////////////////////
            // Шаг #3: Если модуль WiFi уже включен, но среди настроенных конфигураций  //
            // не оказалось настройки для сети с нужным SSID, то запрашиваем результаты //
            // сканирования доступных WiFi сетей.                                       //
            //////////////////////////////////////////////////////////////////////////////

            // Получаем список доступных на текущий момент WiFi сетей.
            List<ScanResult> results =  wifiManager.getScanResults();
            // Пытаемся подсоединится к искомой сети, если она есть в списке.
            return connectToSSID (wifiManager, results, searchPointInfo);

        } else {

            ////////////////////////////////////////////////////////////////////////////////
            // Шаг #4: В противном случае (модуль WiFi - выключен) включаем модуль WiFi   //
            // и ждем завершения его включения. Затем просматриваем все доступные         //
            // преднастроенные конфигурации и поключаемся к искомой. Если безрезультатно, //
            // то сканируем доступные WiFi сети и подключаемся к искомой, если имеется.   //
            ////////////////////////////////////////////////////////////////////////////////
            Log.i(TAG, "WiFi module is OFF now. Trying to turn it ON.");
            // Включаем WiFi.
            if (!wifiManager.setWifiEnabled(true)) {
                Log.e(TAG, "Can't request to enable WiFI module!");
                return false;
            }

            // Создается фильтр широковещательных сообщений, чтобы знать об изменениях в состоянии модуля.
            final IntentFilter filters = new IntentFilter();
            filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            filters.addAction("android.net.wifi.STATE_CHANGE");

            Log.i(TAG, "The broadcast receiver [WiFiEnabled] is registered.");
            // Регистрируется приемник широковещательных сообщений с настроенным фильтром.
            context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    // Проверка состояния модуля. Нам интересен момент, когда модуль включится.
                    if (wifiManager.isWifiEnabled()){

                        Log.i(TAG, "WiFi module is enable now.");

                        // Отписываемся от сообщений системы касаемых изменения состояния модуля.
                        try {
                            context.getApplicationContext().unregisterReceiver(this);
                        } catch (IllegalArgumentException e){
                            e.printStackTrace();
                        }

                        /////////////////////////////////////////////////////////////
                        // Шаг #4.1: Проверка существования уже имеющихся настроек //
                        //         для такого соединения в менеджере соединений.   //
                        /////////////////////////////////////////////////////////////
                        Log.i(TAG, "Going through the all predefined WiFi configurations ...");
                        List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
                        if (wifiConfigurations != null) {
                            for (WifiConfiguration wifiConfiguration : wifiConfigurations) {

                                String currSSID  = wifiConfiguration.SSID;
                                String currBSSID = wifiConfiguration.BSSID;

                                if (WifiHelper.isWifiParamsEquals(new WifiPointInfo(currSSID, currBSSID), searchPointInfo)) {
                                    Log.i(TAG, "Ok, required configuration is found. It will apply right now ...");

                                    ////////////////////////////////////////////////////////////////////////////
                                    /* ToDo: проверить текущий пароль и изменить его принеобходимости. */
                                    //wifiConfiguration.preSharedKey = "\""+ Settings.DEFAULT_WIFI_PASSWORD+"999\"";
                                    ////////////////////////////////////////////////////////////////////////////

                                    // Подключаемся к выбранной сети
                                    wifiNetworkReconnect(wifiManager, wifiConfiguration.networkId);
                                    return;
                                }
                            }
                        }
                        Log.i(TAG, "Suitable configuration not fount! Trying get WiFi Scan Results.");

                        ////////////////////////////////////////////////////////////
                        // Шаг #4.2: Если готовые настройки не были найдены - они //
                        //         создаютя вручную и применяются.                //
                        ////////////////////////////////////////////////////////////
                        List<ScanResult> results =  wifiManager.getScanResults();

                        // Если результаты сканирования сети уже получены.
                        if (results != null && !results.isEmpty()){
                            Log.i(TAG, "Scan Results is ready. Connecting to our network.");
                            // Пытаемся подсоединится к искомой сети, если она есть в списке.
                            if (connectToSSID (wifiManager, results, searchPointInfo)) return;
                        }

                        Log.w(TAG, "Scan Results is not ready! The broadcast receiver [ScanResultsAvailable] is registered.");

                        // Если результаты сканирования еще не получены или нужной сети еще нет в радиусе действия,
                        // то делаем скан-запрос доступных сетей.
                        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                            // Ожидаем результатов сканирования.
                            @Override
                            public void onReceive(Context context, Intent intent) {

                                List<ScanResult> results =  wifiManager.getScanResults();
                                if (results != null && !results.isEmpty()){
                                    Log.i(TAG, "Scan Results is ready. Connecting to our network.");
                                    // Пытаемся подсоединится к искомой сети, если она есть в списке.
                                    connectToSSID (wifiManager, results, searchPointInfo);
                                }
                                Log.i(TAG, "Unregister Receiver [ScanResultsAvailable].");
                                // Отписываемся от сообщений системы о результатах сканирования.
                                try {
                                    context.getApplicationContext().unregisterReceiver(this);
                                } catch (IllegalArgumentException e){
                                    e.printStackTrace();
                                }
                            }
                        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        // Начать сканирование.
                        wifiManager.startScan();
                        Log.i(TAG, "Start WiFi network scanning ...");
                    }
                }
            }, filters);
        }

        return true;
    }

    private static boolean connectToSSID (WifiManager wifiManager, List<ScanResult> results, WifiPointInfo pointInfo){
        // Проверка условий подключения.
        if (results == null || wifiManager == null || pointInfo.getSSID() == null) return false;

        // Перебираем результаты сканирования доступных сетей.
        for (ScanResult result : results) {

            // Если среди видимых сетей есть искомая сеть, создаем конфигурацию и подключаемся к ней.
            if (WifiHelper.isWifiParamsEquals(new WifiPointInfo(result.SSID, result.BSSID), pointInfo)){
                Log.i(TAG, "The WiFi network we're looking for is in the scope: " + pointInfo.getSSID());
                Log.i(TAG, "Creating WiFi configuration automatically ...");
                // Создается конфигурация на основе данных, полученых при сканировании доступных сетей.
                WifiConfiguration wifiConfig = createWifiConfig(result, pointInfo.getSSID(), Settings.DEFAULT_WIFI_PASSWORD);
                // Регистрируем созданную конфигурацию.
                int networkId = wifiManager.addNetwork(wifiConfig);
                if (networkId == -1) {
                    Log.e(TAG, "Couldn't add network with SSID: " + pointInfo.getSSID());
                    return false;
                }
                // Подключается к выбранной сети по только-что созданной конфигурации.
                return wifiNetworkReconnect(wifiManager, networkId);
            }
        }
        Log.i(TAG, "The WiFi network we're looking for is out of scope. Please, try the scan again later.");
        // В противном случае - искомая сеть вне зоны доступа.
        return false;
    }

    private static boolean wifiNetworkReconnect(WifiManager wifiManager, int networkId){
        Log.i(TAG, "Connecting to network with id: " + networkId);
        if (!wifiManager.disconnect()) return false;
        if (!wifiManager.enableNetwork(networkId, true)) return false;
        return wifiManager.reconnect();
    }

    private static WifiConfiguration createWifiConfig(ScanResult scanResult, String SSID, String password){

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.status = WifiConfiguration.Status.ENABLED;
        conf.priority = 40;
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        if (scanResult.capabilities.toUpperCase().contains("WEP")) {
            Log.v("rht", "Configuring WEP");

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

            if (password.matches("^[0-9a-fA-F]+$")) {
                conf.wepKeys[0] = password;
            } else {
                conf.wepKeys[0] = "\"".concat(password).concat("\"");
            }

            conf.wepTxKeyIndex = 0;

        } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {
            Log.v("rht", "Configuring WPA");
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.preSharedKey = "\"" + password + "\"";

        } else {
            Log.v("rht", "Configuring OPEN network");
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedAuthAlgorithms.clear();
        }

        return conf;
    }

    public static boolean removeWifiConfiguration(Context context, String SSID){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return removeWifiConfiguration(wifiManager, SSID);
    }

    private static boolean removeWifiConfiguration(WifiManager wifiManager, String SSID){
        int networkId = getExistingNetworkId(wifiManager, SSID);
        if (networkId == -1) return true;

        return wifiManager.removeNetwork(networkId);
    }

    private static int getExistingNetworkId(WifiManager wifiManager, String SSID) {
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (WifiHelper.areSsidEqual(WifiHelper.trimQuotes(existingConfig.SSID), WifiHelper.trimQuotes(SSID))) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }
}
