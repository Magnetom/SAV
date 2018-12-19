/*
 * About: The System of Accounting of Vehicles (SAV).
 * Author: Odyssey
 * Date: 11.2018
 */

package odyssey.projects.sav.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import odyssey.projects.adapters.ViewPagerAdapter;
import odyssey.projects.frames.MainFragment;
import odyssey.projects.frames.UserSettingsPreferenceFragment;
import odyssey.projects.services.MarkOpService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Настраивается адаптер для ViewPager.
        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new MainFragment());
        pagerAdapter.addFragment(new UserSettingsPreferenceFragment());

        // Настраивается ViewPager.
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Останавливаем менеджер управления отметками.
        stopService(new Intent(this, MarkOpService.class));
    }
}