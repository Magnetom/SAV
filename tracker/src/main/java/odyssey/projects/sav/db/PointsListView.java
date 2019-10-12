package odyssey.projects.sav.db;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.NinePatchDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import odyssey.projects.sav.activity.R;
import odyssey.projects.sav.adapters.PointsAdapter;
import odyssey.projects.sav.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import odyssey.projects.sav.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

public class PointsListView extends DbProcX{

    private final String TAG = "POINTS_LIST_VIEW";

    private PointsAdapter adapter;

    private static long track = -1;

    public PointsListView(Context context, long track) { super(context); setTrack(track);}

    @Override
    void setupAdapter(Context context) {
        adapter = new PointsAdapter(context);

        // Коллбэк вызывается при необходимости поменять точки маршрута местами.
        adapter.setOnPointsSwapCallback(new OnPointsSwapCallback() {
            @Override
            public void OnSwap(int point_id, int to_pos) {
                db.movePoint(point_id, to_pos);
            }
        });
    }

    @Override
    void setupListView() {

        // Drag & Drop manager
        RecyclerViewDragDropManager dragDropManager = new RecyclerViewDragDropManager();
        dragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) context.getDrawable(R.drawable.material_shadow_z3));
        dragDropManager.setInitiateOnMove(false);
        dragDropManager.setInitiateOnLongPress(true);
        dragDropManager.setLongPressTimeout(750);

        // Adapter
        RecyclerView.Adapter wrappedAdapter = dragDropManager.createWrappedAdapter(adapter);

        // RecyclerView
        final RecyclerView recyclerView = ((AppCompatActivity) context).findViewById(R.id.pointsRecyclerView);
        if (recyclerView == null) return;

        /*
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        */
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Когда RecyclerView не планирует изменять размеры своих дочерних элементов динамически.
        // В результате recyclerView не будет перерисовываться каждый раз, когда в элементе списка
        // обновятся данные, этот элемент перерисуется самостоятельно.
        recyclerView.setHasFixedSize(true);

        // Декорирования элементов списка.
        //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        //recyclerView.addItemDecoration(new CharacterItemDecoration(10));
        recyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(context, R.drawable.list_divider_h), true));

        // Присваиваем адаптер для виджета RecyclerView
        recyclerView.setAdapter(wrappedAdapter);

        // disable change animations
        //((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        // Animation
        //final GeneralItemAnimator animator = new DraggableItemAnimator();
        //recyclerView.setItemAnimator(animator);

        dragDropManager.attachRecyclerView(recyclerView);


        Snackbar.make(recyclerView, "TIP: Long press item to initiate Drag & Drop action!", Snackbar.LENGTH_LONG).setDuration(2500).show();
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
