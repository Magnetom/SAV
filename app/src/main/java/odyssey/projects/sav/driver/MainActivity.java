/*
 * About: The System of Accounting of Vehicles (SAV).
 * Author: Odyssey
 * Date: 11.2018
 */

package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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

public class MainActivity extends AppCompatActivity {

    public static final int MSG_INT_UNBLOCK       = 1;
    public static final int MSG_EXT_CHANGE_STATUS = 2;

    // Кнопка, на которой отображается текущий выбранный госномер.
    TextView vehicleFrameButton;

    // Анимация, которая отображает статус менеджера маркеров.
    GifImageView gifImage;

    // переключатель "ОТКЛ./АВТО"
    SwitchCompat mainSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Основная инициализация.
        mainInit();

        this.findViewById(R.id.MyTestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean result = DbProcessor.getInstance(getApplication()).insertVehicle("H750AM750");
                int ii = 0;
            }
        });

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
        RemoteMarkManager.init(this, queueHandler);
    }

    Handler queueHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            MessagesHandler(msg);
            return true;
        }
    });

    private void MessagesHandlerInit() {

        /*
        HandlerThread queueThreadHandler = new HandlerThread("MAIN_ACTIVITY_HANDLER_THREAD", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        // Запускаем поток.
        queueThreadHandler.start();
        // Настраиваем обработчик сообщений.
        queueHandler = new Handler(queueThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                MessagesHandler(msg);
            }
        };
        */
    }

    private static Boolean blocked = false;

    private void MessagesHandler(Message msg) {

        switch (msg.what) {
            //-----------------------------------------------------
            case MSG_INT_UNBLOCK:
                blocked = false;
                break;
            //-----------------------------------------------------
            // Сообщение об изменении статуса менеджера отметок.
            case MSG_EXT_CHANGE_STATUS:

                // Если обнаружено, что система визуализации временно заблоирована (для удержания какого-либо)
                // значка на экране, то откладываем его выполнение на некоторое время.
                if (blocked) {
                    queueHandler.sendMessageDelayed(Message.obtain(msg), 100);
                    return;
                }

                RemoteMarkManager.StatusEnum status = (RemoteMarkManager.StatusEnum)msg.obj;
                // СТАТУСЫ МАРКЕРА
                switch (status){
                    //-----------------------------------------------------
                    // Система маркеров сообщила об ошибке. Необходимо гарантированно задержать
                    // отображение этого значка на некоторое время, чтобы его смог заметить пользователь.
                    case FAIL:
                        gifImage.setImageResource(R.drawable.status_fail);
                        blocked = true;
                        queueHandler.sendMessageDelayed(Message.obtain(queueHandler, MSG_INT_UNBLOCK), 2000);
                        break;
                    //-----------------------------------------------------
                    // Система маркеров еще не инициалиизрована.
                    case NO_INIT:
                        gifImage.setImageResource(R.drawable.status_stopped);
                        break;
                    //-----------------------------------------------------
                    // Система маркеров отсановлена.
                    case STOPPED:
                        gifImage.setImageResource(R.drawable.status_stopped);
                        // Переводим переключатель в состояние ОТКЛ.
                        mainSwitch.setChecked(false);
                        break;
                    //-----------------------------------------------------
                    // Система маркеров запущена.
                    case ACTIVATED:
                        gifImage.setImageResource(R.drawable.status_activated);
                        blocked = true;
                        queueHandler.sendMessageDelayed(Message.obtain(queueHandler, MSG_INT_UNBLOCK), 500);
                        break;
                    //-----------------------------------------------------
                    // Сеть WiFi найдена. Попытка подключиться к серверу.
                    case CONNECTING:
                        gifImage.setImageResource(R.drawable.status_connecting);
                        blocked = true;
                        queueHandler.sendMessageDelayed(Message.obtain(queueHandler, MSG_INT_UNBLOCK), 1500);
                        break;
                    //-----------------------------------------------------
                    // Сервер обнаружен в сети. Попытка передать отметку.
                    case CONNECTED:
                        gifImage.setImageResource(R.drawable.status_connected);
                        blocked = true;
                        queueHandler.sendMessageDelayed(Message.obtain(queueHandler, MSG_INT_UNBLOCK), 1500);
                        break;
                    //-----------------------------------------------------
                    // Произведено успешное подключение к серверу и выполнена отметка.
                    case IDLE:
                        gifImage.setImageResource(R.drawable.status_idle);
                        break;
                    //-----------------------------------------------------
                    // Сервер сообщил о том, что еще не вышел таймаут после последней отметки.
                    case POSTPONE:
                        gifImage.setImageResource(R.drawable.status_postponded);
                        break;
                    //-----------------------------------------------------
                    default:
                        gifImage.setImageResource(R.drawable.status_fail);
                        break;
                }
                break;
            //-----------------------------------------------------
            default:break;
        }
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

            // Обновляем содержимое кнопки.
            vehicleFrameButton.setText(vehicle);

            // Запускаем менеджер управления отметками.
            //RemoteMarkManager.reRun(this);

            // Останавливаем менеджер управления отметками.
            RemoteMarkManager.stop();
        }
    }

}
