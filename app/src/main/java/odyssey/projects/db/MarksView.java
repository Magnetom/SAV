package odyssey.projects.db;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import odyssey.projects.adapters.MarksCursorAdapter;
import odyssey.projects.callbacks.CallbacksProvider;
import odyssey.projects.pref.SettingsCache;
import odyssey.projects.sav.driver.R;

import static odyssey.projects.utils.DateTimeUtils.timestampToStringYYYYMMDD;

public class MarksView extends DbProc {

    private MarksCursorAdapter adapter;
    private ListView listView;

    public MarksView(Context context) {super(context);}

    @Override
    SimpleCursorAdapter getAdapter() {return adapter;}

    @Override
    void setupAdapter(Context context) {
        // формируем столбцы сопоставления
        String[] from = new String[] {
                Db.TABLE_MARKS_COLUMNS.COLUMN_ID,
                Db.TABLE_MARKS_COLUMNS.COLUMN_VEHICLE,
                Db.TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP};

        int[] to = new int[] { 0,       // [00] Db.COLUMN_ID
                0,                      // [01] Db.COLUMN_VEHICLE
                R.id.tvTimestamp};      // [02] Db.COLUMN_TIMESTAMP

        // создаем адаптер и настраиваем список
        adapter = new MarksCursorAdapter(context, R.layout.mark_item, null, from, to, 0);
    }

    @Override
    void setupListView() {
        listView = (((AppCompatActivity) context).findViewById(R.id.mainListView));
        if (listView == null) return;

        // Настраиваем view для случая пустого списка.
        ViewGroup parentGroup = (ViewGroup) listView.getParent();
        View emptyListView = ((AppCompatActivity) context).getLayoutInflater().inflate(R.layout.empty_list_layout, parentGroup, false);
        parentGroup.addView(emptyListView);
        listView.setEmptyView(emptyListView);

        listView.setDivider(context.getResources().getDrawable(android.R.color.transparent));

        // Присваиваем адаптер для виджета ListView
        listView.setAdapter(adapter);

        // Устанавливаем шапку списка.
        //View list_header = LayoutInflater.from(this.context).inflate(R.layout.mark_list_header, null);
        //listView.addHeaderView(list_header);
    }

    @Override
    CursorLoader initCursorLoader() {
        return new MyCursorLoader(this.context, this.db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
        // Отправялем в основное активити информацию о количестве элементов в списе (количество пройденных кругов).
        if(CallbacksProvider.getLoopsCountListener()!=null){
            CallbacksProvider.getLoopsCountListener().LoopsUpdated(listView.getAdapter().getCount());
        }
        // По окончанию обновления данных в ListView плавно перемещаемся в конец списка.
        if (listView != null) listView.smoothScrollToPosition(listView.getCount());
    }

    @Override
    void OnDestroy() {}

    private static class MyCursorLoader extends CursorLoader {
        Db db;
        MyCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
        }
        @Override
        public Cursor loadInBackground() {
            String current_vehicle = SettingsCache.VEHICLE;
            if (current_vehicle != null && (!current_vehicle.equals("")) ) return db.getAllMarks(current_vehicle, timestampToStringYYYYMMDD(System.currentTimeMillis()));
            else
                //return db.getAllMarks();
                return null;
        }
    }
}
