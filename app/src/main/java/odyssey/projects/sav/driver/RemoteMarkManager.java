package odyssey.projects.sav.driver;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

                // Возможность отметки на сервере разблокирована! Пробуем отметится.
                // Сначала проверяем, подключены ли мы к сети WiFi. Для этого нам необходимо узнать контекст.
                if ((msg.obj instanceof Context)){

                    final Context context = (Context)msg.obj;
                     // Текущая активная сеть WiFi?
                     if (isWiFiNetwork(context)){
                         // MAC-адрес WiFi сети. Здесь можно сделать ее проверку.
                         String ssid = getWifiBSSID(context);

                         // ToDo: проврека WiFi SSID.

                         // Ок, сеть подключена.
                         // Теперь проверяем связь с сервером. Для начала - пингуем его.
                         if (isReachableByPing(Settings.DB_SERVER_IP)){

                             final Message msg_copy = Message.obtain(msg);

                             // Сервер доступен. Пытаемся выполнить отметку на сервере.
                             VolleyWrapper.doMark(context, requestQueue, null,"A888MP42RUS", new LongOpCallback() {
                                 @Override
                                 public void onSuccess(Object obj, Object param) {
                                     try {
                                         JSONObject jsonObject = ((JSONObject) obj);

                                         // Получаем статус текущего запроса на сервер.
                                         String status = jsonObject.getString("status");

                                         // При запросе на сервер не произошло никакой ошибки. Также сервер не приостановил
                                         // отметку данного гос. номера.
                                         if (!status.equals("error") && !status.equals("disabled")){
                                             // Получаем значение времени задержки перед следующей попыткой отметиться.
                                             int delay = jsonObject.getInt("delay");
                                             // Если запрос на отметку был отложен сервером, обновляем локальную базу данных.
                                             if (!status.equals("postpone")){
                                                 // Получаем список отметок за сегодня.
                                                 JSONArray today_marks  = jsonObject.getJSONArray("today_marks");

                                                 // Добавляем все сегодняшние отметки, если они имеются, в локальную базу данных.
                                                 for (int ii=0; ii<today_marks.length();ii++){
                                                     // С помощью процессора локальной БД записываем все временные метки отметок в БД.
                                                     localDbProc.addMark(today_marks.getString(ii));
                                                 }
                                             }

                                             // Если значение времени задержки от сервера по каки-либо причинам пришло недостоверное, то пытаемся
                                             // исправить это и установить время задержки на значение по-умолчанию.
                                             if (delay <= 0 ) delay = MINUTES_1;
                                             else
                                                 delay = delay * MINUTES_1; // Время необходимой задержки от сервера мы получаем в секундах. Переводим его в миллисеунды.


                                             //Message m = new Message();
                                             //m.copyFrom(ms);

                                             // Взводим курок заново ...
                                             queueHandler.sendMessageDelayed(Message.obtain(msg_copy), delay);

                                         } else { // Ошибка на сервере или клиенту запрещено отмечаться (возможность отметок приостановлена).

                                             // Сервер вернул статус ошибки! Обрабатываем ее.
                                             if (status.equals("error")){
                                                 DebugUtils.debugPrintErrorStd1(context, TAG);
                                                 // Взводим курок заново ..., но ставим максимальный временной интервал на случай исправления ошибок на сервере.
                                                 queueHandler.sendMessageDelayed(Message.obtain(msg_copy), MINUTES_10);
                                             }

                                             // Сервер сообщил, что отметка данного гос. номера временно приостановлена.
                                             // Останавливаем попытки отметится.
                                             if (status.equals("disabled")) {
                                                 /* Иные действия ... */
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
                                 }
                             });
                         } else{ // Сервер не доступен!
                             // Проверим доступность сети через несколько секунд.
                             queueHandler.sendMessageDelayed(Message.obtain(msg), SECONDS_30);
                         }
                    } else { // Сеть WiFi не активна!
                         // Проверим доступность сети через несколько секунд.
                         queueHandler.sendMessageDelayed(Message.obtain(msg), SECONDS_10);
                     }
                }
                break;
        }
    }

    private static void recoverAfterFail(){
        /**/
    }

    public static void init(Context context){
        // Основная инициалзация статических элементов.
        init();

        // Инициализируем менеджер по работе с базой данных и визуальными компонентами, отражающими состояние базы данных.
        localDbProc = localDbProc.getInstance(context);
        //localDbProc.addMark("2018-11-19 00:08:37");
        //localDbProc.clearTableMarks();

        // Настраиваем очередь веб-запросов, если она не была настроена ранее.
        if (requestQueue == null) requestQueue = Volley.newRequestQueue(context);
        if (settings == null) settings = LocalSettings.getInstance(context);
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
        //queueHandler.sendMessage(Message.obtain(queueHandler, MSG_MARK, context));
        return true;
    }

    public static void stop(){
        // Удаляем из очереди все имеющиеся сообщения, если таковые имеются.
        queueHandler.removeCallbacksAndMessages(null);
    }

    public static void onDestroy(){
        if (requestQueue != null)       requestQueue.stop();
        if (queueThreadHandler != null) queueThreadHandler.quitSafely();
        if (settings != null)           settings = null;

        // Остановка компонента работы с локальной базой данных.
        localDbProc.OnDestroy();
    }

}
