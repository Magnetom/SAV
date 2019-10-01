package odyssey.projects.sav.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    List<Phone> phones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setInitialData();
        // Настройка RecyclerView.
        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        DataAdapter adapter = new DataAdapter(this, phones);
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    private void setInitialData(){

        phones.add(new Phone ("Huawei P10", "Huawei", R.drawable.ic_launcher_background));
        phones.add(new Phone ("Elite z3", "HP", R.drawable.ic_launcher_background));
        phones.add(new Phone ("Galaxy S8", "Samsung", R.drawable.ic_launcher_background));
        phones.add(new Phone ("LG G 5", "LG", R.drawable.ic_launcher_background));
    }
}
