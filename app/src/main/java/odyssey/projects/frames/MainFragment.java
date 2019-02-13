package odyssey.projects.frames;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import odyssey.projects.callbacks.CallbacksProvider;
import odyssey.projects.callbacks.LoopsCountListener;
import odyssey.projects.callbacks.MarkStatusListener;
import odyssey.projects.callbacks.MarksDatasetListener;
import odyssey.projects.db.MarksView;
import odyssey.projects.debug.DebugOut;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.pref.SettingsCache;
import odyssey.projects.sav.driver.R;
import odyssey.projects.sav.driver.VehicleSelectActivity;
import odyssey.projects.services.MarkOpService;
import pl.droidsonroids.gif.GifImageView;

import static odyssey.projects.sav.driver.Settings.ACTION_TYPE_CMD;
import static odyssey.projects.utils.DateTimeUtils.getDDMMYYYY;

public final class MainFragment extends Fragment {
    public static final String TAG = "MAI_FRAGMENT";

    // Состояния автомата в обработчике сообщений о статусе.
    public static final int MSG_ST_CHANGE_STATUS = 1;

    // Кнопка, на которой отображается текущий выбранный госномер.
    private TextView vehicleFrameButton;

    // Анимация, которая отображает статус менеджера маркеров.
    private GifImageView gifImage;

    // Отображение общего количества пройденных кругов.
    private TextView marksTotal;

    // Класс визуализации списков отметок.
    private MarksView marksView;

