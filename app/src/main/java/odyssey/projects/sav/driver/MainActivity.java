/*
 * About: The System of Accounting of Vehicles (SAV).
 * Author: Odyssey
 * Date: 11.2018
 */

package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import odyssey.projects.db.DbProcessor;
import odyssey.projects.pref.LocalSettings;
import pl.droidsonroids.gif.GifImageView;

import static odyssey.projects.utils.DateTimeUtils.getDDMMYYYY;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_GEN_MARKS_CNT    = 1;
    public static final int MSG_ST_CHANGE_STATUS = 2;


    // Кнопка, на которой отображается текущий выбранный госномер.
    private TextView vehicleFrameButton;

    // Текущая дата списка отметок.
    private TextView currDate;

    // Анимация, которая отображает статус менеджера маркеров.
    private GifImageView gifImage;

    // Отображение общего количества пройденных кругов.
    private TextView marksTotal;

    // переключатель "ОТКЛ./АВТО"
    SwitchCompat mainSwitch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Основная инициализация.
        mainInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RemoteMarkManager.onDestroy();
    }

    // Основная инициализация.
    private void mainInit(){
        // Обработчик различных асинхронных событий и сообщений.
        MessagesHandlerInit();
        // Регистрируем обработчики событий от нажатия различных объектов View.
        setupOnClickListeners();
        // Инициализация менеджера отметок.
        RemoteMarkManager.init(this, statusHandler, generalHandler);
    }

    /*
    Handler statusHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //super.handleMessage(msg);
            StatusMessagesHandler(msg);
            return true;
        }
    });
    */
    private Handler statusHandler;  // Обработчик сообщений о статусе текущей отметки на удаленном сервере.
    private Handler generalHandler; // Основной обработчик общих сообщений.

    private void MessagesHandlerInit() {

        /* Регистрируем обработчик изменений статуса сетевых операций (отметки на удаленном сервере). */
        HandlerThread statusThreadHandler = new HandlerThread("STATUS_THREAD_HANDLER", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        // Запускаем поток.
        statusThreadHandler.start();
        // Настраиваем обработчик сообщений.
        statusHandler = new Handler(statusThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                StatusMessagesHandler(msg);
            }
        };

        /* Регистрируем обработчик для прочих сообщений */
        HandlerThread generalThreadHandler = new HandlerThread("GENERAL_THREAD_HANDLER", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        // Запускаем поток.
        generalThreadHandler.start();
        // Настраиваем обработчик сообщений.
        generalHandler = new Handler(generalThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                GeneralMessagesHandler(msg);
            }
        };
    }

    private void GeneralMessagesHandler(Message msg) {
        switch (msg.what) {
            //-----------------------------------------------------
            // Сообщение об текущем количестве кругов. Приходит от обертки адаптера списка по
            // окончанию прорисовки всех элементов списка.
            case MSG_GEN_MARKS_CNT:
                // Количество пройденных кругов.
                int marks_cnt = msg.arg1;
                setMarksTotalFromForeignThread(marks_cnt);
                break;
            //-----------------------------------------------------
            default:break;
        }
    }

    private void StatusMessagesHandler(Message msg) {

        switch (msg.what) {
            //-----------------------------------------------------
            // Сообщение об изменении статуса менеджера отметок.
            case MSG_ST_CHANGE_STATUS:

                // Получаем статус системы отметок через входящее сообщение.
                RemoteMarkManager.StatusEnum status = (RemoteMarkManager.StatusEnum)msg.obj;

                try {
                    // СТАТУСЫ МАРКЕРА
                    switch (status){
                        //-----------------------------------------------------
                        // Система маркеров сообщила об ошибке. Необходимо гарантированно задержать
                        // отображение этого значка на некоторое время, чтобы его смог заметить пользователь.
                        case FAIL:
                            setStatusIconFromForeignThread(R.drawable.status_fail);
                            Thread.sleep(2000);
                            break;
                        //-----------------------------------------------------
                        // Система маркеров еще не инициалиизрована.
                        case NO_INIT:
                            //setStatusIconFromForeignThread(R.drawable.status_blocked);
                            setStatusIconFromForeignThread(0);
                            break;
                        //-----------------------------------------------------
                        // Система маркеров отсановлена.
                        case STOPPED:
                            //setStatusIconFromForeignThread(R.drawable.status_blocked);
                            setStatusIconFromForeignThread(0);
                            // Переводим переключатель в состояние ОТКЛ.
                            setSwitchFromForeignThread(false);
                            break;
                        //-----------------------------------------------------
                        // Система маркеров запущена.
                        case ACTIVATED:
                            setStatusIconFromForeignThread(R.drawable.status_activated);
                            Thread.sleep(500);
                            break;
                        //-----------------------------------------------------
                        // Сеть WiFi найдена. Попытка подключиться к серверу.
                        case CONNECTING:
                            setStatusIconFromForeignThread(R.drawable.status_connecting);
                            Thread.sleep(1500);
                            break;
                        //-----------------------------------------------------
                        // Сервер обнаружен в сети. Попытка передать отметку.
                        case CONNECTED:
                            setStatusIconFromForeignThread(R.drawable.status_connected);
                            Thread.sleep(1500);
                            break;
                        //-----------------------------------------------------
                        // Произведено успешное подключение к серверу и выполнена отметка.
                        case IDLE:
                            setStatusIconFromForeignThread(R.drawable.status_idle);
                            break;
                        //-----------------------------------------------------
                        // Сервер сообщил о том, что еще не вышел таймаут после последней отметки.
                        case POSTPONE:
                            setStatusIconFromForeignThread(R.drawable.status_postponded);
                            break;
                        //-----------------------------------------------------
                        // Сервер сообщил о том, что еще текущий госномер заблокирован на сервере администратором ресурса.
                        case BLOCKED:
                            setStatusIconFromForeignThread(R.drawable.status_blocked);
                            break;
                        //-----------------------------------------------------
                        default:
                            setStatusIconFromForeignThread(R.drawable.status_fail);
                            break;
                    }
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            //-----------------------------------------------------
            default:break;
        }
    }

    // Изменяет иконку статуса из потока, отличного от MainUI Thread.
    private void setStatusIconFromForeignThread(final int resId){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gifImage.setImageResource(resId);
            }
        });
    }

    // Изменяет переключатель ОТКЛ./АВТО из потока, отличного от MainUI Thread.
    private void setSwitchFromForeignThread(final boolean checked){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainSwitch.setChecked(checked);
            }
        });
    }

    // Изменяет общее значение пройденных кругов.
    private void setMarksTotalFromForeignThread(final int cnt){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = Integer.valueOf(cnt).toString();
                marksTotal.setText(text);
            }
        });
    }

    // Инициализация слушателей на нажатие объектов.
    private void setupOnClickListeners() {

        final Context context = this;

        /* ТАБЛИЧКА СО СТАТУСОМ */
        ConstraintLayout statusLayout = findViewById(R.id.statusLayout);
        View statusView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.status_button_layout, statusLayout, false);
        statusLayout.addView(statusView);

        /* АНИМАЦИЯ НА ТАБЛИЧКЕ СО СТАТУСОМ */
        gifImage = statusView.findViewById(R.id.statusGifImageView);

        /* ТАБЛИЧКА С ГОСНОМЕРОМ */
        ConstraintLayout frameLayout = findViewById(R.id.vehicleFrameLayout);
        View frameView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.vehicle_frame_layout, frameLayout, false);
        frameLayout.addView(frameView);

        // Получаем ссылку на кнопку.
        vehicleFrameButton = findViewById(R.id.vehicleIdView);
        // Нажатие на кнопку ГОСНОМЕР.
        if (vehicleFrameButton != null){
            // Устанавливаем текущий госномер из локальных настроек.
            String currentVehicle = LocalSettings.getInstance(this).getText(LocalSettings.SP_VEHICLE);
            vehicleFrameButton.setText((currentVehicle.equals("")?"------":currentVehicle));
            vehicleFrameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Запускаем активити выбора госномера. Ждем от нее результата - госномер.
                    startActivityForResult(new Intent(context, VehicleSelectActivity.class), 1);
                }
            });
        }

        /* ПЕРЕКЛЮЧАТЕЛЬ "ОТКЛ./АВТО" */
        mainSwitch =  findViewById(R.id.switch1);
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // Останавливаем менеджер управления отметками.
                    RemoteMarkManager.stop();
                } else {
                    // Запускаем менеджер управления отметками.
                    RemoteMarkManager.reRun(context);
                }
            }
        });

        /* ЗАГОЛОВОК СПИСКА ОТМЕТОК - ТЕКУЩАЯ ДАТА */
        currDate = findViewById(R.id.currDateView);
        currDate.setText(getDDMMYYYY(System.currentTimeMillis()));

        /* ЗАГОЛОВОК СПИСКА ОТМЕТОК  - ОБЩЕЕ КОЛИЧЕСТВО ПРОЙДЕННЫХ КРУГОВ */
        marksTotal = findViewById(R.id.totalCountValue);
        marksTotal.setText("0");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Проверка на отсутствие возвращаемых данных.
        if (data == null) return;

        // Получаем госномер выбранного ТС из активиты выбора ТС.
        String vehicle = data.getStringExtra("VEHICLE");
        if (vehicle != null && !vehicle.equals("")){
            // Сохраняем выбанное ТС в локальные настройки.
            LocalSettings.getInstance(this).saveText(LocalSettings.SP_VEHICLE, vehicle);

            // Параллельно сохраняем  номер ТС в локальную БД для ведения статистики и формирования списка всех ТС,
            // используемых данным приложением.
            Boolean result = DbProcessor.getInstance(this).insertVehicle(vehicle);
            //Boolean result = VehiclesViewer.getInstance(this).insertVehicle(vehicle);

            // Запускаем менеджер управления отметками.
            //RemoteMarkManager.reRun(this);

            // Останавливаем менеджер управления отметками.
            RemoteMarkManager.stop();
        }

        // Обновляем содержимое кнопки.
        vehicleFrameButton.setText( vehicle.equals("")?"--------":vehicle);
    }

}
