package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import odyssey.projects.db.DbProcessor;
import odyssey.projects.pref.LocalSettings;

public class LocalPrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {

        final Context context = this;

        RemoteMarkManager.setStopRequest();

        if (key.equals(LocalSettings.SP_ALL_DB_REMOVE)) {
            //Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            //connectionPref.setSummary(sharedPreferences.getString(key, ""));
            if (sharedPreferences.getBoolean(key, false)){

                // Сбрасываем значение обратно на FALSE, тем самым имитируя исполнение команды.
                sharedPreferences.edit().putBoolean(key, false).apply();
                // Перезагружаем текущую активити для актуализиции состояния чекбокса.
                recreate();

                // Настраиваем диалоговое окно ввода госномера.
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.error_outline_red_48x48)
                        .setTitle("Удалить все данные?")
                        .setPositiveButton("Готово", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {

                                /* ДЕЛАЕМ ОЧИСТКУ БД И ЛОКАЛЬНЫХ НАСТРОЕК! */
                                DbProcessor.getInstance(context).clearTableMarks();
                                // Стираем все госномера.
                                DbProcessor.getInstance(context).removeAllVehicles();
                                // Стираем текущее ТС.
                                sharedPreferences.edit().putString(LocalSettings.SP_VEHICLE, "").apply();
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}

