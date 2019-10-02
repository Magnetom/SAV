package odyssey.projects.frames;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.Objects;

import odyssey.projects.callbacks.OnViewCreatedListener;
import odyssey.projects.debug.LogViewer;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.pref.SettingsCache;
import odyssey.projects.sav.driver.R;


public final class DebugLogFragment extends Fragment {

    private static final String TAG = "DEBUG_LOG_FRAGMENT";

    private LogViewer logViewer;
    private OnViewCreatedListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.debug_log_frament, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Основная инициализация.
        mainInit();

        if (listener != null) listener.onViewCreated(view,savedInstanceState);
    }

    public void setOnViewCreatedListener(OnViewCreatedListener listener){
        this.listener = listener;
    }

    // Основная инициализация.
    private void mainInit(){
        logViewer = new LogViewer(getContext());
        // Инициализируем слушателей.
        initListeners();
    }

    /* Регистрация слушателей. */
    private void initListeners() {
        // Кнопка "ОЧИСТИТЬ ЛОГ СОБЫТИЙ"
        Button clrBtn = Objects.requireNonNull(getView()).findViewById(R.id.logClearButton);
        if (clrBtn!=null){
            clrBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getContext() != null)
                    // Настраиваем диалоговое окно очистки БД и локальных настроек.
                    new AlertDialog.Builder(getContext())
                            .setIcon(R.drawable.error_outline_red_48x48)
                            .setTitle("Очистить лог событий?")
                            .setPositiveButton("ОЧИСТИТЬ", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    if (logViewer!=null){
                                        logViewer.clearAllReports();
                                    }
                                    // Закрываем текущее диалоговое окно.
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Закрываем текущее диалоговое окно.
                                    dialog.cancel();
                                }
                            })
                            .create()
                            .show();
                }
            });
        }

        final LocalSettings localSettings = LocalSettings.getInstance(getContext());

        // Чекбокс "ИНФОРМАЦИЯ"
        final CheckBox infoChkBox = Objects.requireNonNull(getView()).findViewById(R.id.infoCheckBox);
        if (infoChkBox!=null){

            infoChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (infoChkBox.isPressed()){
                        localSettings.saveBoolean(LocalSettings.SP_DEBUG_LOG_INFO, isChecked);
                        localSettings.updateCacheSettings();
                    }
                }
            });
            //infoChkBox.setChecked(localSettings.getBoolean(LocalSettings.SP_DEBUG_LOG_INFO));
            infoChkBox.post(new Runnable() {
                @Override
                public void run() {
                    infoChkBox.setChecked(SettingsCache.DEBUG_LOG_INFO);
                }
            });

            /*
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    infoChkBox.setChecked(true);
                }
            }, 6000);
            */
        }

        // Чекбокс "ПРЕДУПРЕЖДЕНИЯ"
        final CheckBox warnChkBox = Objects.requireNonNull(getView()).findViewById(R.id.warnCheckBox);
        if (warnChkBox!=null){
            warnChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (warnChkBox.isPressed()){
                        localSettings.saveBoolean(LocalSettings.SP_DEBUG_LOG_WARN, isChecked);
                        localSettings.updateCacheSettings();
                    }
                }
            });
            //warnChkBox.setChecked(localSettings.getBoolean(LocalSettings.SP_DEBUG_LOG_WARN));
            warnChkBox.post(new Runnable() {
                @Override
                public void run() {
                    warnChkBox.setChecked(SettingsCache.DEBUG_LOG_WARN);
                }
            });
        }

        // Чекбокс "ОШИБКИ"
        final CheckBox errorChkBox = Objects.requireNonNull(getView()).findViewById(R.id.errorCheckBox);
        if (errorChkBox!=null){
            errorChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (errorChkBox.isPressed()){
                        localSettings.saveBoolean(LocalSettings.SP_DEBUG_LOG_ERROR, isChecked);
                        localSettings.updateCacheSettings();
                    }
                }
            });
            //errorChkBox.setChecked(localSettings.getBoolean(LocalSettings.SP_DEBUG_LOG_ERROR));
            errorChkBox.post(new Runnable() {
                @Override
                public void run() {
                    errorChkBox.setChecked(SettingsCache.DEBUG_LOG_ERROR);
                }
            });
        }

        // Кнопка "РАСШАРИТЬ ЛОГ"
        Button shareBtn = Objects.requireNonNull(getView()).findViewById(R.id.shareButton);
        if (shareBtn!=null){
            shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (logViewer!=null) logViewer.shareData();
                    /*
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    */
                }
            });
        }
    }
}
