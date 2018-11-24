package odyssey.projects.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import odyssey.projects.adapter.MarksCursorAdapter;
import odyssey.projects.sav.driver.R;

public final class DbProcessor implements LoaderManager.LoaderCallbacks<Cursor>{

    private final Context context;
    private static DbProcessor instance = null;

    private MarksCursorAdapter adapter = null;
    private Db db = null;

    private DbProcessor(Context context) {
        // Сохраняем ссылку на контекст.
        this.context = context;
        setupDb(context);
        // Настраиваем адаптер.
        setupAdapter(context);
        // Настраиваем виджет List View.
        setupListView();
        // Настраиваем Loader Manager.
        setupLoadManager(context);
        // Действия после инициализации экземпляра класса.
        onPostCreate();
    }

    private void onPostCreate(){
        /* */
    }

    public static DbProcessor getInstance(Context context){
        if (instance == null) return instance = new DbProcessor(context);
        return instance;
    }

    private void setupAdapter(Context context){
        // формируем столбцы сопоставления
        String[] from = new String[] {
                Db.TABLE_MARKS_COLUMNS.COLUMN_ID,
                Db.TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP};

        int[] to = new int[] { 0,                      // [00] Db.COLUMN_ID
                               R.id.tvTimestamp};      // [01] Db.COLUMN_TIMESTAMP

        // создаем адаптер и настраиваем список
        adapter = new MarksCursorAdapter(context, R.layout.mark_item, null, from, to, 0);
    }

    private void setupListView(){

        ListView listView = (ListView)   (((AppCompatActivity) context).findViewById(R.id.mainListView));
         // Присваиваем адаптер для виджета ListView
        listView.setAdapter(adapter);
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        db.close();
    }

    private void setupDb(Context context){
        this.db = new Db();
        this.db.open(context);
    }

    private void setupLoadManager(Context context){
        // создаем лоадер для чтения данных
        ((AppCompatActivity)context).getSupportLoaderManager().initLoader(0, null, this);
    }

    private void loadManagerForceLoad(Context context){
        // создаем лоадер для чтения данных
        ((AppCompatActivity)context).getSupportLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    @MainThread
    @NonNull
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(this.context,this.db);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int ii = 0;
    }

    private static class MyCursorLoader extends CursorLoader {
        Db db;
        MyCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
        }
        @Override
        public Cursor loadInBackground() {
            return db.getAllMarks();
        }
    }

    public void OnDestroy() {
        instance = null;
    }

    public void addMark(String timestamp){
        db.addMark(timestamp);
        this.loadManagerForceLoad(context);
    }

    public void clearTableMarks(){
        db.clearTableMarks();
        this.loadManagerForceLoad(context);
    }

    public Boolean deleteVehicle(String vehicle){
        Boolean result = db.deleteVehicle(vehicle);
        this.loadManagerForceLoad(context);
        return result;
    }

    public Boolean insertVehicle(String vehicle){
        Boolean result = false;
        try {
            result = db.insertVehicle(vehicle);
        } catch (SQLiteException e){
            return false;
        }
        this.loadManagerForceLoad(context);
        return result;
    }

    public ArrayList<String> getAllVehicles(){
        ArrayList<String> list = new ArrayList<String>();
        Cursor cursor;

        try {
            cursor = db.getAllVehicles();
        } catch (SQLiteException e){
            return null;
        }

        cursor.moveToFirst();

        for (int ii=0; ii<cursor.getCount();ii++,cursor.moveToNext()){
            list.add(cursor.getString(Db.TABLE_VEHICLES_COLUMNS.ID_COLUMN_VEHICLE));
        }

        return list;
    }
}