    // переключатель "ОТКЛ./АВТО"
    private SwitchCompat mainSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Основная инициализация.
        mainInit();
    }
    // Основная инициализация.
    private void mainInit(){
        // Обработчик различных асинхронных событий и сообщений.
        MessagesHandlerInit();
        // Регистрируем обработчики событий от нажатия различных объектов View.
        setupOnClickListeners();
        // Ициализируем класс для отображения списка отметок.
        marksView = new MarksView(getContext());
        // Инициализируем слушателей
        initListeners();
        // Устанавливаем иконку статуса отметок по-умолчанию - пусто.
        setStatusIconFromForeignThread(0);
        // Делаем запрос статуса сервиса отметок т.к. сервис работает независимо от приложения.
        startMarkOpService(MarkOpService.CMD_GET_STATUS);
        // Останавливаем сервис, если он еще продолжает работу после завершения приложения.
        //stopMarkOpService();
    }

    /* Регистрация слушателей. */
    private void initListeners() {

        // Регистрируем слушателя сообщений об изменениях в статусе отметок на удаленном сервере.
        CallbacksProvider.registerMarkStatusListener(new MarkStatusListener() {
            @Override
            public void changed(MarkOpService.StatusEnum newStatus) {
                if (statusHandler!=null) statusHandler.sendMessage(Message.obtain(statusHandler, MainFragment.MSG_ST_CHANGE_STATUS, newStatus));
            }
        });

        // Регистрируем слушателя сообщений об изменениях в наборе данных отметок локальной БД.
        CallbacksProvider.registerMarkDatasetListener(new MarksDatasetListener() {
            @Override
            public void changed(boolean changed) {
                if (marksView!=null) marksView.doUpdate();
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

        // Настраиваем обработчик сообщений о статусе сервиса отметок.
        statusHandler = new Handler(statusThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                StatusMessagesHandler(msg);
            }
        };
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
        if (getActivity() != null)
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gifImage.setImageResource(resId);
            }
        });
    }

    // Изменяет переключатель ОТКЛ./АВТО из потока, отличного от MainUI Thread.
    private void setSwitchFromForeignThread(final boolean checked){
        if (getActivity() != null)
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String value = "ОТКЛ.";
                if (checked) value = "АВТО.";
                DebugOut.generalPrintInfo(getContext(),"Состояние переключателя ОТКЛ./АВТО. изменено сторонним потоком на:\r\n"+value+".", TAG);
                mainSwitch.setChecked(checked);
            }
        });
    }

    // Изменяет общее значение пройденных кругов.
    private void setMarksTotalFromForeignThread(final int cnt){
        if (getActivity() != null)
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = Integer.valueOf(cnt).toString();
                if (marksView!=null) marksTotal.setText(text);
            }
        });
    }

    // Инициализация слушателей на нажатие объектов.
    private void setupOnClickListeners() {

        if (getActivity() == null) return;
        /* ТАБЛИЧКА СО СТАТУСОМ */
        ConstraintLayout statusLayout = getActivity().findViewById(R.id.statusLayout);
        View statusView = getActivity().getLayoutInflater().inflate(R.layout.status_button_layout, statusLayout, false);
        statusLayout.addView(statusView);

        /* АНИМАЦИЯ НА ТАБЛИЧКЕ СО СТАТУСОМ */
        gifImage = statusView.findViewById(R.id.statusGifImageView);

        /* ТАБЛИЧКА С ГОСНОМЕРОМ */
        ConstraintLayout frameLayout = getActivity().findViewById(R.id.vehicleFrameLayout);
        View frameView = getActivity().getLayoutInflater().inflate(R.layout.vehicle_frame_layout, frameLayout, false);
        frameLayout.addView(frameView);

        // Получаем ссылку на кнопку.
        vehicleFrameButton = getActivity().findViewById(R.id.vehicleIdView);
        // Нажатие на кнопку ГОСНОМЕР.
        if (vehicleFrameButton != null){
            // Устанавливаем текущий госномер из локальных настроек.
            String currentVehicle = SettingsCache.VEHICLE;
            vehicleFrameButton.setHapticFeedbackEnabled(true); // Поддержка обратной связи в виде вибрации от нажатия на элемент.
            vehicleFrameButton.setText((currentVehicle.equals("")?"------":currentVehicle));
            vehicleFrameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vehicleFrameButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    // Запускаем активити выбора госномера. Ждем от нее результата - госномер.
                    startActivityForResult(new Intent(getContext(), VehicleSelectActivity.class), 1);
                }
            });
        }

        /* ПЕРЕКЛЮЧАТЕЛЬ "ОТКЛ./АВТО" */
        mainSwitch =  getActivity().findViewById(R.id.switch1);
        mainSwitch.setHapticFeedbackEnabled(true); // Поддержка обратной связи в виде вибрации от нажатия на элемент.
        mainSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainSwitch.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (mainSwitch.isPressed()){
                    if (!isChecked) {
                        DebugOut.generalPrintInfo(getContext(),"Переключатель ОТКЛ./АВТО. вручную переключен в состояние:\r\nОТКЛ.", TAG);
                        // Останавливаем менеджер управления отметками.
                        stopMarkOpService();
                    } else {
                        DebugOut.generalPrintInfo(getContext(),"Переключатель ОТКЛ./АВТО. вручную переключен в состояние:\r\nАВТО.", TAG);
                        // Запускаем менеджер управления отметками.
                        startMarkOpService(MarkOpService.CMD_RUN_MARKS);
                    }
                }
            }
        });

        /* ЗАГОЛОВОК СПИСКА ОТМЕТОК - ТЕКУЩАЯ ДАТА */
        // Текущая дата списка отметок.
        TextView currDate = getActivity().findViewById(R.id.currDateView);
        currDate.setText(getDDMMYYYY(System.currentTimeMillis()));

        /* ЗАГОЛОВОК СПИСКА ОТМЕТОК  - ОБЩЕЕ КОЛИЧЕСТВО ПРОЙДЕННЫХ КРУГОВ */
        marksTotal = getActivity().findViewById(R.id.totalCountValue);
        marksTotal.setText("0");
    }

    protected void updateCurrentVehicleFrame(){
        String vehicle = SettingsCache.VEHICLE;
        // Обновляем содержимое кнопки.
        if (vehicleFrameButton!=null) vehicleFrameButton.setText( vehicle.equals("")?"--------":vehicle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Проверка на отсутствие возвращаемых данных.
        if (data == null) return;

        // Получаем госномер выбранного ТС из активиты выбора ТС.
        String vehicle = data.getStringExtra("VEHICLE");
        if (vehicle != null && !vehicle.equals("")){
            // Сохраняем выбанное ТС в локальные настройки.
            LocalSettings localSettings = LocalSettings.getInstance(getContext());
            localSettings.saveText(LocalSettings.SP_VEHICLE, vehicle);
            localSettings.updateCacheSettings();

            DebugOut.generalPrintInfo(getContext(),"Зафиксирована смена госномера на "+vehicle+".", TAG);

            // Останавливаем менеджер управления отметками.
            stopMarkOpService();
            // Обновляем список отметок для текущего ТС.
            if (marksView!=null) marksView.doUpdate();
        }
        // Обновляем содержимое кнопки.
        updateCurrentVehicleFrame();
    }


    private void startMarkOpService(int param){
        if (getActivity() != null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                //getActivity().startForegroundService(new Intent(getContext(), MarkOpService.class).putExtra(ACTION_TYPE_CMD, param));
                getActivity().startService(new Intent(getContext(), MarkOpService.class).putExtra(ACTION_TYPE_CMD, param));
            } else {
                getActivity().startService(new Intent(getContext(), MarkOpService.class).putExtra(ACTION_TYPE_CMD, param));
            }
        }
    }

    // Останавливаем менеджер управления отметками.
    private void stopMarkOpService(){
        if (getActivity() != null) {
            // Останавливаем сервис, если он действительно запущен.
            if (isMyServiceRunning(MarkOpService.class)){

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                    //getActivity().startForegroundService(new Intent(getContext(), MarkOpService.class).putExtra(ACTION_TYPE_CMD, MarkOpService.CMD_STOP));
                    getActivity().startService(new Intent(getContext(), MarkOpService.class).putExtra(ACTION_TYPE_CMD, MarkOpService.CMD_STOP));
                } else {
                    getActivity().startService(new Intent(getContext(), MarkOpService.class).putExtra(ACTION_TYPE_CMD, MarkOpService.CMD_STOP));
                }
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        if (getActivity() != null) {
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCurrentVehicleFrame();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Останавливаем сервис, если приложение закрывается.
        stopMarkOpService();
    }
}
