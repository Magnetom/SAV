package odyssey.projects.sav.remote;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import odyssey.projects.sav.db.Track;
import odyssey.projects.sav.debug.DebugOut;

public class UploadManager {

    private static final String TAG = "UPLOAD_MANAGER";

    /* Основные маркеры ответов сервера. */
    private static final String SRV_MARKER_DETAILS  = "details";
    private static final String SRV_MARKER_STATUS   = "status";

    /* Возможные статусы штатных ответов сервера. */
    private static final String SRV_STATUS_SUCCESS   = "success";
    private static final String SRV_STATUS_EMPTY     = "empty";
    private static final String SRV_STATUS_UNKNOWN   = "unknown";
    private static final String SRV_STATUS_ERROR     = "error";

    public static void doUpload(final Context context, final Track track){

        VolleyWrapper.doUpload(context, null, null, track, new LongOpCallback() {

            @Override
            public void onSuccess(Object obj, Object param) {

                try{

                    JSONObject jsonObject = ((JSONObject) obj);
                    // Получаем статус текущего запроса на сервер.
                    String respStatus = jsonObject.getString(SRV_MARKER_STATUS);

                    // выгрузка завершилась успехом.
                    if (respStatus.equalsIgnoreCase(SRV_STATUS_SUCCESS)){
                        DebugOut.generalPrintInfo(context, "Маршрут '"+track.getName()+"' выгружен успешно.", TAG);

                    } else { // Выгрузка завершлась с ОШИБКОЙ!
                        String extra = "- статус: " + respStatus;
                        if (!jsonObject.isNull(SRV_MARKER_DETAILS)) extra += "\r\n- причина: " + jsonObject.getString(SRV_MARKER_DETAILS);
                        DebugOut.generalPrintWarning(context, "Выгрузка не удалась!\r\nПодробнее:\r\n"+extra, TAG);
                    }

                } catch (JSONException e){
                    DebugOut.debugPrintException(context, e, TAG);
                    DebugOut.generalPrintWarning(context, "Неизвестная ошибка при обработке ответа с сервера!\r\nПодробнее:\r\n" + e.getLocalizedMessage(), TAG);
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Object obj) {
                DebugOut.debugPrintVolleyError(context, obj, TAG);
            }
        });
    }
}
