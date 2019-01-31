package odyssey.projects.sav.driver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import odyssey.projects.callbacks.LongOpCallback;
import odyssey.projects.pref.LocalSettings;

import static odyssey.projects.utils.UidUtils.getStrUid;

public class VolleyWrapper {

    private final static String TAG = "VOLLEY";

    /* @Brief: Регистрирует на сервере отметку для указанного гос. номера автомобиля.
     */
    public static void doMark(final Context context,
                              final RequestQueue requestQueue,
                              JSONObject jsonRequest,
                              final @NonNull String vehicle_id,
                              final LongOpCallback callback){
        // Здесь будут лежать все параметры с ключами.
        Map<String,String> params = new HashMap<>();
        // Добавляем параметры к запросу.
        params.put("vehicle",  vehicle_id); // Идентификационный гос. номер автомобиля.
        if (jsonRequest == null) jsonRequest = new JSONObject();
        jsonRequestT(
                context,
                requestQueue,
                LocalSettings.getInstance(context).getScriptUrl(Settings.MARK_SCRIPT),
                jsonRequest,
                params,
                callback,
                null);
    }


    /* @Brief: Шаблон для запроса на удаленный сервер с исползованием библиотеки Volley.
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
