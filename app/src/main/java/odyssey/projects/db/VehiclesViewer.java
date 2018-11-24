package odyssey.projects.db;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import odyssey.projects.adapter.MarksCursorAdapter;
import odyssey.projects.adapter.VehiclesCursorAdapterNew;
import odyssey.projects.sav.driver.R;

public final class VehiclesViewer extends DbProc {

    private SimpleCursorAdapter adapter;

    public VehiclesViewer(Context context) {
        super(context);
    }

    /*
    public static Boolean isInstantiated (){
        return instance != null;
    }
    public static VehiclesViewer getInstance(Context context){
        if (instance == null) return instance = new VehiclesViewer(context);
        return instance;
    }
    */

    @Override
    SimpleCursorAdapter getAdapter() {
        return adapter;
    }

    @Override
    void setupAdapter(Context context) {
        // формируем столбцы сопоставления
        String[] from = new String[] {
                Db.TABLE_VEHICLES_COLUMNS.COLUMN_ID,
                Db.TABLE_VEHICLES_COLUMNS.COLUMN_VEHICLE,
                Db.TABLE_VEHICLES_COLUMNS.COLUMN_POPULARITY};

        int[] to = new int[] {
                0,                    // [00] Db.COLUMN_ID
                R.id.vehicleItemView, // [01] Db.COLUMN_VEHICLE
                0};                   // [02] Db.COLUMN_POPULARITY

        // создаем адаптер и настраиваем список
        adapter = new VehiclesCursorAdapterNew(context, R.layout.vehicles_list_item, null, from, to, 0);
    }

    @Override
    void setupListView() {
        ListView listView = (ListView) (((AppCompatActivity) context).findViewById(R.id.vehiclesIdList));
        if (listView == null) return;

        // Настраиваем view для случая пустого списка.
        ViewGroup parentGroup = (ViewGroup) listView.getParent();
        View emptyListView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.empty_list_layout, parentGroup, false);
        parentGroup.addView(emptyListView);
        listView.setEmptyView(emptyListView);

        // Присваиваем адаптер для виджета ListView
        listView.setHeaderDividersEnabled(true);
        listView.setAdapter(adapter);
        listView.setDivider(context.getResources().getDrawable(android.R.color.transparent));

        // Включаем возможность длительной кликабельности.
        listView.setLongClickable(true);

        // Настраиваем слушателя выбора строки.
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView vehicle = (TextView)view.findViewById(R.id.vehicleItemView);

                /*
                // Готовим данные для возврата их в родительскую активити.
                Intent intent = new Intent();
                intent.putExtra("VEHICLE", vehicle.getText().toString());
                setResult(RESULT_OK, intent);

                // Завершаем текущую активити.
                finish();
                */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final TextView vehicle = (TextView)view.findViewById(R.id.vehicleItemView);
                if (vehicle == null || (vehicle.getText().length() == 0)) return true;

                // Настраиваем диалоговое окно "Удалить госномер?".
                new AlertDialog.Builder(context)
                        .setTitle("Удаление из списка")
                        .setMessage("Вы действительно хотите удалить госномер "+vehicle.getText()+"?")
                        .setIcon(R.drawable.error_outline_red_48x48)
                        .setPositiveButton("УДАЛИТЬ", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int which) {
                                // Удаляем из БД указанный госномер.
                                Boolean result = deleteVehicle(vehicle.getText().toString());
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
    }

    @Override
    CursorLoader initCursorLoader() {
        return new VehiclesCursorLoader(this.context,this.db);
    }

    @Override
    void OnDestroy() {
        //instance = null;
    }

    private static class VehiclesCursorLoader extends CursorLoader {
        Db db;
        VehiclesCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
        }
        @Override
        public Cursor loadInBackground() {
            return db.getAllVehicles();
        }
    }
}
