package odyssey.projects.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.*;

import java.net.SocketException;

import odyssey.projects.callbacks.CallbacksProvider;
import odyssey.projects.callbacks.LongOpCallback;
import odyssey.projects.db.Db;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.sav.driver.Settings;
import odyssey.projects.sav.driver.VolleyWrapper;
import odyssey.projects.utils.DebugUtils;
import odyssey.projects.utils.Noise;


import static odyssey.projects.utils.network.wifi.Wifi.connectToNetworkId;
import static odyssey.projects.utils.network.wifi.Wifi.disableWifi;
import static odyssey.projects.utils.network.wifi.Wifi.enableWifi;
import static odyssey.projects.utils.network.wifi.Wifi.isWifiEnabled;
import static odyssey.projects.utils.network.wifi.Wifi.removeWifiConfiguration;

public final class MarkOpService extends Service {

    public static final String TAG = "MARK_SERVICE";

    /* Основные маркеры ответов сервера. */
    private static final String SRV_MARKER_POSTPONE     = "postpone";
    private static final String SRV_MARKER_BLOCKED      = "blocked";
    private static final String SRV_MARKER_DETAILS      = "details";
    private static final String SRV_MARKER_ERROR        = "error";
    private static final String SRV_MARKER_STATUS       = "status";
    private static final String SRV_MARKER_DELAY        = "delay";
    private static final String SRV_MARKER_TODAY_MARKS  = "today_marks";

    /* Опеределие секунд. */
    public static final int SECONDS_1   = 1000;          // Одна секунда в миллисекундах.
    public static final int SECONDS_5   = 5*SECONDS_1;
    public static final int SECONDS_10  = 10*SECONDS_1;
    public static final int SECONDS_15  = 15*SECONDS_1;
    public static final int SECONDS_30  = 30*SECONDS_1;
    /* Опеределие минут. */
    public static final int MINUTES_1  = 60*SECONDS_1;  // Одна минута в миллисекундах.
    public static final int MINUTES_5  = 5*MINUTES_1;
    public static final int MINUTES_10 = 10*MINUTES_1;

    /* Блок основных таймаутов сервиса отметок. */
    // Таймаут в случае выключенного модуля WiFi.
    private static final int TIMEOUT_WIFI_DISABLED         = SECONDS_15;
    // Таймаут в случае отсутствия активного WiFi соединения при попытке отметится.
    private static final int TIMEOUT_WIFI_UNAVAILABLE   = SECONDS_30;
    // Таймаут в случае отсутствия сервера в сети (сбой пинга ip-адреса сервера).
    private static final int TIMEOUT_SERVER_UNREACHABLE = SECONDS_5;
    // Таймаут в случае отсутствия ответа от сервера при Volley-запросе на порт 80 на сервере.
    private static final int TIMEOUT_SERVER_80_TIMEOUT  = SECONDS_5;
    // Таймаут в случае, если сервер коррктно вернул в ответе статус ошибки выполнения скрипта.
    private static final int TIMEOUT_SERVER_OWN_FATAL   = SECONDS_30;

    /* Блок установок времени */
    // Продолжительность свечения экрана после удачной отметки.
    private static final int DURATION_SCREEN_WAKE_UP = SECONDS_10;



    private static final int MSG_MARK    = 1;
    private static final int MSG_UNBLOCK = 2;

    public static final int CMD_STOP        = -1;
    public static final int CMD_GET_STATUS  = 1;
    public static final int CMD_RUN_MARKS   = 2;

    public enum StatusEnum {
        NO_INIT, IDLE, ACTIVATED, CONNECTING, CONNECTED, FAIL, POSTPONE, STOPPED, BLOCKED
    }

    public enum WifiStatus {
        ENABLED, DISABLED, UNKNOWN
    }

    private StatusEnum Status;
    private boolean isStopRequested;
    private Handler queueHandler;
    private RequestQueue requestQueue;

    // Заблокирована ли возможность отмечаться на сервере. Это необходимо для обеспечения необходимой паузы
    // между последовательными отметками.
    private boolean markBlocked;

    private boolean useSSIDFilter;

    // Транспортное средство, отметки о котором передаются в настоящий момент.
    private String Vehicle;

    // Использование фильтра по SSID.
    private boolean useSSIDFilter;
    // Разрешенное SSID имя WiFi сети. Считывается из локальных настроек при каждом запуске менеджера.
    private String AllowedSSID;

    // Использование фильтра по BSSID.
    private boolean useBSSIDFilter;
    // Разрешенный BSSID адрес маршрутизатора. Считывается из локальных настроек при каждом запуске менеджера.
    private String AllowedBSSID;

    // Статус WiFi модуля перед запуском отметки. Если модуль был выключен, то после отметки приложение также его выключит.
    private WifiNetworkLastState wifiLastState;

    // Адрес или доменное имя удаленного сервера.
    private String ServerAddress;

    // Статус, который удаленный сервер присвоил текущей попытке клиента отметится.
    private String srvResponseStatus;

