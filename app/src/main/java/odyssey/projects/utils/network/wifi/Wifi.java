package odyssey.projects.utils.network.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.util.List;

import odyssey.projects.utils.network.NetworkStateChangeListener;

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


    public static void bindToNetwork(Context context, final String networkSSID, final NetworkStateChangeListener listener) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //cm.registerNetworkCallback(
            cm.requestNetwork(
                    new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .build(),
                    // Эта функция обратного вызова {onAvailable()} будет вызвана сразу после того,
                    // как инересующая сеть будет обнаружена.
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(android.net.Network network) {
                            boolean ssidAllowed = true;

                            if (network == null) return;

                            // SSID фильтр.
                            if (networkSSID != null) {
                                NetworkInfo networkInfo = cm.getNetworkInfo(network);
                                if (networkInfo != null)
                                if (!WifiHelper.areSsidEqual(networkInfo.getExtraInfo(), networkSSID)){
                                    ssidAllowed = false;
                                }
                            }

                            if (ssidAllowed){
                                boolean connected = false;
                                //super.onAvailable(network);
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    connected = cm.bindProcessToNetwork(network);
                                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                    //noinspection deprecation
                                    connected = ConnectivityManager.setProcessDefaultNetwork(network);
                                }
                                if(connected) {
                                    if (listener!=null) listener.onNetworkBound();
                                    cm.unregisterNetworkCallback(this);
                                }
                            }
                        }
                    });
        }
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

    public static void _bindToNetwork(final Context context) {
        // Получаем доступ к менеджеру соединений.
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // set the transport type do WiFi.
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            // addTransportType-Добавляет заданный транспортный запрос к этому строителю.
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            // Эта функция обратного вызова {onAvailable()} будет вызвана только после того, как инересующая сеть будет подключена.
            connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(android.net.Network network) {
                    super.onAvailable(network);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //Use the General object to bind the process to it.
                        connectivityManager.bindProcessToNetwork(network);
                    }else { //This method was deprecated in API level 23
                        ConnectivityManager.setProcessDefaultNetwork(network);
                    }
                    connectivityManager.unregisterNetworkCallback(this);
                }
            });
        }
    }

    /* Отсоединить приложение от активного соединения (сокета). */
    public static void unbindFromNetwork(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager.setProcessDefaultNetwork(null);
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
                        context.getApplicationContext().unregisterReceiver(this);

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
                                context.getApplicationContext().unregisterReceiver(this);
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
                WifiConfiguration wifiConfig = createWifiConfig(result, pointInfo.getSSID(), "localnvk");
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

    /* Подсоединиться к предпочтительной WiFi сети с нужным SSID. */
    public static boolean connectToSSID(Context context, String SSID) {
        WifiConfiguration configuration = createOpenWifiConfiguration(context, SSID);
        Log.d(TAG,"Priority assigned to configuration is " + configuration.priority);

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int networkId = wifiManager.addNetwork(configuration);
        Log.d(TAG,"networkId assigned while adding network is " + networkId);

        return enableNetwork(context, SSID, networkId);
    }

    private static boolean enableNetwork(Context context, String SSID, int networkId) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (networkId == -1) {
            networkId = getExistingNetworkId(context, SSID);
            Log.d(TAG, "networkId of existing network is " + networkId);

            if (networkId == -1) {
                Log.e(TAG, "Couldn't add network with SSID: " + SSID);
                return false;
            }
        }

        return wifiManager.enableNetwork(networkId, true);
    }

    /* Создать конфигурацию для присоединения к открытой WiFi сети. */
    private static WifiConfiguration createOpenWifiConfiguration(Context context, String SSID) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = WifiHelper.formatSSID(SSID);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        assignHighestPriority(context, configuration);
        return configuration;
    }

    private static void disconnect(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.disconnect();
    }

    private static int getExistingNetworkId(Context context, String SSID) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

    private static void assignHighestPriority(Context context, WifiConfiguration config) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (config.priority <= existingConfig.priority) {
                    config.priority = existingConfig.priority + 1;
                }
            }
        }
    }
}
