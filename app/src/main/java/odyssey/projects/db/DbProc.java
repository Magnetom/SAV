package odyssey.projects.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;

public abstract class DbProc implements LoaderManager.LoaderCallbacks<Cursor>{

    protected final Context context;

    Db db;

    DbProc(Context context) {
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

    abstract SimpleCursorAdapter getAdapter();

    abstract void setupAdapter(Context context);

    abstract void setupListView();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        db.close();
    }

    private void setupDb(Context context){
        this.db = new Db(context);
    }

    private void setupLoadManager(Context context){
        // создаем лоадер для чтения данных
        ((AppCompatActivity)context).getSupportLoaderManager().initLoader(0, null, this);
    }

    private void loadManagerForceLoad(Context context){
        // создаем лоадер для чтения данных
        ((AppCompatActivity)context).getSupportLoaderManager().getLoader(0).forceLoad();
    }

    public void doUpdate(){
        loadManagerForceLoad(context);
    }

    @Override
    @MainThread
    @NonNull
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //return new MyCursorLoader(this.context,this.db);
        return initCursorLoader();
    }

    /*
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        this.getAdapter().swapCursor(data);
    }
    */
    @Override
    public abstract void onLoadFinished(Loader<Cursor> loader, Cursor data);


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    abstract CursorLoader initCursorLoader();

    abstract void OnDestroy();

    public void addMark(String vehicle, String timestamp){
        db.addMark(vehicle, timestamp);
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

    public boolean removeAllVehicles(){
        try {
            db.removeAllVehicles();
            this.loadManagerForceLoad(context);
            return true;
        } catch (SQLiteException e){
            return false;
        }
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
