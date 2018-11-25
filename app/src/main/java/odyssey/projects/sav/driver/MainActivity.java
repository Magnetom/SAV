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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import odyssey.projects.db.DbProcessor;
import odyssey.projects.pref.LocalSettings;

public class MainActivity extends AppCompatActivity {

    // Кнопка, на которой отображается текущий выбранный госномер.
    TextView vehicleFrameButton;

    public static final int MSG_MM_CHANGE_STATUS = 1;

    private static Handler queueHandler;

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
        // Инициализация менеджера отметок.
        RemoteMarkManager.init(this);
        // Регистрируем обработчики событий от нажатия различных объектов View.
        setupOnClickListeners();
    }

    private void MessagesHandlerInit() {

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
    }

    private void MessagesHandler(Message msg) {
        switch (msg.what) {
            //-----------------------------------------------------
            // Сообщение об изменении статуса менеджера отметок.
            case MSG_MM_CHANGE_STATUS:
                int ii = 0;
                break;
            //-----------------------------------------------------
            default:break;
        }
    }

    public static Handler getMainHandler(){
        return queueHandler;
    }

    // Инициализация слушателей на нажатие объектов.
    private void setupOnClickListeners() {

        final Context context = this;

        /* ТАБЛИЧКА С ГОСНОМЕРОМ */
        // Визуализируем табличку с госномером.
        //ViewStub viewStub = (ViewStub) findViewById(R.id.emptyStub);
        //viewStub.inflate();

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
