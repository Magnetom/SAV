package odyssey.projects.frames;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import odyssey.projects.callbacks.CallbacksProvider;
import odyssey.projects.db.Db;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.sav.driver.R;
import odyssey.projects.utils.network.StrHelper;

public final class UserSettingsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.user_settigs, rootKey);
        // additional setup
    }

    /*
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.user_settigs, rootKey);
    }
    */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        final Context context = getActivity();

        if (context == null) return;

        if (key.equals(LocalSettings.SP_ALL_DB_REMOVE)) {
            //Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            //connectionPref.setSummary(sharedPreferences.getString(key, ""));
            if (sharedPreferences.getBoolean(key, false)){

                // Сбрасываем значение обратно на FALSE, тем самым имитируя исполнение команды.
                sharedPreferences.edit().putBoolean(key, false).apply();

                // Перезагружаем окно настроек.
                setPreferencesFromResource(R.xml.user_settigs, null);

                // Настраиваем диалоговое окно очистки БД и локальных настроек.
                new AlertDialog.Builder(context)
                        .setIcon(R.drawable.error_outline_red_48x48)
                        .setTitle("Удалить все ходки?")
                        .setPositiveButton("Готово", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {

                                /* ДЕЛАЕМ ОЧИСТКУ БД И ЛОКАЛЬНЫХ НАСТРОЕК! */
                                boolean result = false;
                                // Создается экземпляр класса для работы с БД.
                                Db db = new Db(context);
                                try {
                                    // Стираем все отметки.
                                    db.clearTableMarks();
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
        } else
            // Перепроверяем данные, которые ввел пользователь и удаляем ненужные символы из имени SSID, если таковые есть.
        if (key.equals(LocalSettings.SP_ALLOWED_WIFI_SSID)) {
            sharedPreferences.edit().putString(key, StrHelper.trimSpaces(sharedPreferences.getString(key, ""))).apply();
        }

        // Обновляем настройки в кеше настроек.
        LocalSettings.getInstance(context).updateCacheSettings();
    }


    /*
    @Override
    protected RecyclerView.Adapter onCreateAdapter(PreferenceScreen preferenceScreen) {
        return new CustomPreferenceGroupAdapter(preferenceScreen);
    }

    static class CustomPreferenceGroupAdapter extends PreferenceGroupAdapter {

        @SuppressLint("RestrictedApi")
        public CustomPreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
            super(preferenceGroup);
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onBindViewHolder(PreferenceViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            Preference currentPreference = getItem(position);
            //For a preference category we want the divider shown above.
            if (position != 0 && currentPreference instanceof PreferenceCategory) {
                holder.setDividerAllowedAbove(true);
                holder.setDividerAllowedBelow(false);
            } else {
                //For other dividers we do not want to show divider above
                //but allow dividers below for CategoryPreference dividers.
                holder.setDividerAllowedAbove(false);
                holder.setDividerAllowedBelow(true);
            }
        }
    }
    */
    @Override
    public void onResume() {
        super.onResume();
        //unregister the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
