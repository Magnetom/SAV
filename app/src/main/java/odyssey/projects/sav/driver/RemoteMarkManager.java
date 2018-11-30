package odyssey.projects.sav.driver;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import odyssey.projects.db.DbProcessor;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.utils.DebugUtils;

import static odyssey.projects.utils.WebUtils.*;

//final Messenger inMessenger = new Messenger(new IncomingHandler());
public final class RemoteMarkManager {

    public static final String TAG = "MARK_MANAGER";

    public static final int SECONDS_1   = 1000;          // Одна секунда в миллисекундах.
    public static final int SECONDS_10  = 10*SECONDS_1;
    public static final int SECONDS_30  = 30*SECONDS_1;


    public static final int MINUTES_1  = 60*SECONDS_1;  // Одна минута в миллисекундах.
    public static final int MINUTES_5  = 5*MINUTES_1;
    public static final int MINUTES_10 = 10*MINUTES_1;

    private static final int MSG_MARK    = 1;
    private static final int MSG_UNBLOCK = 2;

    public enum StatusEnum {
        NO_INIT, IDLE, ACTIVATED, CONNECTING, CONNECTED, FAIL, POSTPONE, STOPPED, BLOCKED
    }

    private static StatusEnum Status = StatusEnum.NO_INIT;

    private static HandlerThread queueThreadHandler = null;
    private static Handler queueHandler = null;
    private static RequestQueue requestQueue = null;

    private static LocalSettings settings = null;

    // Класс для рботы с локальной БД и ее отображением в визуальные компоненты.
    private static DbProcessor localDbProc;

    // Заблокирована ли возможность отмечаться на сервере. Это необходимо для обеспечения необходимой паузы
    // между последовательными отметками.
    private static boolean markBlocked = false;

    // Транспортное средство, отметки о котором передаются в настоящий момент.
    private static String vehicle = null;

    private static Handler statusHandler;
    private static Handler generalHandler;

    private static void init () {
        markBlocked = false;

        queueThreadHandler = new HandlerThread("REMOTE_MARKER_THREAD", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        // Запускаем поток.
        queueThreadHandler.start();
        // Настраиваем обработчик сообщений.
        queueHandler = new Handler(queueThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MessagesHandler(msg);
            }
        };
    }

