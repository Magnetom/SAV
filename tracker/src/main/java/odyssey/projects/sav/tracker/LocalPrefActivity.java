package odyssey.projects.sav.tracker;


import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LocalPrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "LOCAL_PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        /* */
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

    // And override this method
    @Override
    public boolean onNavigateUp(){
        finish();
        return true;
    }
}

