package odyssey.projects.callbacks;

/**
 * Created by Odyssey on 16.07.2017.
 */

public interface LongOpCallback {
    public void onSuccess(Object obj, Object param);
    public void onError(Object obj);
}