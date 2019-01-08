package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import odyssey.projects.callbacks.CallbacksProvider;
import odyssey.projects.db.Db;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.services.MarkOpService;

public class LocalPrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.admin_settings);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {

        final Context context = this;

        // Останавливаем менеджер управления отметками.
        stopService(new Intent(LocalPrefActivity.this, MarkOpService.class));

        if (key.equals(LocalSettings.SP_ALL_DB_REMOVE)) {
            //Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            //connectionPref.setSummary(sharedPreferences.getString(key, ""));
            if (sharedPreferences.getBoolean(key, false)){

                // Сбрасываем значение обратно на FALSE, тем самым имитируя исполнение команды.
                sharedPreferences.edit().putBoolean(key, false).apply();
                // Перезагружаем окно настроек.
                addPreferencesFromResource(R.xml.admin_settings);

                // Настраиваем диалоговое окно очистки БД и локальных настроек.
                new AlertDialog.Builder(context)
                        .setIcon(R.drawable.error_outline_red_48x48)
                        .setTitle("Удалить все данные?")
                        .setPositiveButton("Готово", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {

                                /* ДЕЛАЕМ ОЧИСТКУ БД И ЛОКАЛЬНЫХ НАСТРОЕК! */
                                boolean result = false;
                                // Создается экземпляр класса для работы с БД.
                                Db db = new Db(context);
                                try {
                                    // Стираем все отметки.
                                    db.clearTableMarks();
                                    // Стираем все госномера.
                                    db.removeAllVehicles();
                                    result = true;
                                } catch (SQLiteException e){
                                    e.printStackTrace();
                                }

                                if (CallbacksProvider.getMarksDatasetCallback() != null){
                                    CallbacksProvider.getMarksDatasetCallback().changed(true);
                                }

                                if (CallbacksProvider.getLoopsCountListener() != null){
                                    CallbacksProvider.getLoopsCountListener().LoopsUpdated(0);
                                }

                                // Формируем сообщение о результате очистки.
                                final String mess = result?"Данные очищены.":"Ошибка очистки!";
                                final int res = result?R.drawable.info_success_48:R.drawable.info_error_48;

                                //////////////////////////////////////////////////////////////
                                // Настраиваем диалоговое окно информирования об окончании очистки БД.
                                new AlertDialog.Builder(context)
                                        .setIcon(res)
                                        .setTitle(mess)
                                        .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .create()
                                        .show();
                                //////////////////////////////////////////////////////////////

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

        // Обновляем настройки в кеше настроек.
        LocalSettings.getInstance(context).updateCacheSettings();
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

