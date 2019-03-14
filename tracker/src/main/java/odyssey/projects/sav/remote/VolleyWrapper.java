package odyssey.projects.sav.remote;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import odyssey.projects.sav.Settings;
import odyssey.projects.sav.db.LocationPoint;
import odyssey.projects.sav.db.Track;

import static odyssey.projects.sav.utils.UidUtils.getStrUid;

public class VolleyWrapper {

    private final static String TAG = "VOLLEY";


    /* @Brief: Выкладывает на сервер указанный маршрут и принадлежащие ему точки.
     */
    public static void doUpload(final Context context,
                                final RequestQueue requestQueue,
                                JSONObject jsonRequest,
                                final Track track,
                                final LongOpCallback callback){

        if (jsonRequest == null) jsonRequest = new JSONObject();

        // Здесь будут лежать все параметры с ключами.
        Map<String,String> params = new HashMap<>();
        params.put("type", "track");

        JSONObject jsonTrack  = new JSONObject();
        JSONObject jsonPoints = new JSONObject();
        JSONArray  jsonPoints2 = new JSONArray();

        try {
            // Получаем идентификатор маршрута и его имя.
            jsonTrack.put("id",   track.getId());
            jsonTrack.put("name", track.getName());

            // Перебираем все путевые точки маршрута, если они есть.
            if (track.getPoints_list() != null && !track.getPoints_list().isEmpty()){

                List<LocationPoint> points = track.getPoints_list();

                for (LocationPoint point: points) {


                    JSONObject jsonPoint = new JSONObject();

                    jsonPoint.put("id",         point.getId());
                    jsonPoint.put("track_id",   point.getTrack_id());
                    jsonPoint.put("name",       point.getName());
                    jsonPoint.put("latitude",   point.getLatitude());
                    jsonPoint.put("longitude",  point.getLongitude());
                    jsonPoint.put("tolerance",  point.getTolerance());
                    jsonPoint.put("active",     point.getActive());

                    jsonPoints2.put(jsonPoint);
                }

            }

            jsonTrack.put("points", jsonPoints2);

            jsonRequest.put("track", jsonTrack);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonRequestT(
                context,
                requestQueue,
                Settings.UPLOAD_SCRIPT_DEFAULT_ADDRESS,
                jsonRequest,
                params,
                callback,
                null);
    }


    /* @Brief: Шаблон для запроса на удаленный сервер с использованием библиотеки Volley.
     *         Параметры передаются методом POST в виде json-массива.
     *         Ответ от сервера принимается в виде json-массива.
     */
    private static void jsonRequestT(final Context context,
                                           RequestQueue requestQueue,
                                     final String url,
                                     final JSONObject jsonRequest,
                                     final Map<String, String> params,
                                     final LongOpCallback callback,
                                     final Object param) {

        // Ложим в запрос дополнительные параметры.
        try {
            // Добавляется стандартный параметр - ключ безопасности.
            jsonRequest.put("token", Settings.CLIENT_REQUEST_TOKEN);
            // Добавляеся уникальный идентификатор запроса для исключения дублирования записей в БД на стороне сервера.
            jsonRequest.put("request", getStrUid());

            // Добавляются прочие пользовательские параметры.
            for (String key: params.keySet()){
                String value = params.get(key);
                jsonRequest.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //JsonArrayRequest of volley.
        JsonObjectRequest arrayRequest = new JsonObjectRequest(
                JsonRequest.Method.POST,
                url,
                jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            if (callback != null) {callback.onSuccess(response, param);}
                        }else
                        if (callback != null) {callback.onError(null);}
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (callback != null) {callback.onError(error);}
                    }
                }) {
        };

        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(context);
            requestQueue.getCache().clear();
        }

        arrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                4000/*DefaultRetryPolicy.DEFAULT_TIMEOUT_MS*/,
                2/*DefaultRetryPolicy.DEFAULT_MAX_RETRIES*/,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding the json request to the queue.
        requestQueue.add(arrayRequest.setShouldCache(false));
        Log.i(TAG, "Performing script {"+url+"}");
    }
}
