package odyssey.projects.sav.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import odyssey.projects.sav.db.OnModeChangedCallback;
import odyssey.projects.sav.db.PointsListView;
import odyssey.projects.sav.db.PointsListViewX;

public class PointsActivity extends AppCompatActivity {

    private PointsListView pointsListView;
    private PointsListViewX pointsListViewX;
    private LocationListener locationListener;

    private ConstraintLayout actionBarLayout;
    private View mainModeView;
    private View editModeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        // Получаем из интента номер трека, для котороо необхоимо отобразить список точек.
        long track = -1;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            track = b.getLong("track");
        }

        layoutInit();
        //Основная инициализация.
        mainInit(track);
    }

    private void layoutInit() {

        actionBarLayout = findViewById(R.id.actionBarLayout);

        if (actionBarLayout != null){

            mainModeView = getLayoutInflater().inflate(R.layout.activity_points_header_main,      actionBarLayout, false);
            editModeView = getLayoutInflater().inflate(R.layout.activity_points_header_item_edit, actionBarLayout, false);

            setMainMode();
        }
    }

    private void setMainMode(){
        if (actionBarLayout != null) {
            actionBarLayout.removeAllViews();
            if (mainModeView != null) actionBarLayout.addView(mainModeView);
        }
    }
    private void setItemEditMode(){
        if (actionBarLayout != null) {
            actionBarLayout.removeAllViews();
            if (editModeView != null) actionBarLayout.addView(editModeView);
        }
    }

    // Основная инициализация.
    private void mainInit(long track){
        if (track == -1) return;

        // Инициализация основного виджета.
        //pointsListView = new PointsListView(this, track);
        pointsListViewX = new PointsListViewX(this, track);

        // Инициализация заголовка активити: название просматриваемого/редактируемого маршрута.
        TextView currTrack = findViewById(R.id.currentTrackNameView);
        //if (currTrack != null && pointsListView != null) currTrack.setText(pointsListView.getTrackName(track));
        //if (currTrack != null) currTrack.setText(pointsListViewX.getTrackName(track));

        // Инициализируем слушателей
        initListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Запускается отображение текущих координат.
        initLocationVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Заканчиваем отображение текущих координат.
        removeLocationVisibility();
    }

    private void initLocationVisibility() {

        setLocationIsUnavailable(false);

        if (    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Разрешите использование определения точного местоположения!", Toast.LENGTH_LONG).show();
            return;
        }

        LocationManager locationManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationListener != null) locationManager.removeUpdates(locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, initLocationListener());
    }

    private void removeLocationVisibility(){
        if (locationListener != null){
            LocationManager locationManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    private LocationListener initLocationListener(){

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                setLocationValues(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                /**/
            }

            @Override
            public void onProviderEnabled(String provider) {
                setLocationIsUnavailable(true);
            }

            @Override
            public void onProviderDisabled(String provider) {
                setLocationIsUnavailable(false);
            }
        };
        return locationListener;
    }

    // Установить значения широты и долготы - "значения не доступны".
    private void setLocationIsUnavailable(final boolean isProviderEnabled){

        final TextView latitudeView  = findViewById(R.id.latitudeView);
        final TextView longitudeView = findViewById(R.id.longitudeView);

        if (latitudeView != null && longitudeView != null)
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                latitudeView.setText("0.000000");
                longitudeView.setText("0.000000");
                if (!isProviderEnabled) {
                    latitudeView.setTextColor(getResources().getColor(R.color.colorNoLocation));
                    longitudeView.setTextColor(getResources().getColor(R.color.colorNoLocation));
                } else {
                    latitudeView.setTextColor(getResources().getColor(R.color.colorTextNormal));
                    longitudeView.setTextColor(getResources().getColor(R.color.colorTextNormal));
                }
            }
        });
    }

    // Установить значения широты и долготы из параметра @location.
    private void setLocationValues(final android.location.Location location){

        final TextView latitudeView  = findViewById(R.id.latitudeView);
        final TextView longitudeView = findViewById(R.id.longitudeView);

        if (latitudeView != null && longitudeView != null)
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                latitudeView.setText (new DecimalFormat("#0.000000").format(location.getLatitude()));
                longitudeView.setText(new DecimalFormat("#0.000000").format(location.getLongitude()));

                latitudeView.setTextColor(getResources().getColor(R.color.colorTextNormal));
                longitudeView.setTextColor(getResources().getColor(R.color.colorTextNormal));
            }
        });
    }

    /* Регистрация слушателей. */
    private void initListeners() {
        final Context context = this;

        // Кнопка - "ДОБАВИТЬ ТОЧКУ К МАРШРУТУ".
        View fButton = findViewById(R.id.addNewPointButton);
        if (fButton != null) fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = getLayoutInflater().inflate(R.layout.add_point_dialog, null);

                final TextView latitudeView  = findViewById(R.id.latitudeView);
                final TextView longitudeView = findViewById(R.id.longitudeView);

                final TextView pointName        = view.findViewById(R.id.newPointNameText);
                final TextView pointLatitude    = view.findViewById(R.id.newPointLatitudeText);
                final TextView pointLongitude   = view.findViewById(R.id.newPointLongitudeText);
                final TextView pointTolerance   = view.findViewById(R.id.newPointToleranceText);

                pointLatitude.setText(latitudeView.getText());
                pointLongitude.setText(longitudeView.getText());
                pointTolerance.setText("50");

                // Настраиваем диалоговое овно присвоения имени новой точке.
                new AlertDialog.Builder(context,R.style.AlertDialogTheme)
                        .setView(view)
                        .setIcon(R.drawable.place_marker_32)
                        .setTitle("Новая точка маршрута")
                        //.setMessage("Введите имя для новой точки маршрута.")
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {

                                if (pointName.getText().toString().equals("")) pointName.setText("новая точка");

                                if (pointsListView != null)
                                pointsListView.addPoint(pointsListView.getTrack(),
                                        pointName.getText().toString(),
                                        pointLatitude.getText().toString(),
                                        pointLongitude.getText().toString(),
                                        pointTolerance.getText().toString());
                            }
                        })
                        .create()
                        .show();
            }
        });

        // Регистрация слушателя - изменение режима просмотра списка точек (просмотр/редактирование).
        if (pointsListView != null)
        pointsListView.setOnModeChangedCallback(new OnModeChangedCallback() {
            @Override
            public void editMode(boolean mode) {
                if (mode) setItemEditMode();
                else
                    setMainMode();
            }
        });
    }
}
