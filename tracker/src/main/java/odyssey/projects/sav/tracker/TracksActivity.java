package odyssey.projects.sav.tracker;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import odyssey.projects.sav.db.TracksView;


public class TracksActivity extends AppCompatActivity {

    private TracksView tracksView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        //Основная инициализация.
        mainInit();
    }

    // Основная инициализация.
    private void mainInit(){
        tracksView = new TracksView(this);
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
                                        tracksView.addTrack(trackName.getText().toString());
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
        if (tracksView != null) tracksView.doUpdate();
    }
}
