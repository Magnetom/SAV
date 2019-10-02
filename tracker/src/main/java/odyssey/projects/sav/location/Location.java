package odyssey.projects.sav.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class Location {

    private static final String TAG = "LOCATION";

    private static final long MIN_TIME_BW_UPDATES = 1000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10.0f;

    private static boolean ENABLE_GPS_LOCATION_PROVIDER = true;
    private static boolean ENABLE_NETWORK_LOCATION_PROVIDER = false;
    private static boolean ENABLE_PASSIVE_LOCATION_PROVIDER = false;

    private HandlerThread queueThreadHandler;
    private Handler queueHandler;

    private static boolean getLocation(final Context context, final LocationResult locResult) {

        // Current location
        android.location.Location loc = null;
        // Location manager.
        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // Getting GPS location status.
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // Getting network location status.
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        // Getting passive location status.
        boolean isPassiveEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled && !isPassiveEnabled) {
            Log.d(TAG, "Warning: can't determinate location: all location providers are disabled!");
            locResult.onAllProvidersDisabled();
            return false;
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled && ENABLE_GPS_LOCATION_PROVIDER) {
                loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(TAG, "GPS location is Enabled.");
            }
            if (isNetworkEnabled && (loc == null) && ENABLE_NETWORK_LOCATION_PROVIDER) {
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Log.d(TAG, "Network location is Enabled.");
            }
            if (isPassiveEnabled && (loc == null) && ENABLE_PASSIVE_LOCATION_PROVIDER) {
                loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                Log.d(TAG, "Passive location is Enabled");
            }
            if (loc == null) {
                Log.d(TAG, "Warning: all enabled location services returned NULL location.");
                locResult.onUnknownLocation();
                return false;
            } else {
                locResult.onSuccessResult(loc);
            }
        }
        return true;
    }

    public void getLocationPeriodically(final Context context, final LocationSuccessResult locResult) {

        queueThreadHandler = new HandlerThread("GET_LOCATION_THREAD", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        queueThreadHandler.setDaemon(true);
        // Запускаем поток.
        queueThreadHandler.start();
        // Настраиваем обработчик сообщений.
        queueHandler = new Handler(queueThreadHandler.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Если локация получена успешно, то запускаем периодические опросы текущего местоположения.
                if (getLocation(context, new LocationResult() {
                    @Override
                    public void onAllProvidersDisabled() {
                    }

                    @Override
                    public void onUnknownLocation() {
                    }

                    @Override
                    public void onSuccessResult(android.location.Location location) {
                        if (locResult != null) locResult.onSuccessResult(location);
                    }
                })) {
                    // Запускаем периодические опросы GPS модуля для определения координат.
                    sendMessageDelayed(Message.obtain(msg), 3000);
                }
            }
        };
        queueHandler.sendEmptyMessage(0);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (queueThreadHandler != null) queueThreadHandler.quit();
        if (queueHandler != null) queueHandler.removeCallbacksAndMessages(null);

        queueThreadHandler = null;
        queueHandler = null;
    }

    public void getLocationPeriodically2(final Context context) {

        queueThreadHandler = new HandlerThread("GET_LOCATION_THREAD", android.os.Process.THREAD_PRIORITY_FOREGROUND);
        queueThreadHandler.setDaemon(true);
        // Запускаем поток.
        queueThreadHandler.start();

        if (    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Location manager.
        LocationManager locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        //criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);

        //String locationProvider = locationManager.getBestProvider(criteria, true);
        //String locationProvider = LocationManager.NETWORK_PROVIDER;
        String locationProvider = LocationManager.GPS_PROVIDER;

        locationManager.requestLocationUpdates(locationProvider, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                int ii = 0;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                int ii = 0;
            }

            @Override
            public void onProviderEnabled(String provider) {
                int ii = 0;
            }

            @Override
            public void onProviderDisabled(String provider) {
                int ii = 0;
            }
        });
        //locationManager.removeUpdates();

        /*
        locationManager.requestLocationUpdates(1000, 10, criteria, new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                int ii = 0;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                int ii = 0;
            }

            @Override
            public void onProviderEnabled(String provider) {
                int ii = 0;
            }

            @Override
            public void onProviderDisabled(String provider) {
                int ii = 0;
            }
        }, queueThreadHandler.getLooper());
        */
    }
}
