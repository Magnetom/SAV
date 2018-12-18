package odyssey.projects.frames;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import odyssey.projects.sav.driver.R;

public final class UserSettingsPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.user_settigs, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //ListView lv = (ListView) view.findViewById(android.R.id.list);
        //ViewGroup parent = (ViewGroup)lv.getParent();
        //parent.setPadding(0, 0, 0, 0);
        //view.setPadding(0,0,0,0);
    }
}
