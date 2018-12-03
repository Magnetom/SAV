package odyssey.projects.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import odyssey.projects.adapter.MarksCursorAdapter;
import odyssey.projects.pref.LocalSettings;
import odyssey.projects.sav.driver.MainActivity;
import odyssey.projects.sav.driver.R;

import static odyssey.projects.utils.DateTimeUtils.getTimeStamp;
import static odyssey.projects.utils.DateTimeUtils.timestampToStringYYYYMMDD;

public final class DbProcessor implements LoaderManager.LoaderCallbacks<Cursor>{

    private final Context context;
    private static Handler generalHandler = null;
    private static DbProcessor instance = null;

    private MarksCursorAdapter adapter = null;
    private Db db = null;

    private ListView listView;

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

    public static DbProcessor getInstance(Context context, Handler h){
        generalHandler = h;
        if (instance == null) return instance = new DbProcessor(context);
        return instance;
    }

    private void setupAdapter(Context context){
        // формируем столбцы сопоставления
        String[] from = new String[] {
                Db.TABLE_MARKS_COLUMNS.COLUMN_ID,
                Db.TABLE_MARKS_COLUMNS.COLUMN_VEHICLE,
                Db.TABLE_MARKS_COLUMNS.COLUMN_TIMESTAMP};

        int[] to = new int[] { 0,                      // [00] Db.COLUMN_ID
                               0,                      // [01] Db.COLUMN_VEHICLE
                               R.id.tvTimestamp};      // [02] Db.COLUMN_TIMESTAMP

        // создаем адаптер и настраиваем список
        adapter = new MarksCursorAdapter(context, R.layout.mark_item, null, from, to, 0);
    }

    private void setupListView(){

        listView = (ListView)   (((AppCompatActivity) context).findViewById(R.id.mainListView));

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

        // Отправялем в основное активити информацию о количестве элементов в списе (количество пройденных кругов).
        if (generalHandler != null)
            generalHandler.sendMessage(
                Message.obtain(
                        generalHandler, MainActivity.MSG_GEN_MARKS_CNT, listView.getAdapter().getCount(),0)
        );
        // По окончанию обновления данных в ListView плавно перемещаемся в конец списка.
        if (listView != null) listView.smoothScrollToPosition(listView.getCount());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private static class MyCursorLoader extends CursorLoader {
        Db db;
        MyCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
        }
        @Override
        public Cursor loadInBackground() {
            String current_vehicle = LocalSettings.getInstance(getContext()).getText(LocalSettings.SP_VEHICLE);
            if (current_vehicle != null && (!current_vehicle.equals("")) ) return db.getAllMarks(current_vehicle, timestampToStringYYYYMMDD(System.currentTimeMillis()));
            else
                //return db.getAllMarks();
                return null;
        }
    }

    public void OnDestroy() {
        instance = null;
    }

    public void addMark(String vehicle, String timestamp){
        db.addMark(vehicle, timestamp);
        this.loadManagerForceLoad(context);
    }

    public Boolean clearTableMarks(){
        try {
            db.clearTableMarks();
        } catch (SQLiteException e){
            return false;
        }
        this.loadManagerForceLoad(context);
        return true;
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

    public boolean removeAllVehicles(){
        try {
            db.removeAllVehicles();
        } catch (SQLiteException e){
            return false;
        }
        this.loadManagerForceLoad(context);
        return true;
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
