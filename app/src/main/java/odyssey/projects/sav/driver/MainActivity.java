/*
 * About: The System of Accounting of Vehicles (SAV).
 * Author: Odyssey
 * Date: 11.2018
 */

package odyssey.projects.sav.driver;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;

import java.util.ArrayList;

import odyssey.projects.db.DbProcessor;
import odyssey.projects.db.VehiclesViewer;
import odyssey.projects.pref.LocalSettings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Основная инициализация.
        mainInit();

        this.findViewById(R.id.MyTestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean result = DbProcessor.getInstance(getApplication()).insertVehicle("H750AM750");
                int ii = 0;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RemoteMarkManager.onDestroy();
    }

    // Основная инициализация.
    private void mainInit(){
        // Инициализация менеджера отметок.
        RemoteMarkManager.init(this);
        // Регистрируем обработчики событий от нажатия различных объектов View.
        setupOnClickListeners();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        String vehicle = data.getStringExtra("VEHICLE");
        if (vehicle != null && !vehicle.equals("")){
            // Сохраняем выбанное ТС в локальные настройки.
            LocalSettings.getInstance(this).saveText(LocalSettings.SP_VEHICLE, vehicle);

            // Параллельно сохраняем  номер ТС в локальную БД для ведения статистики и формирования списка всех ТС,
            // используемых данным приложением.
            //Boolean result = DbProcessor.getInstance(this).insertVehicle(vehicle);
            //Boolean result = VehiclesViewer.getInstance(this).insertVehicle(vehicle);

            // Запускаем менеджер управления отметками.
            RemoteMarkManager.reRun(this);
        }
    }

    // Инициализация слушателей на нажатие объектов.
    private void setupOnClickListeners(){

        final Context context = this;

        /* ТАБЛИЧКА С ГОСНОМЕРОМ */
        // Визуализируем табличку с госномером.
        ViewStub viewStub = (ViewStub) findViewById(R.id.emptyStub);
        viewStub.inflate();
        // Получаем ссылку на кнопку.
        View btn = findViewById(R.id.vehicleIdView);
        // Нажатие на кнопку ГОСНОМЕР.
        if (btn != null)
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VehicleSelectActivity.class);

                ArrayList<String> list = DbProcessor.getInstance(context).getAllVehicles();

                intent.putExtra("VEHICLES_LIST", list);
                startActivityForResult(intent, 1);
            }
        });

    }
}
