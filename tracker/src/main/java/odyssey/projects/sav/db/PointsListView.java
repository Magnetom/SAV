package odyssey.projects.sav.db;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import odyssey.projects.sav.activity.R;
import odyssey.projects.sav.adapters.PointsAdapter;

public class PointsListView extends DbProcX{

    private final String TAG = "POINTS_LIST_VIEW";

    private PointsAdapter adapter;

    private static long track = -1;

    public PointsListView(Context context, long track) { super(context); setTrack(track);}

    @Override
    void setupAdapter(Context context) {
        adapter = new PointsAdapter(context);
    }

    @Override
    void setupListView() {
        final RecyclerView recyclerView = ((AppCompatActivity) context).findViewById(R.id.pointsRecyclerView);
        if (recyclerView == null) return;


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Когда RecyclerView не планирует изменять размеры своих дочерних элементов динамически.
        // В результате recyclerView не будет перерисовываться каждый раз, когда в элементе списка
        // обновятся данные, этот элемент перерисуется самостоятельно.
        recyclerView.setHasFixedSize(true);

        // Декорирования элементов списка.
        //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(new CharacterItemDecoration(10));

        // Присваиваем адаптер для виджета RecyclerView
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        this.adapter.swapCursor(data);
    }

    @Override
    CursorLoader initCursorLoader() {
        return new PointsListView.MyCursorLoader(this.context, this.db);
    }

    @Override
    void OnDestroy() { }

    private static class MyCursorLoader extends CursorLoader {
        Db db;
        MyCursorLoader(@NonNull Context context, Db db) {
            super(context);
            this.db = db;
            // Снимаем выделения со всех элементов списка, если таковые были.
            db.clearAllPointsSelection();
        }
        @Override
        public Cursor loadInBackground() {
            return  db.getTrackPoints(track);
        }
    }

    public long  getTrack() {return track;}
    private void setTrack(long track_id) { track = track_id; }

    public String getTrackName(Long track_id){
        return db.getTrackName(track_id);
    }
}
