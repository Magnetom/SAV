package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import odyssey.projects.db.Db;
import odyssey.projects.db.VehiclesViewer;
import odyssey.projects.intf.VehicleSelectedCallback;
import odyssey.projects.utils.hash;

public class VehicleSelectActivity extends AppCompatActivity {

    private VehiclesViewer vehiclesViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_select);

        final Context context = this;

        // Стрелка "НАЗАД"
        final ImageButton backButton = findViewById(R.id.backNarrowBtn);
        if (backButton != null){
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backButton.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    finish();
                }
            });
        }
        backButton.setHapticFeedbackEnabled(true);

        vehiclesViewer = new VehiclesViewer(this, new VehicleSelectedCallback() {
            @Override
            public void onSelected(String vehicle) {

                // Готовим данные для возврата их в родительскую активити.
                Intent intent = new Intent();
                intent.putExtra("VEHICLE", vehicle);
                setResult(RESULT_OK, intent);

                // Параллельно сохраняем  номер ТС в локальную БД для ведения статистики и
                // формирования списка всех ТС, используемых данным приложением.
                vehiclesViewer.insertVehicle(vehicle);

                // Завершаем текущую активити.
                finish();
            }
        });

        // Настраиваем кнопку ДОБАВИТЬ ГОСНОМЕР
        final View btn = findViewById(R.id.addNewVehicleBtn);
        if (btn != null){
            btn.setHapticFeedbackEnabled(true); // Поддержка обратной связи в виде вибрации от нажатия на элемент.
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                    View editLayout = getLayoutInflater().inflate(R.layout.new_vehicle_layout,null);

                    final TextView vid = editLayout.findViewById(R.id.vehicleNewIdView);

                    // Настраиваем диалоговое окно ввода госномера.
                    new AlertDialog.Builder(context)
                            .setView(editLayout)
                            .setPositiveButton("Готово", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!vid.getText().toString().equals("")){

                                        // Переводим госномер в верхний регистр.
                                        vid.setText(vid.getText().toString().toUpperCase());

                                        //////////////////////////////////////////////////////////////
                                        // РЕАЛИЗАЦИЯ СКРЫТЫХ СЕРВИСНЫХ ФУНКЦИ!
                                        // Вызов активити настроек!
                                        //if (vid.getText().toString().equalsIgnoreCase(hash.MD5("252a17de5554e541ea3056502c125f0b"))){
                                        if ( hash.MD5(vid.getText().toString()).equalsIgnoreCase("252a17de5554e541ea3056502c125f0b")){

                                            // Открываем окно с настройками.
                                            startActivityForResult(new Intent(context, LocalPrefActivity.class), 1);

                                            // Закрываем текущее диалоговое окно.
                                            dialog.cancel();
                                            // Завершаем текущую активити.
                                            finish();
                                            //return;
                                        }
                                        //////////////////////////////////////////////////////////////

                                        // Сохраняем выбранное ТС в локальную базу данных
                                        vehiclesViewer.insertVehicle(vid.getText().toString());

                                        // Готовим данные для возврата их в родительскую активити.
                                        Intent intent = new Intent();

                                        intent.putExtra("VEHICLE", vid.getText().toString());
                                        setResult(RESULT_OK, intent);

                                        // Закрываем текущее диалоговое окно.
                                        dialog.cancel();
                                        // Завершаем текущую активити.
                                        finish();
                                    }
                                }
                            })
                            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .create()
                            .show();
                }
            });
        }

    }
}
