package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import odyssey.projects.callbacks.VehicleSelectedListener;
import odyssey.projects.db.VehiclesViewer;
import odyssey.projects.debug.DebugOut;
import odyssey.projects.utils.Hash;

public class VehicleSelectActivity extends AppCompatActivity {

    private static final String TAG = "VEHICLE_SELECT_ACTIVITY";

    private VehiclesViewer vehiclesViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_select);

        DebugOut.generalPrintInfo(getApplicationContext(), "Запущена активити выбора госномера", TAG);

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

        vehiclesViewer = new VehiclesViewer(this, new VehicleSelectedListener() {
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

                                        // Сохраняем выбранное ТС в локальную базу данных
                                        vehiclesViewer.insertVehicle(vid.getText().toString());

                                        // Готовим данные для возврата их в родительскую активити.
                                        setResult(RESULT_OK, new Intent().putExtra("VEHICLE", vid.getText().toString()));

                                        // Закрываем текущее диалоговое окно.
                                        dialog.cancel();
                                        // Завершаем текущую активити.
                                        finish();
                                    } else{
                                        // Если введен пустой номер, то запускаем диалоговое окно для ввода
                                        // пароля для запуска инженерного меню.

                                        final EditText input = new EditText(context);
                                        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                        input.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT));

                                        ConstraintLayout layout = new ConstraintLayout(context);
                                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
                                        layout.setLayoutParams(params);
                                        layout.setPadding(40,0,40,0);

                                        layout.addView(input);
                                        // Настраиваем диалог для ввода пароля.
                                        new AlertDialog.Builder(context)
                                                .setView(layout)
                                                .setTitle("Инженерное меню")
                                                .setMessage("Введите пароль доступа")
                                                .setPositiveButton("Готово", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //////////////////////////////////////////////////////////////
                                                            // РЕАЛИЗАЦИЯ СКРЫТЫХ СЕРВИСНЫХ ФУНКЦИ!
                                                            // Вызов активити настроек!
                                                            if ( Hash.MD5(input.getText().toString()).equalsIgnoreCase("252a17de5554e541ea3056502c125f0b")){

                                                                // Открываем окно с настройками.
                                                                startActivityForResult(new Intent(context, LocalPrefActivity.class), 1);

                                                                // Закрываем текущее диалоговое окно.
                                                                dialog.cancel();
                                                                // Завершаем текущую активити.
                                                                //finish();
                                                                return;
                                                            }
                                                            //////////////////////////////////////////////////////////////
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugOut.generalPrintInfo(getApplicationContext(), "Активити выбора госномера закрыто.", TAG);
    }
}