    private PowerManager.WakeLock serviceWakeLock;

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        init (this);
    }

    /* Инициализация класса */
    private void init (Context context) {
        DebugOut.generalPrintInfo(context, "Инициализация сервиса автоматического учета рейсов автотранспорта.", TAG);

        // Инициализация глобальных переменных.
        markBlocked = false;
        Status = StatusEnum.NO_INIT;
        srvResponseStatus = "unknown";

        wifiLastState = new WifiNetworkLastState(WifiStatus.UNKNOWN, -1);

        //HandlerThread queueThreadHandler = new HandlerThread("REMOTE_MARKER_SERVICE_THREAD", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        HandlerThread queueThreadHandler = new HandlerThread("REMOTE_MARKER_SERVICE_THREAD", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        queueThreadHandler.setDaemon(true);
        // Запускаем поток.
        queueThreadHandler.start();
        // Настраиваем обработчик сообщений.
        queueHandler = new Handler(queueThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MessagesHandler(msg);
            }
        };

        clrStopRequest();

        // Настраиваем очередь веб-запросов, если она не была настроена ранее.
        requestQueue = Volley.newRequestQueue(context);
        requestQueue.getCache().clear();

        // Отсылаем отчет в главное активити.
        sendStatusReport(StatusEnum.NO_INIT);

        Log.i(TAG, "MarkManager was init successfully.");
    }

    private void initForeground(){

        int NOTIFICATION_ID = 234;

        Context ctx = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "my_channel_01";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            //mChannel.setLightColor(Color.RED);
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.favicon)
                .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.favicon), 128, 128, false))
                .setContentTitle("Учет рейсов")
                .setContentText("Служба автоматических отметок запущена.");


        /*
        Intent resultIntent = new Intent(ctx, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        */

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("ru.odyssey.aura.action.main");
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        //notificationManager.notify(NOTIFICATION_ID, builder.build());
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private final class WifiNetworkLastState{
        WifiStatus status;
        int networkId;

        WifiNetworkLastState(WifiStatus status, int networkId) {this.status = status; this.networkId = networkId;}

        WifiStatus getStatus(){return status;}
        int getNetworkId()    {return networkId;}

        void setStatus(WifiStatus status) {this.status = status;}
        void setNetworkId(int networkId)     {this.networkId = networkId;}

    }

    private boolean markStarted;

    /* Основной обрботчик сообщений. */
    private void MessagesHandler(final Message msg){

        Log.i(TAG, "New message ["+msg.what+"] was arrived.");

        // Проверяем на внешний запрос остановить менеджер отметок.
        if (stopRequestedPoll()) return;

        switch (msg.what){
            //-----------------------------------------------------
            // Этот функционал в настоящий момент не используется!!! Для будущего применения!
            case MSG_UNBLOCK:
                markBlocked = false;
                break;
            //-----------------------------------------------------
            // Пришел запрос на отметку на сервере.
            case MSG_MARK:
                // Если возможность отметки заблокирована, откладываем попытку на некоторое время.
                if (markBlocked) {
                    queueHandler.sendMessageDelayed(Message.obtain(msg), MINUTES_5);
                    break;
                }

                // Отчет о статусе.
                sendStatusReport(StatusEnum.CONNECTING);
                Log.i(TAG, "Status changed to: {CONNECTING}");

                // Возможность отметки на сервере разблокирована! Пробуем отметится.
                // Сначала проверяем, подключены ли мы к сети WiFi. Для этого нам необходимо узнать контекст.
                if ((msg.obj instanceof Context)){

                    final Context context = (Context)msg.obj;

                    DebugOut.generalPrintInfo(context, "Выполняется запрос на отметку госномера "+Vehicle+".", TAG);

                    if (!markStarted){
                        markStarted = true;
                        // Действия перед попыткой отметки.
                        beforeMarkActions();
                    }

                    // Проверяется статус WiFi модуля: ВКЛЮЧЕН или ВЫКЛЮЧЕН.
                    if (isWifiEnabled(context)) {

                        // Текущая активная сеть WiFi?
                        if (WifiHelper.isWiFiNetwork(context)) {

                            Log.i(TAG, "WiFi network is active.");

                            //////////////////////////////////
                            // SSID фильтрация (имя сети).  //
                            //////////////////////////////////

                            // Получаем имя Wi-Fi сети. Очищаем его от лишних символов и знаокв ковычек.
                            final String real_SSID = WifiHelper.getWifiSSID(context);

                            // Проврека фильтра по имени WiFi сети.
                            if (useSSIDFilter) {
                                Log.i(TAG, "WiFi SSID check is enabled. Doing it ...");
                                DebugOut.generalPrintInfo(context, "Фильтр SSID активен. Проверка...", TAG);

                                // Если разрешенный не соответствует реальному, то завершаем попытку отметки.
                                if (! WifiHelper.areSsidEqual(AllowedSSID, real_SSID) ) {

                                    Log.i(TAG, "Current WiFi SSID " + real_SSID + " is not allowed by setting file!");
                                    Log.i(TAG, "Waiting for another WiFi network... Postpone " + TIMEOUT_WIFI_SSID_CHK_FAILED / SECONDS_1 + " sec. Change status to: {ACTIVATED}");
                                    DebugOut.generalPrintWarning(context, "SSID имя " + real_SSID + " текущего WiFi соединения не одобрено настройками приложения. Пауза в " + TIMEOUT_WIFI_SSID_CHK_FAILED / SECONDS_1 + " секунд.", TAG);

                                    // Проверим доступность нужной нам сети через несколько секунд.
                                    queueHandler.sendMessageDelayed(Message.obtain(msg), TIMEOUT_WIFI_SSID_CHK_FAILED);
                                    sendStatusReport(StatusEnum.ACTIVATED);

                                    // Инициируем соединение с определенной WiFi сетью для работы приложения.
                                    triggerConnectToApplicationWiFi();

                                    return;
                                }
                                {
                                    Log.i(TAG, "Ok, SSID " + real_SSID + " is allowed.");
                                    DebugOut.generalPrintInfo(context, "SSID имя " + real_SSID + " текущего WiFi соединения одобрено.", TAG);
                                }
                            }

                            ////////////////////////////////////////
                            // BSSID фильтрация (mac-адрес сети). //
                            ////////////////////////////////////////

                            // MAC-адрес WiFi сети. Здесь можно сделать ее проверку.
                            final String real_BSSID = WifiHelper.getWifiBSSID(context);

                        // Проврека WiFi SSID, если установлено в натройках.
                        if (useSSIDFilter){

                            Log.i(TAG, "WiFi SSID check is enabled. Doing it ...");

                                // Если разрешенный не соответствует реальному, то завершаем попытку отметки.
                                if (!AllowedBSSID.equalsIgnoreCase(real_BSSID)) {
                                    Log.i(TAG, "Current WiFi BSSID " + real_BSSID + " is not allowed by setting file!");
                                    Log.i(TAG, "Waiting for another WiFi router... Postpone " + TIMEOUT_WIFI_BSSID_CHK_FAILED / SECONDS_1 + " sec. Change status to: {ACTIVATED}");
                                    DebugOut.generalPrintWarning(context, "BSSID текущего WiFi роутера " + real_BSSID + " не одобрено настройками приложения. Отметка отложена на " + TIMEOUT_WIFI_BSSID_CHK_FAILED / SECONDS_1 + " секунд.", TAG);

                                    // Проверим доступность нужной нам сети через несколько секунд.
                                    queueHandler.sendMessageDelayed(Message.obtain(msg), TIMEOUT_WIFI_BSSID_CHK_FAILED);
                                    sendStatusReport(StatusEnum.ACTIVATED);

                                    // Инициируем соединение с определенной WiFi сетью для работы приложения.
                                    triggerConnectToApplicationWiFi();

                                    return;
                                } else {
                                    Log.i(TAG, "Ok, BSSID " + real_BSSID + " is allowed.");
                                    DebugOut.generalPrintInfo(context, "BSSID текущего WiFi роутера " + real_BSSID + " одобрено.", TAG);
                                }
                            }

                            ///////////////////////////////////
                            // Проверка доступности сервера. //
                            ///////////////////////////////////

                            Log.i(TAG, "Connecting to the server " + ServerAddress + " ...");
                            // Ок, сеть подключена.
                            // Теперь проверяем связь с сервером. Для начала - пингуем его.
                            if (General.isReachableByPing_wifi(getApplicationContext(), ServerAddress, true)) {

                                //////////////////////////////////////////////////////////////////////
                                // Все проверки пройдены, начинается попытка отметиться на сервере. //
                                //////////////////////////////////////////////////////////////////////

                                Log.i(TAG, "Ok, server is reachable by ping.");

                                // Отчет о статусе.
                                sendStatusReport(StatusEnum.CONNECTED);

                                final Message msg_copy = Message.obtain(msg);

                                Log.i(TAG, "Trying do mark on server with script: " + LocalSettings.getInstance(context).getScriptUrl(Settings.MARK_SCRIPT));

                                // Сервер доступен. Пытаемся выполнить отметку на сервере.
                                VolleyWrapper.doMark(context, requestQueue, null, Vehicle, new LongOpCallback() {
                                    @Override
                                    public void onSuccess(Object obj, Object param) {
                                        try {

                                            JSONObject jsonObject = ((JSONObject) obj);

                                            // Получаем статус текущего запроса на сервер.
                                            String srvResponseStatus = jsonObject.getString(SRV_MARKER_STATUS);

                                            Log.i(TAG, "Server response was received. Status: " + srvResponseStatus);

                                            // Получаем значение времени задержки перед следующей попыткой отметиться.
                                            int delay = 0;
                                            if (jsonObject.has(SRV_MARKER_DELAY))
                                                delay = jsonObject.getInt(SRV_MARKER_DELAY);

                                            // Если значение времени задержки от сервера по каки-либо причинам пришло недостоверное, то пытаемся
                                            // исправить это и установить время задержки на значение по-умолчанию.
                                            if (delay <= 0) delay = MINUTES_1;
                                            else
                                                delay = delay * MINUTES_1; // Время необходимой задержки от сервера мы получаем в секундах. Переводим его в миллисеунды.

                                            // При запросе на сервер не произошло никакой ошибки. Также сервер не приостановил
                                            // отметку данного гос. номера.
                                            if (!srvResponseStatus.equals(SRV_MARKER_ERROR) && !srvResponseStatus.equals(SRV_MARKER_BLOCKED)) {

                                                // Если запрос на отметку был отложен сервером, обновляем локальную базу данных.
                                                if (!srvResponseStatus.equals(SRV_MARKER_POSTPONE)) {
                                                    // Получаем список отметок за сегодня.
                                                    JSONArray today_marks = jsonObject.getJSONArray(SRV_MARKER_TODAY_MARKS);

                                                    // Добавляем все сегодняшние отметки, если они имеются, в локальную базу данных.
                                                    for (int ii = 0; ii < today_marks.length(); ii++) {
                                                        // Создается экземпляр класса для работы с БД.
                                                        Db db = new Db(context);
                                                        // С помощью процессора локальной БД записываем все временные метки отметок в БД.
                                                        try {
                                                            db.addMark(Vehicle, today_marks.getString(ii));
                                                        } catch (SQLiteException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    // Если набор данных изменился, то уведомляем об этом главную активити.
                                                    if (today_marks.length() > 0)
                                                        sendDatasetWasChanged();

                                                    // Отчет о статусе.
                                                    sendStatusReport(StatusEnum.IDLE);

                                                boolean useVibro = settings.getBoolean(LocalSettings.SP_USE_VIBRO);
                                                boolean useMusic = settings.getBoolean(LocalSettings.SP_USE_MUSIC);
                                                // Проигрываем мелодию и/или делаем вибро, если необходимо.
                                                if (useMusic || useVibro){
                                                    // Проигрываем небольшой звуковой фрагмент в случае успешной отметки.
                                                    if (useMusic) Noise.playSound(context);
                                                    // Делаем небольшую вибрацию в случае успешной отметки.
                                                    if (useVibro) Noise.doVibro(context);
                                                }

                                                boolean useScreenWakeUp = settings.getBoolean(LocalSettings.SP_USE_SCREEN_WAKEUP);
                                                if (useScreenWakeUp) screenWakeUp(context);

                                                Log.i(TAG, "Mark done successfully. Timeout "+delay/MINUTES_1+" min. Changed status to {IDLE}.");
                                            } else {
                                                // Отчет о статусе.
                                                sendStatusReport(StatusEnum.POSTPONE);
                                                Log.i(TAG, "Mark postponed (delay "+delay/MINUTES_1+" min). Changed status to {POSTPONE}.");
                                                DebugUtils.toastPrintInfo(context,"Отметка невозможна еще "+delay/MINUTES_1+" мин.", TAG);
                                            }

                                                // Взводим курок заново ...
                                                postponeNextMarkAttempt(msg_copy, delay, false);

                                            } else { // Ошибка на сервере или клиенту запрещено отмечаться (возможность отметок приостановлена).

                                                // Сервер вернул статус ошибки! Обрабатываем ее.
                                                if (srvResponseStatus.equals(SRV_MARKER_ERROR)) {

                                                    DebugOut.generalPrintError(context, jsonObject.getString(SRV_MARKER_DETAILS), TAG);
                                                    DebugOut.generalPrintError(context, "Сервер вернул статус ошибки!\r\nДетали:\r\n" + jsonObject.getString("details") + "\r\nПауза " + TIMEOUT_SERVER_OWN_FATAL / SECONDS_1 + " сек.", TAG);
                                                    Log.i(TAG, "Error was detected. Report 1. Status changed to: {FAIL}");
                                                    Log.i(TAG, "Error was detected. Report 2. Status changed to: {ACTIVATED}");

                                                    // Взводим курок заново ..., но ставим максимальный временной интервал на случай исправления ошибок на сервере.
                                                    postponeNextMarkAttempt(msg_copy, TIMEOUT_SERVER_OWN_FATAL, false);

                                                    // Последовательно информируем о дву статусах. Статус возникшей ошибки FAIl система визуализации
                                                    // задержит на некоторое время, а затем сменит на статус ACTIVATED.
                                                    // Отчет о статусе.
                                                    sendStatusReport(StatusEnum.FAIL);
                                                    // Отчет о статусе.
                                                    sendStatusReport(StatusEnum.ACTIVATED);
                                                }

                                                // Сервер сообщил, что отметка данного гос. номера временно приостановлена.
                                                // Останавливаем попытки отметится.
                                                if (srvResponseStatus.equals(SRV_MARKER_BLOCKED)) {
                                                    DebugOut.generalPrintWarning(context, "Возможность отметок для " + Vehicle + " заблокирована сервером на " + delay / MINUTES_1 + " мин.", TAG);
                                                    Log.i(TAG, "Warning: mark ability was disabled by the server! Status changed to: {ACTIVATED}");

                                                    // Отчет о статусе.
                                                    sendStatusReport(StatusEnum.BLOCKED);

                                                    // Взводим курок заново ...
                                                    postponeNextMarkAttempt(msg_copy, delay, false);

                                                    // Действия после НЕуспешной отметки.
                                                    postMarkActions();

                                                    return;
                                                }

                                        }
                                    } catch (JSONException e){
                                        recoverAfterFail();
                                        e.printStackTrace();
                                        DebugUtils.debugPrintException(context,e, TAG);
                                        DebugUtils.toastPrintWarning(context,"Неизвестная ошибка на сервере! Служба отметок остановлена.\r\nПодробнее:\r\n"+e.getLocalizedMessage(), TAG);
                                    }
                                }

                                    @Override
                                    public void onError(Object obj) {

                                        // Таймаут соединения с приложением сервера на порту 80. WiFi сеть активна. Сервер доступен.
                                        if ((obj instanceof NoConnectionError) || (obj instanceof TimeoutError)) {

                                            DebugOut.generalPrintWarning(context, "Сервер обнаружен, но http-служба недоступна на порту 80!\r\nПауза " + TIMEOUT_SERVER_80_TIMEOUT / SECONDS_1 + " сек.", TAG);
                                            Log.i(TAG, "Warning: no activity on server port 80! Postpone " + TIMEOUT_SERVER_80_TIMEOUT / SECONDS_1 + " sec. Change status to: {ACTIVATED}");

                                            // Удаляем из очереди все имеющиеся сообщения, если таковые имеются. Это деалется потому, что при создании запроса
                                            // библеотека Volley делает несколько попыток соединиться и этот callback может быть вызван несколько раз через
                                            // определенный таймаут. Чобы исклчить из очереди несколько MSG_MARK сообщений, очищем предыдущие имеющиеся в очереди.
                                            postponeNextMarkAttempt(msg_copy, TIMEOUT_SERVER_80_TIMEOUT, true);

                                            sendStatusReport(StatusEnum.ACTIVATED);
                                            return;
                                        }
                                        if (obj instanceof ServerError) {
                                            DebugOut.generalPrintError(context, "На сервере возникли неполадки! Обратитесь к администратору.\r\nПауза "+TIMEOUT_SERVER_GENERAL_ERROR / SECONDS_1+" сек.\r\nПодробнее:\r\n" + obj.toString(), TAG);
                                            Log.i(TAG, "Warning: errors on the server was occurred! Change status to: {ACTIVATED}");

                                            postponeNextMarkAttempt(msg_copy, TIMEOUT_SERVER_GENERAL_ERROR, true);

                                            sendStatusReport(StatusEnum.ACTIVATED);
                                            return;
                                        }
                                        ///////////////////////////////
                                        // Прочие ошибки соединения. //
                                        ///////////////////////////////
                                        DebugOut.debugPrintVolleyError(context, obj, TAG);
                                        DebugOut.generalPrintError(context, "Неизвестная ошибка коммуникации с сервером! Подробности:\r\n" + ((obj != null) ? obj.toString() : "unknown"), TAG);

                                        // Взводим курок заново ...
                                        postponeNextMarkAttempt(msg_copy, TIMEOUT_UNKNOWN_SERVER_ERROR, true);

                                        return;
                                    }
                                });
                            } else { // Сервер не доступен!

                                DebugOut.generalPrintWarning(context, "Сервер "+ServerAddress+" в сети не обнаружен!\r\nПауза " + TIMEOUT_SERVER_UNREACHABLE / SECONDS_1 + " сек.", TAG);
                                Log.i(TAG, "Warning: server is unreachable! Postpone " + TIMEOUT_SERVER_UNREACHABLE / SECONDS_1 + " sec. Change status to: {ACTIVATED}");

                                // Проверим доступность сети через несколько секунд.
                                postponeNextMarkAttempt(msg, TIMEOUT_SERVER_UNREACHABLE, false);

                                sendStatusReport(StatusEnum.ACTIVATED);
                            }
                        } else { /* Нет активных WiFi подключений! */
                            DebugOut.generalPrintWarning(context, "Активная WiFi сеть не обнаружена.\r\nПауза " + TIMEOUT_WIFI_UNAVAILABLE / SECONDS_1 + " сек.", TAG);
                            Log.i(TAG, "Warning: WiFi network is not active! Postpone " + TIMEOUT_WIFI_UNAVAILABLE / SECONDS_1 + " sec. Change status to: {ACTIVATED}");

                            // Проверим доступность сети через несколько секунд.
                            postponeNextMarkAttempt(msg, TIMEOUT_WIFI_UNAVAILABLE, false);

                            // Инициируем соединение с определенной WiFi сетью для работы приложения.
                            triggerConnectToApplicationWiFi();

                            sendStatusReport(StatusEnum.ACTIVATED);
                        }
                    } else { /* Модуль WiFi отключен! */

                        DebugOut.generalPrintWarning(context, "В данный момент модуль WiFi отключен. Включаем модуль ...\r\nПауза " + TIMEOUT_WIFI_DISABLED / SECONDS_1 + " сек.", TAG);
                        Log.i(TAG, "Warning: WiFi is disabled! Try do ReEnabled it. Postpone " + TIMEOUT_WIFI_DISABLED / SECONDS_1 + " sec. Change status to: {ACTIVATED}");

                        // Если потребность приложения в WiFi подключени к предопределенной WiFi сети
                        // не прерывает текущее WiFi подключение пользователя, то по окончанию отметки выключаем
                        // модуль WiFi.
                        //wifiLastState = new WifiNetworkLastState(WifiStatus.DISABLED, -1);

                        // Проверим статус модуля WiFi через некоторое время.
                        queueHandler.sendMessageDelayed(Message.obtain(msg), TIMEOUT_WIFI_DISABLED);

                        // Инициируем соединение с определенной WiFi сетью для работы приложения.
                        triggerConnectToApplicationWiFi();

                        sendStatusReport(StatusEnum.ACTIVATED);
                    }
                }
                break;
        }
    }

    // Вызывается перед началом отметки.
    private void beforeMarkActions() {
        // Сохраняем состояние модуля.
        backupWifiModuleState();
    }

    // Вызывается после успешной отметки.
    private void postMarkActions() {
        // Возвращаем WiFi модуль в исходное состояние (до начала очередной отметки).
        restoreWifiModuleState();
    }

    // Откладывает очередную последующую попытку отметки на указанное время.
    private void postponeNextMarkAttempt(final   Message msg,
                                         int     timeout,
                                         boolean clearQueue){

        // Чистится очередь сообщений, если это необходимо.
        if (clearQueue) queueHandler.removeCallbacksAndMessages(null);
        // Делается копия сообщения и его обработка откладывается на указанное время.
        queueHandler.sendMessageDelayed(Message.obtain(msg), timeout);
    }

    // Сохранить текущее состояние модуля WiFi.
    private void backupWifiModuleState(){

        DebugOut.generalPrintInfo(getApplicationContext(), "Сохраняется текущее состояние WiFi модуля ...", TAG);
        if (isWifiEnabled(getApplicationContext())){

            // Если текущее активное соединение - это соединение с необходимой нам сетью,
            // то запланируем отключение от него по окончанию отметки.
            if (isActiveWifiConnectionSuitable()) {
                DebugOut.generalPrintInfo(getApplicationContext(), "Текущее состояние WiFi сохранено как:\r\n\"Подключен к рабочей сети\".", TAG);
                wifiLastState.setStatus(WifiStatus.DISABLED);
                return;
            }
            // В противном случае - это соединение с какой-то другой сетью, которая была активирована
            // пользователем. Сохраняем это соединение для восстановления его после отметки.
            DebugOut.generalPrintInfo(getApplicationContext(), "Текущее состояние WiFi сохранено как:\r\n\"Подключен к сети пользователя\".", TAG);
            wifiLastState.setStatus(WifiStatus.ENABLED);
            try {
                wifiLastState.setNetworkId(WifiHelper.getActiveWifiNetworkId(getApplicationContext()));
            } catch (Exception e) {
                e.printStackTrace();
                wifiLastState.setNetworkId(-1);
            }
        } else {
            DebugOut.generalPrintInfo(getApplicationContext(), "Текущее состояние WiFi сохранено как:\r\n\"ОТКЛЮЧЕН\".", TAG);
            wifiLastState.setStatus(WifiStatus.DISABLED);
            wifiLastState.setNetworkId(-1);
        }
    }

    // Восстановить исходное состояние модуля WiFi и последнего его подключения, которо было до начала отметки приложением.
    private void restoreWifiModuleState(){
        if (wifiLastState.getStatus() != WifiStatus.UNKNOWN) {
            DebugOut.generalPrintInfo(getApplicationContext(), "Восстанавливается состояние WiFi модуля.", TAG);
            if (wifiLastState.getStatus() == WifiStatus.DISABLED) {
                disableWifi(getApplicationContext());
                DebugOut.generalPrintInfo(getApplicationContext(), "Модуль WiFi преводится в исходное состояние - ВЫКЛЮЧЕН.", TAG);
            } else {
                String extra = "";
                if (wifiLastState.getNetworkId() > 0)
                    extra = "Автоматическое подключение к сети:\r\n" + wifiLastState.getNetworkId();

                DebugOut.generalPrintInfo(getApplicationContext(), "Модуль WiFi преводится в исходное состояние - ВКЛЮЧЕН.\r\n" + extra, TAG);
                enableWifi(getApplicationContext());
                connectToNetworkId(getApplicationContext(), wifiLastState.getNetworkId());
            }
            wifiLastState.setStatus(WifiStatus.UNKNOWN);
            wifiLastState.setNetworkId(-1);
        }
    }

    private void triggerConnectToApplicationWiFi(){

        String WifiSSID = null;
        if (SettingsCache.USE_SSID_FILTER) WifiSSID = SettingsCache.ALLOWED_WIFI_SSID;

        String WifiBSSID = null;
        if (SettingsCache.USE_BSSID_FILTER) WifiBSSID = SettingsCache.ALLOWED_WIFI_BSSID;

        String extraMessage = "";
        if (WifiSSID  != null) extraMessage += "\r\nSSID: ["  + WifiSSID + "]";
        if (WifiBSSID != null) extraMessage += "\r\nBSSID: [" + WifiBSSID + "]";
        if (WifiSSID == null && WifiBSSID == null) extraMessage = "\r\n - любая WiFi сеть.";

        DebugOut.generalPrintInfo(getApplicationContext(), "Запрос на соединение с предопределенной WiFi сетью:"+extraMessage+"\r\nВыполняется подключение ...", TAG);

        // Запускаем слушателя, настроенного на сообщение об успешном подключении модуля WiFi к искомой сети.
        reloadApplicationWifiConnectedListener();

        // Включаем модуль WiFi и подключаемся к предопределенной WiFi сети.
        Wifi.connectToWifi(getApplicationContext(), WifiSSID, WifiBSSID);
    }

    private BroadcastReceiver wifiConnectedReceiver = null;
    // Регистрируется приемник широковещательных сообщений о подключении к нужной WiFi-сети.
    private void reloadApplicationWifiConnectedListener(){

        IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        // Удаляем предыдущий приемник, если таковой имеется.
        if (wifiConnectedReceiver != null){
            getApplicationContext().unregisterReceiver(wifiConnectedReceiver);
            wifiConnectedReceiver = null;
        }

        DebugOut.generalPrintInfo(getApplicationContext(), "Запущен слушатель нового WiFi подключения.", TAG);

        // Настраиваем приемник заново.
        getApplicationContext().registerReceiver(wifiConnectedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.i(TAG, "Registered WiFi connectivity changes ...");

                final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (wifiManager.isWifiEnabled()) {

                    if (isActiveWifiConnectionSuitable()) {
                        DebugOut.generalPrintInfo(getApplicationContext(), "Модуль WiFi включен.", TAG);
                        DebugOut.generalPrintInfo(getApplicationContext(), "Искомое подключение найдено:\r\nSSID: " + SettingsCache.ALLOWED_WIFI_SSID + "\r\nBSSID: " + SettingsCache.ALLOWED_WIFI_BSSID, TAG);
                        Log.i(TAG, "Ok, searching connection has been found.\r\nSSID: " + SettingsCache.ALLOWED_WIFI_SSID + "\r\nBSSID: " + SettingsCache.ALLOWED_WIFI_BSSID);

                        // Ускорить отложенное задание на отметку.
                        boostMarkTask(getApplicationContext(), false);
                        // Отменяем слушателя.
                        getApplicationContext().unregisterReceiver(this);
                        wifiConnectedReceiver = null;
                    } else {
                        Log.i(TAG, "Current WiFi connection is not allowed for the application requirements!");
                        String extra = "";
                        String ssid = wifiManager.getConnectionInfo().getSSID();
                        if (ssid != null) extra = StrHelper.trimAll(ssid);
                        DebugOut.generalPrintWarning(getApplicationContext(), "Обнаружено новое WiFi подключение:\r\nSSID [" + extra + "]\r\n, но оно не соответствует настройкам безопасности приложения.", TAG);
                    }
                } else {
                    DebugOut.generalPrintInfo(getApplicationContext(), "Произошло отключение WiFi.", TAG);
                }
            }
        }, filters);
    }

    private boolean isActiveWifiConnectionSuitable(){

        ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // Проверка условий.
        if (networkInfo == null || networkInfo.getExtraInfo() == null) return false;
        // Если WiFi соединение не установлено, завершаем работу.
        if ( !networkInfo.isConnected() ) return false;

        Log.i(TAG, "Current active WiFi network connection is:\r\nSSID: "+networkInfo.getExtraInfo()+"\r\nBSSID: "+ WifiHelper.getWifiBSSID(getApplicationContext()));

        boolean isAllowedSSID  = true;
        boolean isAllowedBSSID = true;

        // Если требуется фильтрация по SSID имени, то делаем это.
        if (SettingsCache.USE_SSID_FILTER){
            isAllowedSSID = WifiHelper.areSsidEqual(networkInfo.getExtraInfo(), SettingsCache.ALLOWED_WIFI_SSID);
        }

        // Если требуется фильтрация по BSSID роутера, то делаем это.
        if (SettingsCache.USE_BSSID_FILTER){
            String BSSID = WifiHelper.getWifiBSSID(getApplicationContext());
            if (BSSID != null) {
                isAllowedBSSID = BSSID.equalsIgnoreCase(SettingsCache.ALLOWED_WIFI_BSSID);
            }
        }

        return isAllowedSSID && isAllowedBSSID;
    }

    // Сообщить все подписчикам об изменении набора данных отметок.
    private void sendDatasetWasChanged(){
        if (CallbacksProvider.getMarksDatasetCallback() != null){
            CallbacksProvider.getMarksDatasetCallback().changed(true);
        }
    }

    // Отослать отчет о статус в главное активити.
    private void sendStatusReport(StatusEnum status){
        Status = status;
        // Получаем обработчик событий и сообщений в главном активити.
        if (CallbacksProvider.getMarkStatusCallback() != null){
            CallbacksProvider.getMarkStatusCallback().changed(status);
        }
    }

    // Запрос на остановку менеджера отметок от внешнего модуля.
    // Например, активити настроек может запросить остановить менеджер после изменения важных настроек.
    public void setStopRequest (){
        isStopRequested = true;
        stopRequestedPoll();
    }

    private void clrStopRequest(){
        isStopRequested = false;
    }

    private boolean stopRequestedPoll(){
        if (isStopRequested){

            isStopRequested = false;

            markStarted = false;

            Log.i(TAG, "Stop request was detected! Status changed to {STOPPED}.");
            DebugOut.generalPrintInfo(this, "Зарегистрирован запрос на очистку очереди отметок.\r\nОчередь очищена.", TAG);

            ///////////////////////////////////////////////////////////////////////////////////
            // Уничтожаем основной поток отметок.
            /*
            if (queueThreadHandler != null) {
                Thread dummy = queueThreadHandler;
                queueThreadHandler = null;
                dummy.interrupt();
            }
            */
            ///////////////////////////////////////////////////////////////////////////////////

            // Удаляем из очереди все имеющиеся сообщения, если таковые имеются.
            queueHandler.removeCallbacksAndMessages(null);
            // Возвращаем модуль WiFi в исходное состояние.
            restoreWifiModuleState();
            // Отчет о статусе.
            sendStatusReport(StatusEnum.STOPPED);
            // Освобождаем ресурсы процессора по обработке потока отметок.
            serviceWakeUnlock();
            Log.i(TAG, "Stop request was detected! Status changed to {STOPPED}.");
            return true;
        }
        return false;
    }

    // Дает возможность сервису работать даже после отключения дисплея (блокировки экрана).
    private void serviceWakeLock(Context context){
        PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        serviceWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sav:mark_service_unlock");
        if (serviceWakeLock != null) {
            serviceWakeLock.acquire();
            Log.i(TAG, "Service Waked Lock.");
        }
    }

    // Отключить возможность сервиса выполняться во вемя блокировки экрана.
    private void serviceWakeUnlock(){
        if (serviceWakeLock != null) {
            serviceWakeLock.release();
            Log.i(TAG, "Service Waked UnLock.");
        }
    }

    // Зажечь экран на предустановленное время.
    private void screenWakeUp(Context context){
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if(!isScreenOn)
        {
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"sav:screen_wakeup");
            wl.acquire(DURATION_SCREEN_WAKE_UP);

            PowerManager.WakeLock wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"sav:service_cpu");
            wl_cpu.acquire(DURATION_SCREEN_WAKE_UP);
        }
    }

    /* Запустить поток для автоматического выполнения отметок в бесконечном цикле. */
    public boolean reRun(Context context){

        markStarted = false;

        clrStopRequest();

        // Проверяем флаг глобального разрешения/запрещения работы приложения
        Boolean globEnable = settings.getBoolean(LocalSettings.SP_GLOBAL_ENABLE);
        if (!globEnable){
            DebugUtils.toastPrintError(context,"Пожалуйста, включите возможность использования программы.", TAG);
            sendStatusReport(StatusEnum.STOPPED);
            return false;
        }

        // Гос. номер текущего транспортного средства берем из локальных настроек.
        Vehicle = settings.getText(LocalSettings.SP_VEHICLE);

        if ((Vehicle == null) || Vehicle.equals("")) {
            DebugUtils.toastPrintError(context,"Пожалуйста, укажите гос. номер ТС.", TAG);
            sendStatusReport(StatusEnum.STOPPED);
            return false;
        }

        // Требуется ли фильтр по имени WiFi сети.
        useSSIDFilter = SettingsCache.USE_SSID_FILTER;
        // Из локальных настроек считываем разрешенное имя SSID WiFi сети.
        AllowedSSID = SettingsCache.ALLOWED_WIFI_SSID;

        // Требуется ли фильтр по MAC-адресу.
        useSSIDFilter = settings.getBoolean(LocalSettings.SP_USE_SSID_FILTER);
        // Из локальных настроек считываем разрешенный SSID маршрутизатора.
        AllowedSSID = settings.getText(LocalSettings.SP_ALLOWED_WIFI_SSID);
        // Считываем адрес сервера.
        ServerAddress = settings.getText(LocalSettings.SP_SERVER_ADDRESS);

        if (ServerAddress == null || ServerAddress.equals("")){
            DebugUtils.toastPrintError(context,"Пожалуйста, укажите адрес удаленного сервера в настройках вашего приложения!", TAG);
            sendStatusReport(StatusEnum.STOPPED);
            return false;
        }

        // Обнуляем значение последнего состояния WiFi модуля перед. По-умолчанию - состояние НЕ ИЗВЕСТНО.
        wifiLastState.setStatus(WifiStatus.UNKNOWN);
        wifiLastState.setNetworkId(-1);

        // Если это первый запуск отметок и первое подключения к WiFi этого
        // релиза приложения соответственно, то пытаемся очистить настройки WiFi соединения,
        // сделанные предыдущими версиями этого приложения (если таковые есть).
        if (!SettingsCache.WIFI_AUTO_CONFIGURED){
            // Вне зависимости от результатов автоконфигурирования/сброса настроек
            // более не пытаемся повторить сброс заново. Это можно сделать вручную в инженерном меню.
            LocalSettings.getInstance(this).setSpWifiAutoConfigured();
            SettingsCache.WIFI_AUTO_CONFIGURED = true;
            // Удаляем преднастроенное ранее подключение, если таковое имеется.
            removeWifiConfiguration(context, SettingsCache.ALLOWED_WIFI_SSID);
        }

        // Отсылаем отчет в главное активити.
        sendStatusReport(StatusEnum.ACTIVATED);
        Log.i(TAG, "Run/rerun triggered. Status changed to: {ACTIVATED}");
        return true;
    }

    /* Ускорить отложенное задание на отметку, ожидающую благоприятных условий (например, подключенной сети WiFi).
     * Если указан параметр {inAnyCase = true}, то запуск задания произайдет без дополнительных проверок. */
    private void boostMarkTask(Context context, boolean inAnyCase){

        // Если условия для отметки изменились и в очереди сообщений имеются отложенные задания на отметку, пытаемся
        // форсировать их обработку путем удаления старых и создания новых заданий для выполнения их без задержки.
        if (    inAnyCase ||
                (queueHandler.hasMessages(MSG_MARK) &&                       // В очереди сообщений есть отложенный запрос на отметку.
                !srvResponseStatus.equalsIgnoreCase(SRV_MARKER_POSTPONE) &&  // Это сообщение не было отложено по причине преждевременной попытки отметится.
                !srvResponseStatus.equalsIgnoreCase(SRV_MARKER_BLOCKED))){   // Это сообщение не было отложено по причине блокировки клиента.

            // Удаляем из очереди все имеющиеся сообщения, если таковые имеются.
            queueHandler.removeCallbacksAndMessages(null);
            // Запускаем обработчик запросов (периодические запросы на отметку на сервере).
            queueHandler.sendMessage(Message.obtain(queueHandler, MSG_MARK, context));

            if (inAnyCase)DebugOut.generalPrintInfo(context, "Триггер отметки запущен.", TAG);
            else
                DebugOut.generalPrintInfo(context, "Форсирование текущей отметки.", TAG);
        } else {
            String extra = "";
            if (!queueHandler.hasMessages(MSG_MARK)) extra = "\r\n- в очереди сообщений нет отложенного запроса на отметку;";
            if (srvResponseStatus.equalsIgnoreCase(SRV_MARKER_POSTPONE)) extra += "\r\n- задание было отложено по причине преждевременной попытки отметится;";
            if (srvResponseStatus.equalsIgnoreCase(SRV_MARKER_BLOCKED))  extra += "\r\n- задание было отложено по причине блокировки клиента;";

            DebugOut.generalPrintWarning(context, "Форсирование отметки в настоящий момент невозможно! Причина(-ы):"+extra, TAG);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Если интент не был передан, уведомляем главное активити о текущем статусе сервиса.
        if (intent == null) sendStatusReport(Status);
        else {
            switch (intent.getIntExtra(Settings.ACTION_TYPE_CMD, 0)){
                //-----------------------------------------------------
                // Получение команды: сообщит текущий статус сервиса отметок.
                case MarkOpService.CMD_GET_STATUS:
                    sendStatusReport(Status);
                    break;
                //-----------------------------------------------------
                // Получение команды: запуск сервиса автоматических отметок на сервере.
                case MarkOpService.CMD_RUN_MARKS:
                    // Если нет препядствий для запуска сервиса, выводится уведомление.
                    if (reRun(this)){
                        // Этим уведомлением перемещаем сервис на уровень foreground.
                        initForeground();
                    }
                    break;
                // -----------------------------------------------------
                // Получение команды: остановить сервис.
                case MarkOpService.CMD_STOP:
                    stop();
                    break;
                //-----------------------------------------------------
                default:break;
            }
        }
        // Описывает стандартное поведение. Похоже на то, как был реализован метод onStart() в Android 2.0.
        // Если вы вернете это значение, обработчик onStartCommand() будет вызываться при повторном запуске
        // сервиса после преждевременного завершения работы. Обратите внимание, что аргумент Intent,
        // передаваемый в onStartCommand(), получит значение null. Данный режим обычно используется для
        // сервисов, которые сами обрабатывают свои состояния, явно стартуя и завершая свою работу при
        // необходимости (с помощью методов startService() и stopService()). Это относится к сервисам, которые
        // проигрывают музыку или выполняют другие задачи в фоновом режиме.
        //return START_STICKY;

        // Этот режим используется в сервисах, которые запускаются для выполнения конкретных действий или команд.
        // Как правило, такие сервисы используют stopSelf() для прекращения работы, как только команда выполнена.
        // После преждевременного прекращения работы сервисы, работающие в данном режиме, повторно запускаются только
        // в том случае, если получат вызовы. Если с момента завершения работы Сервиса не был запущен метод startService(),
        // он остановится без вызова обработчика onStartCommand(). Данный режим идеально подходит для сервисов, которые
        // обрабатывают конкретные запросы, особенно это касается регулярного выполнения заданных действий (например,
        // обновления или сетевые запросы). Вместо того, чтобы перезапускать сервис при нехватке ресурсов, часто более
        // целесообразно позволить ему остановиться и повторить попытку запуска по прошествии запланированного интервала.
        //return START_NOT_STICKY ;

        // Если система преждевременно завершила работу сервиса, он запустится повторно, но только
        // когда будет сделан явный запрос на запуск или если процесс завершился до вызова метода
        // stopSelf(). В последнем случае вызовется обработчик onStartCommand(), он получит первоначальное
        // намерение, обработка которого не завершилась должным образом.
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugOut.generalPrintWarning(getApplicationContext(), "Сервис автоматической отметки автотранспорта остановлен!", TAG);
    }

    public void stop(){
        setStopRequest();
        Log.i(TAG, "Stop trigger was requested ...");
    }
}
