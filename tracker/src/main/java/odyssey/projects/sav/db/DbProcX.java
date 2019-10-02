package odyssey.projects.sav.db;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.Objects;

public abstract class DbProcX implements LoaderManager.LoaderCallbacks<Cursor>{

    protected final Context context;

    Db db;

    DbProcX(Context context) {
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

    //abstract RecyclerViewCursorAdapter<RecyclerViewCursorViewHolder> getAdapter();

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
        Objects.requireNonNull(((AppCompatActivity) context).getSupportLoaderManager().getLoader(0)).forceLoad();
    }

    public void doUpdate(){
        loadManagerForceLoad(context);
    }

    @Override
    @MainThread
    @NonNull
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return initCursorLoader();
    }

    @Override
    public abstract void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data);


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    abstract CursorLoader initCursorLoader();

    abstract void OnDestroy();

    /**********************************************************************
     *  API управления маршрутами.
     *********************************************************************/

    public void addTrack(String name){
        db.addTrack(name);
        this.loadManagerForceLoad(context);
    }

    void deleteTrack(Integer id){
        db.deleteTrack(id);
        this.loadManagerForceLoad(context);
    }

    void renameTrack(Integer id, String newName){
        db.renameTrack(id, newName);
        this.loadManagerForceLoad(context);
    }

    /**********************************************************************
     *  API управления точками маршрутов.
     *********************************************************************/

    public void addPoint(Long track_id, String point_name, String latitude, String longitude, String tolerance){
        db.addPoint(track_id, point_name, latitude, longitude, tolerance);
        this.loadManagerForceLoad(context);
    }

    public static boolean isPointSelected(Cursor cursor){
        if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0){
            return "1".equals(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_SELECTED));
        }
        return false;
    }

    public static LocationPointItem getPoint(Cursor cursor){
        LocationPointItem point = null;
        if (cursor != null && !cursor.isClosed() && cursor.getCount() > 0){
            //cursor.moveToFirst();

            point = new LocationPointItem();
            point.id        = cursor.getLong(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_ID);
            point.track_id  = cursor.getLong(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_TRACK_ID);
            point.sequence  = cursor.getLong(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_SEQUENCE);
            point.name      = cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_POINT);
            point.latitude  = cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_LATITUDE);
            point.longitude = cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_LONGITUDE);
            point.tolerance = cursor.getInt(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_TOLERANCE);
            point.active    = "1".equalsIgnoreCase(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_ACTIVE));
            point.selected  = "1".equalsIgnoreCase(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_SELECTED));
        }
        return point;
    }

    public LocationPointItem getPoint (Long point_id){
        return getPoint (db.getPoint(point_id));
    }

    void updatePoint(LocationPointItem newPointValues){
        db.updatePoint(newPointValues.id, newPointValues.name, newPointValues.latitude, newPointValues.longitude, newPointValues.tolerance, newPointValues.active);
        this.loadManagerForceLoad(context);
    }

    void deletePoint(Long point_id){
        db.deletePoint(point_id);
        this.loadManagerForceLoad(context);
    }

    // Получить полную информацию о маршруте.
    TrackItem getTrack (Long track_id){

        TrackItem track = null;
        Cursor track_cursor = db.getTrack(track_id);

        if ( (track_cursor != null) && (track_cursor.getCount() != 0)){
            track_cursor.moveToFirst();

            track = new TrackItem();

            track.id    = track_id;
            track.name  = track_cursor.getString(Db.TABLE_TRACKS_COLUMNS.ID_COLUMN_TRACK);

            Cursor points_cursor = db.getTrackPoints(track_id);
            if ( (points_cursor != null) && (points_cursor.getCount() != 0)){
                points_cursor.moveToFirst();

                for (int ii = 0; ii < points_cursor.getCount(); ii++ ){

                    track.points_list.add(getPoint(points_cursor));
                    // Перемещаем курсор на следующий объект.
                    points_cursor.moveToNext();
                }
            }
        }

        return track;
    }

    void selectPoint(Long point_id, boolean select){
        db.selectPoint(point_id, select);
        this.loadManagerForceLoad(context);
    }

    void togglePointSelection(Long point_id){
        db.togglePointSelection(point_id);
        this.loadManagerForceLoad(context);
    }

    void clearAllPointsSelection(){
        db.clearAllPointsSelection();
        this.loadManagerForceLoad(context);
    }

}