    /* Основной обрботчик сообщений. */
    private static void MessagesHandler(final Message msg){

        Log.i(TAG, "New message ["+msg.what+"] was arrived.");

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

                     // Текущая активная сеть WiFi?
                     if (isWiFiNetwork(context)){
                         Log.i(TAG, "WiFi network is active.");

                         // MAC-адрес WiFi сети. Здесь можно сделать ее проверку.
                         String ssid = getWifiBSSID(context);

                         // ToDo: проврека WiFi SSID.

                         // Ок, сеть подключена.
                         // Теперь проверяем связь с сервером. Для начала - пингуем его.
                         if (isReachableByPing(Settings.DB_SERVER_IP)){
                             Log.i(TAG, "Server "+Settings.DB_SERVER_IP+" is reachable by ping.");

                             // Отчет о статусе.
                             sendStatusReport(StatusEnum.CONNECTED);

                             final Message msg_copy = Message.obtain(msg);

                             Log.i(TAG, "Trying do mark on server ...");

                             // Сервер доступен. Пытаемся выполнить отметку на сервере.
                             VolleyWrapper.doMark(context, requestQueue, null,vehicle, new LongOpCallback() {
                                 @Override
                                 public void onSuccess(Object obj, Object param) {
                                     try {

                                         JSONObject jsonObject = ((JSONObject) obj);

                                         // Получаем статус текущего запроса на сервер.
                                         String status = jsonObject.getString("status");

                                         Log.i(TAG, "Server response was received. Status: "+status);

                                         // Получаем значение времени задержки перед следующей попыткой отметиться.
                                         int delay = 0;
                                         if (jsonObject.has("delay")) delay = jsonObject.getInt("delay");

                                         // Если значение времени задержки от сервера по каки-либо причинам пришло недостоверное, то пытаемся
                                         // исправить это и установить время задержки на значение по-умолчанию.
                                         if (delay <= 0 ) delay = MINUTES_1;
                                         else
                                             delay = delay * MINUTES_1; // Время необходимой задержки от сервера мы получаем в секундах. Переводим его в миллисеунды.

                                         // При запросе на сервер не произошло никакой ошибки. Также сервер не приостановил
                                         // отметку данного гос. номера.
                                         if (!status.equals("error") && !status.equals("blocked")){

                                             // Если запрос на отметку был отложен сервером, обновляем локальную базу данных.
                                             if (!status.equals("postpone")){
                                                 // Получаем список отметок за сегодня.
                                                 JSONArray today_marks  = jsonObject.getJSONArray("today_marks");

                                                 // Добавляем все сегодняшние отметки, если они имеются, в локальную базу данных.
                                                 for (int ii=0; ii<today_marks.length();ii++){
                                                     // С помощью процессора локальной БД записываем все временные метки отметок в БД.
                                                     localDbProc.addMark(vehicle, today_marks.getString(ii));
                                                 }

                                                 // Отчет о статусе.
                                                 sendStatusReport(StatusEnum.IDLE);
                                                 Log.i(TAG, "Mark done successfully. Timeout "+delay * MINUTES_1+" min. Changed status to {IDLE}.");
                                             } else {
                                                 // Отчет о статусе.
                                                 sendStatusReport(StatusEnum.POSTPONE);
                                                 Log.i(TAG, "Mark postponed (delay "+delay * MINUTES_1+" min). Changed status to {POSTPONE}.");
                                             }

                                             // Взводим курок заново ...
                                             queueHandler.sendMessageDelayed(Message.obtain(msg_copy), delay);

                                         } else { // Ошибка на сервере или клиенту запрещено отмечаться (возможность отметок приостановлена).

                                             // Сервер вернул статус ошибки! Обрабатываем ее.
                                             if (status.equals("error")){

                                                 //DebugUtils.debugPrintErrorStd1(context, TAG);
                                                 DebugUtils.debugPrintError(context, jsonObject.getString("details"), TAG);

                                                 // Взводим курок заново ..., но ставим максимальный временной интервал на случай исправления ошибок на сервере.
                                                 queueHandler.sendMessageDelayed(Message.obtain(msg_copy), MINUTES_10);

                                                 // Последовательно информируем о дву статусах. Статус возникшей ошибки FAIl система визуализации
                                                 // задержит на некоторое время, а затем сменит на статус ACTIVATED.
                                                 // Отчет о статусе.
                                                 sendStatusReport(StatusEnum.FAIL);
                                                 // Отчет о статусе.
                                                 sendStatusReport(StatusEnum.ACTIVATED);
                                                 Log.i(TAG, "Error was detected. Report 1. Status changed to: {FAIL}");
                                                 Log.i(TAG, "Error was detected. Report 2. Status changed to: {ACTIVATED}");
                                             }

                                             // Сервер сообщил, что отметка данного гос. номера временно приостановлена.
                                             // Останавливаем попытки отметится.
                                             if (status.equals("blocked")) {
                                                 // Отчет о статусе.
                                                 sendStatusReport(StatusEnum.BLOCKED);

                                                 // Взводим курок заново ...
                                                 queueHandler.sendMessageDelayed(Message.obtain(msg_copy), delay);

                                                 Log.i(TAG, "Warning: mark ability was disabled by the server! Status changed to: {ACTIVATED}");
                                                 return;
                                             }

                                         }

                                     } catch (JSONException e){
                                         recoverAfterFail();
                                         e.printStackTrace();
                                         DebugUtils.debugPrintException(context,e, TAG);
                                     }
                                 }

                                 @Override
                                 public void onError(Object obj) {
                                     DebugUtils.debugPrintVolleyError(context, obj, TAG);
                                     recoverAfterFail();
                                 }
                             });
                         } else{ // Сервер не доступен!
                             // Проверим доступность сети через несколько секунд.
                             queueHandler.sendMessageDelayed(Message.obtain(msg), SECONDS_30);
                             sendStatusReport(StatusEnum.ACTIVATED);
                             Log.i(TAG, "Warning: server is unreachable! Postpone 30 sec. Change status to: {ACTIVATED}");
                         }
                    } else { // Сеть WiFi не активна!
                         // Проверим доступность сети через несколько секунд.
                         queueHandler.sendMessageDelayed(Message.obtain(msg), SECONDS_10);
                         sendStatusReport(StatusEnum.ACTIVATED);
                         Log.i(TAG, "Warning: WiFi network is not active! Postpone 10 sec. Change status to: {ACTIVATED}");
                     }
                }
                break;
        }
    }

    private static void recoverAfterFail(){
        /**/
        sendStatusReport(StatusEnum.FAIL);
        sendStatusReport(StatusEnum.STOPPED);
        Log.i(TAG, "Recover after fail. Report 1. Status changed to: {FAIL}");
        Log.i(TAG, "Recover after fail. Report 2. Status changed to: {STOPPED}");
    }

    // Отослать отчет о статус в главное активити.
    private static void sendStatusReport(StatusEnum status){
        // Получаем обработчик событий и сообщений в главном активити.
        if (statusHandler != null){
            statusHandler.sendMessage(Message.obtain(statusHandler, MainActivity.MSG_ST_CHANGE_STATUS, status));
        }
    }

    public static void init(Context context, Handler h1, Handler h2){
        // Обработчик сообщений о статусе в главной активити.
        statusHandler = h1;
        // Основной обработчик сообщений в главной панели.
        generalHandler = h2;

        // Отсылаем отчет в главное активити.
        sendStatusReport(StatusEnum.NO_INIT);

        // Основная инициалзация статических элементов.
        init();

        // Инициализируем менеджер по работе с базой данных и визуальными компонентами, отражающими состояние базы данных.
        localDbProc = DbProcessor.getInstance(context, generalHandler);

        // Настраиваем очередь веб-запросов, если она не была настроена ранее.
        requestQueue = Volley.newRequestQueue(context);
        requestQueue.getCache().clear();

        settings = LocalSettings.getInstance(context);

        Log.i(TAG, "MarkManager was init successfully.");
    }

    public static Boolean reRun(Context context){
        // Гос. номер текущего транспортного средства берем из локальных настроек.
        vehicle = settings.getText(LocalSettings.SP_VEHICLE);
        //if (vehicle.isEmpty()) vehicle = "A888MP42RUS"; // ТЕСТОВАЯ ЗАГЛУШКА!!!!!!!!!

        if ((vehicle == null) || vehicle.equals("")) {
            DebugUtils.debugPrintError(context,"Пожалуйста, укажите гос. номер ТС.", TAG);
            return false;
        }
        // Удаляем из очереди все имеющиеся сообщения, если таковые имеются.
        queueHandler.removeCallbacksAndMessages(null);
        // Запускаем обработчик запросов (периодические запросы на отметку на сервере).
        queueHandler.sendMessage(Message.obtain(queueHandler, MSG_MARK, context));

        // Отсылаем отчет в главное активити.
        sendStatusReport(StatusEnum.STOPPED);
        Log.i(TAG, "Run/rerun triggered. Status changed to: {ACTIVATED}");
        return true;
    }

    public static void stop(){
        // Удаляем из очереди все имеющиеся сообщения, если таковые имеются.
        queueHandler.removeCallbacksAndMessages(null);
        sendStatusReport(StatusEnum.STOPPED);
        Log.i(TAG, "Stop triggered. Status changed to: {STOPPED}");
    }

    public static void onDestroy(){
        if (requestQueue != null)       requestQueue.stop();
        if (queueThreadHandler != null) queueThreadHandler.quitSafely();
        if (settings != null)           settings = null;

        // Остановка компонента работы с локальной базой данных.
        localDbProc.OnDestroy();
    }

}
