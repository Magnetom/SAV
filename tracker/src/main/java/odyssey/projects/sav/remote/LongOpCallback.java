package odyssey.projects.sav.remote;

/**
 * Created by Odyssey on 16.07.2017.
 */

public interface LongOpCallback {
    void onSuccess(Object obj, Object param);
    void onError(Object obj);
}