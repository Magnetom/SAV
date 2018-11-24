package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import odyssey.projects.adapter.VehiclesCursorAdapter;
import odyssey.projects.db.DbProcessor;
import odyssey.projects.db.VehiclesViewer;
import odyssey.projects.pref.LocalSettings;

public class VehicleSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_select);

        final Context context = this;

        /*
        if (VehiclesViewer.isInstantiated()) VehiclesViewer.getInstance(this).doUpdate();
        else
        VehiclesViewer.getInstance(this);
        */
        VehiclesViewer viewer = new VehiclesViewer(this);

        /*
        Intent intent = getIntent();
        ArrayList<String> list = (ArrayList<String>)intent.getSerializableExtra("VEHICLES_LIST");

        // Получаем список.
        ListView vehiclesListView =  this.findViewById(R.id.vehiclesIdList);

        // Настраиваем view для случая пустого списка.
        ViewGroup parentGroup = (ViewGroup)vehiclesListView.getParent();
        View empty = getLayoutInflater().inflate(R.layout.empty_list_layout, parentGroup, false);
        parentGroup.addView(empty);
        vehiclesListView.setEmptyView(empty);

        // Настраиваем адаптер для списка.
        vehiclesListView.setHeaderDividersEnabled(true);
        final VehiclesCursorAdapter adapter = new VehiclesCursorAdapter(this, list);
        vehiclesListView.setAdapter(adapter);
        vehiclesListView.setDivider(getResources().getDrawable(android.R.color.transparent));

        // Включаем возможность длительной кликабельности.
        vehiclesListView.setLongClickable(true);

        // Настраиваем слушателя выбора строки.
        vehiclesListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView vehicle = (TextView)view.findViewById(R.id.vehicleIdView);

                // Готовим данные для возврата их в родительскую активити.
                Intent intent = new Intent();
                intent.putExtra("VEHICLE", vehicle.getText().toString());
                setResult(RESULT_OK, intent);

                // Завершаем текущую активити.
                finish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        vehiclesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final TextView vehicle = (TextView)view.findViewById(R.id.vehicleIdView);
                if (vehicle == null || (vehicle.getText().length() == 0)) return true;

                // Настраиваем диалоговое окно "Удалить госномер?".
                new AlertDialog.Builder(context)
                        .setTitle("Удаление из списка")
                        .setMessage("Вы действительно хотите удалить госномер "+vehicle.getText()+"?")
                        .setIcon(R.drawable.error_outline_red_48x48)
                        .setPositiveButton("УДАЛИТЬ", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                Boolean result = DbProcessor.getInstance(context).deleteVehicle(vehicle.getText().toString());
                            }
                        })
                        .setNegativeButton("ОТМЕНА", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        })
                        .create()
                        .show();
                return false;
            }
        });
        */

        // Настраиваем кнопку ДОБАВИТЬ ГОСНОМЕР
        View btn = findViewById(R.id.addNewVehicleBtn);
        if (btn != null)
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View editLayout = getLayoutInflater().inflate(R.layout.new_vehicle_layout,null);

                final TextView vid = editLayout.findViewById(R.id.vehicleNewIdView);

                // Настраиваем диалоговое окно ввода госномера.
                new AlertDialog.Builder(context)
                        .setView(editLayout)
                        .setPositiveButton("Готово", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                if (!vid.getText().toString().equals("")){

                                    // Готовим данные для возврата их в родительскую активити.
                                    Intent intent = new Intent();
                                    // Переводим госномер в верхний регистр.
                                    vid.setText(vid.getText().toString().toUpperCase());
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
