package odyssey.projects.sav.location;

import android.location.Location;

public interface LocationResult {
    void onAllProvidersDisabled();
    void onUnknownLocation();
    void onSuccessResult(Location location);
}
