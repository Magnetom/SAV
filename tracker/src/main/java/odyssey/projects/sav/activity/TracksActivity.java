package odyssey.projects.sav.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

import odyssey.projects.sav.db.TracksListView;


public class TracksActivity extends AppCompatActivity {

    private TracksListView tracksListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tracks);
        setContentView(R.layout.activity_tracks_new);
        // Основная инициализация.
        mainInit();
        // Настройка тулбара.
        setupToolbar();
    }

    private void setupToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    // Основная инициализация.
    private void mainInit(){
        tracksListView = new TracksListView(this);
        // Инициализируем слушателей
        initListeners();
    }

    /* Регистрация слушателей. */
    private void initListeners() {

        final Context context = this;

        // Кнопка - "ДОБАВИТЬ МАРШРУТ".
        View fButton = findViewById(R.id.addNewTrackButton);
        if (fButton != null) fButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = getLayoutInflater().inflate(R.layout.add_track_dialog, null);

                // Настраиваем диалоговое овно присвоения имени новому маршруту.
                new AlertDialog.Builder(context,R.style.AlertDialogTheme)

                        .setView(view)
                        .setIcon(R.drawable.track_red)
                        .setTitle("Новый маршрут")
                        //.setMessage("Введите имя для нового маршрута.")
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {

                                TextView trackName = view.findViewById(R.id.newTrackNameText);
                                if (trackName!=null) {
                                    if (!trackName.getText().toString().equals("")){
                                        tracksListView.addTrack(trackName.getText().toString());
                                        dialog.cancel();
                                    }
                                }
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (tracksListView != null) tracksListView.doUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Получим идентификатор выбранного пункта меню.
        int id = item.getItemId();

        // Операции для выбранного пункта меню.
        switch (id) {
            case R.id.action_settings:
                // Открываем окно с настройками.
                startActivityForResult(new Intent(this, LocalPrefActivity.class), 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
