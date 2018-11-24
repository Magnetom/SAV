package odyssey.projects.sav.driver;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static odyssey.projects.utils.UidUtils.getStrUid;

public class VolleyWrapper {

    private final static String CRUD_TAG = "CRUD";

    /* @Brief: Регистрирует на сервере отметку для указанного гос. номера автомобиля.
     */
    public static void doMarkOld (final Context context, final RequestQueue requestQueue, String vehicle_id, final LongOpCallback callback){
        // Здесь будут лежать все параметры с ключами.
        Map<String,String> params = new HashMap<>();
        // Добавляем параметры к запросу.
        params.put("vehicle_id",  vehicle_id); // Идентификационный гос. номер автомобиля.
        stringRequestT(context, requestQueue, Settings.MARK_URL, params, callback, null);
    }

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
        params.put("vehicle_id",  vehicle_id); // Идентификационный гос. номер автомобиля.
        if (jsonRequest == null) jsonRequest = new JSONObject();
        jsonRequestT(context, requestQueue, Settings.MARK_URL, jsonRequest, params, callback, null);
    }


    /* @Brief: Шаблон для запроса на удаленный сервер с исползованием библиотеки Volley.
     *         Параметры передаются методом POST в виде json-массива.
     *         Ответ от сервера принимается в виде json-массива.
     */
    private static void jsonRequestT(final Context context,
                                     final RequestQueue requestQueue,
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
            jsonRequest.put("request_id", getStrUid());

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
        // Adding the json request to the queue.
        requestQueue.add(arrayRequest.setShouldCache(false));
    }



    /* @Brief: Используется для парсинга строкового ответа от сервера для определения результата
     *         текущего запроса: GENERAL_SUCCESS или GENERAL_ERROR.
     */
    private static boolean extractResultFromHostEcho(String hostEcho){
        String[] result = hostEcho.toString().split(" ");
        return result[0].startsWith(Settings.GENERAL_SUCCESS);
    }

    /* @Brief: Шаблон для запроса на удаленный сервер с исползованием библиотеки Volley.
     *         Параметры передаются методом POST в виде текстовых строк.
     *         Ответ от сервера принимается в виде текстовой строки.
     */
    private static void stringRequestT(final Context context, final RequestQueue requestQueue, final String url, final Map<String, String> params, final LongOpCallback callback, final Object param){

        //Creating a string request
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (callback != null){
                            if (extractResultFromHostEcho(response)) callback.onSuccess(response, param);
                            else
                                callback.onError(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                        if (error.getMessage() != null) Log.e(CRUD_TAG, "onErrorResponse: "+error.getMessage());
                        else Log.e(CRUD_TAG, "onErrorResponse without detailed message! Error class: "+ error);
                        if (callback != null) callback.onError(error);
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                params.put("token",Settings.CLIENT_REQUEST_TOKEN);
                return params;
            }
        };

        // Adding the string request to the queue.
        requestQueue.add(stringRequest.setShouldCache(false));
    }
}
