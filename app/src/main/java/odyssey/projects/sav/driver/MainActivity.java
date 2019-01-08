/*
 * About: The System of Accounting of Vehicles (SAV).
 * Author: Odyssey
 * Date: 11.2018
 */

package odyssey.projects.sav.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import odyssey.projects.adapters.ViewPagerAdapter;
import odyssey.projects.debug.DebugOut;
import odyssey.projects.frames.DebugLogFragment;
import odyssey.projects.frames.MainFragment;
import odyssey.projects.callbacks.OnViewCreatedListener;
import odyssey.projects.frames.UserSettingsPreferenceFragment;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.services.MarkOpService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализируем класс арботы с локальными настройками.
        LocalSettings.getInstance(this);

        // Настраивается адаптер для ViewPager.
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        DebugLogFragment debugFragment = new DebugLogFragment();

        debugFragment.setOnViewCreatedListener(new OnViewCreatedListener() {
            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                DebugOut.generalPrintInfo(getApplicationContext(), "Приложение запущено.", TAG);
            }
        });

        pagerAdapter.addFragment(new MainFragment());
        pagerAdapter.addFragment(new UserSettingsPreferenceFragment());
        pagerAdapter.addFragment(debugFragment);

        // Настраивается ViewPager.
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(pagerAdapter.getCount());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Останавливаем менеджер управления отметками.
        stopService(new Intent(this, MarkOpService.class));
    }
}