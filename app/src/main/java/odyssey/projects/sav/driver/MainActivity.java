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
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import odyssey.projects.callbacks.CallbacksProvider;
import odyssey.projects.callbacks.LoopsCountListener;
import odyssey.projects.callbacks.MarkStatusListener;
import odyssey.projects.callbacks.MarksDatasetListener;
import odyssey.projects.db.MarksView;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.services.MarkOpService;
import pl.droidsonroids.gif.GifImageView;

import static odyssey.projects.sav.driver.Settings.ACTION_TYPE_CMD;
import static odyssey.projects.utils.DateTimeUtils.getDDMMYYYY;

public class MainActivity extends AppCompatActivity {

    // Состояния автомата в обработчике сообщений о статусе.
    public static final int MSG_ST_CHANGE_STATUS = 1;

    // Кнопка, на которой отображается текущий выбранный госномер.
    private TextView vehicleFrameButton;

    // Текущая дата списка отметок.
    private TextView currDate;

    // Анимация, которая отображает статус менеджера маркеров.
    private GifImageView gifImage;

    // Отображение общего количества пройденных кругов.
    private TextView marksTotal;

    // Класс визуализации списков отметок.
    private MarksView marksView;

    // переключатель "ОТКЛ./АВТО"
    private SwitchCompat mainSwitch;

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
    }

    // Основная инициализация.
    private void mainInit(){
        // Обработчик различных асинхронных событий и сообщений.
        MessagesHandlerInit();
        // Регистрируем обработчики событий от нажатия различных объектов View.
        setupOnClickListeners();
        // Ициализируем класс для отображения списка отметок.
        marksView = new MarksView(this);
        // Инициализируем слушателей
        initListeners();
        // Устанавливаем иконку статуса отметок по-умолчанию - пусто.
        setStatusIconFromForeignThread(0);
        // Делаем запрос статуса сервиса отметок т.к. сервис работаем независимо от приложения.
        startService(new Intent(MainActivity.this, MarkOpService.class).putExtra(ACTION_TYPE_CMD, MarkOpService.CMD_GET_STATUS));
    }

    /* Регистрация слушателей. */
    private void initListeners() {

        // Регистрируем слушателя сообщений об изменениях в статусе отметок на удаленном сервере.
        CallbacksProvider.registerMarkStatusListener(new MarkStatusListener() {
            @Override
            public void changed(MarkOpService.StatusEnum newStatus) {
                statusHandler.sendMessage(Message.obtain(statusHandler, MainActivity.MSG_ST_CHANGE_STATUS, newStatus));
            }
        });

        // Регистрируем слушателя сообщений об изменениях в наборе данных отметок локальной БД.
        CallbacksProvider.registerMarkDatasetListener(new MarksDatasetListener() {
            @Override
            public void changed(boolean changed) {
                marksView.doUpdate();
            }
        });

        // Регистрируем слушателя сообщений об изменении количества пройденных кругов
        CallbacksProvider.registerLoopsListener(new LoopsCountListener() {
            @Override
            public void LoopsUpdated(int loops) {
                setMarksTotalFromForeignThread(loops);
            }
        });
    }

    private Handler statusHandler;  // Обработчик сообщений о статусе текущей отметки на удаленном сервере.

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
    }

    private void StatusMessagesHandler(Message msg) {

        switch (msg.what) {
            //-----------------------------------------------------
            // Сообщение об изменении статуса менеджера отметок.
            case MSG_ST_CHANGE_STATUS:

                // Получаем статус системы отметок через входящее сообщение.
                MarkOpService.StatusEnum status = (MarkOpService.StatusEnum)msg.obj;

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
            vehicleFrameButton.setHapticFeedbackEnabled(true); // Поддержка обратной связи в виде вибрации от нажатия на элемент.
            vehicleFrameButton.setText((currentVehicle.equals("")?"------":currentVehicle));
            vehicleFrameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vehicleFrameButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    // Запускаем активити выбора госномера. Ждем от нее результата - госномер.
                    startActivityForResult(new Intent(context, VehicleSelectActivity.class), 1);
                }
            });
        }

        /* ПЕРЕКЛЮЧАТЕЛЬ "ОТКЛ./АВТО" */
        mainSwitch =  findViewById(R.id.switch1);
        mainSwitch.setHapticFeedbackEnabled(true); // Поддержка обратной связи в виде вибрации от нажатия на элемент.
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainSwitch.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (!isChecked) {
                    // Останавливаем менеджер управления отметками.
                    stopService(new Intent(MainActivity.this, MarkOpService.class));
                } else {
                    // Запускаем менеджер управления отметками.
                    startService(new Intent(MainActivity.this, MarkOpService.class).putExtra(ACTION_TYPE_CMD, MarkOpService.CMD_RUN_MARKS));
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

    protected void updateCurrentVehicleFrame(){
        String vehicle = LocalSettings.getInstance(this).getText(LocalSettings.SP_VEHICLE);
        // Обновляем содержимое кнопки.
        vehicleFrameButton.setText( vehicle.equals("")?"--------":vehicle);
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
            // Останавливаем менеджер управления отметками.
            stopService(new Intent(MainActivity.this, MarkOpService.class));
            // Обновляем список отметок для текущего ТС.
            marksView.doUpdate();
        }
        // Обновляем содержимое кнопки.
        updateCurrentVehicleFrame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentVehicleFrame();
    }
}
