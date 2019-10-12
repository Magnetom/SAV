package odyssey.projects.sav.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import odyssey.projects.sav.activity.R;
import odyssey.projects.sav.db.Db;
import odyssey.projects.sav.db.OnPointsSwapCallback;
import odyssey.projects.sav.utils.DrawableUtils;
import odyssey.projects.sav.utils.ViewUtils;
import odyssey.projects.sav.widget.advrecyclerview.draggable.DraggableItemAdapter;
import odyssey.projects.sav.widget.advrecyclerview.draggable.DraggableItemState;
import odyssey.projects.sav.widget.advrecyclerview.draggable.ItemDraggableRange;
import odyssey.projects.sav.widget.advrecyclerview.utils.AbstractDraggableItemCursorViewHolder;

public class PointsAdapter extends RecyclerViewCursorAdapter<PointsAdapter.PointsViewHolder>
                            implements DraggableItemAdapter<PointsAdapter.PointsViewHolder> {

    OnPointsSwapCallback onPointsSwapCallback;

    public void setOnPointsSwapCallback (OnPointsSwapCallback callback){
        onPointsSwapCallback = callback;
    }

    @Override
    public long getItemId(int position) {
        return mCursorAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mCursorAdapter.getItemViewType(position);
    }

    /**
     * Constructor.
     * @param context The Context the Adapter is displayed in.
     */
    public PointsAdapter(Context context) {
        super(context);
        setHasStableIds(true);
        setupCursorAdapter(null, 0, R.layout.points_list_item, false);
    }

    /**
     * Returns the ViewHolder to use for this adapter.
     */
    @NonNull
    @Override
    public PointsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PointsViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    /**
     * Moves the Cursor of the CursorAdapter to the appropriate position and binds the view for
     * that item.
     */
    @Override
    public void onBindViewHolder(@NonNull PointsViewHolder holder, int position) {
        // Move cursor to this position
        mCursorAdapter.getCursor().moveToPosition(position);

        // Set the ViewHolder
        setViewHolder(holder);

        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());

        // Установка заднего фона для перемещаемого элемента.
        // set background resource (target view ID: container)
        final DraggableItemState dragState = holder.getDragState();

        if (dragState.isUpdated()) {
            int bgResId;

            if (dragState.isActive()) {
                bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.
                DrawableUtils.clearState(holder.mContainer.getForeground());
            } else if (dragState.isDragging()) {
                bgResId = R.drawable.bg_item_dragging_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }

    }

    /**
     * ViewHolder used to display a data.
     */
    //public class PointsViewHolder extends RecyclerViewCursorViewHolder
    //public class PointsViewHolder extends AbstractDraggableItemViewHolder {
    public class PointsViewHolder extends AbstractDraggableItemCursorViewHolder {

        // Элементы для работы AdvancedRecyclerView.
        FrameLayout mContainer;
        View mDragHandle;

        // Графические элементы пользователя.
        final TextView mPointSequence;
        final TextView mPointName;
        final TextView mPointLatitude;
        final TextView mPointLongitude;
        final TextView mPointTolerance;

        PointsViewHolder(View view) {
            super(view);

            // Получаем вспомогательные элементы для работы AdvancedRecyclerView.
            mContainer  = view.findViewById(R.id.container);
            mDragHandle = view.findViewById(R.id.drag_handle);

            // Заполняются графические элементы пользователя.
            mPointSequence  = view.findViewById(R.id.itemPointSequenceView);
            mPointName      = view.findViewById(R.id.pointNameView);
            mPointLatitude  = view.findViewById(R.id.itemLatitudeView);
            mPointLongitude = view.findViewById(R.id.itemLongitudeView);
            mPointTolerance = view.findViewById(R.id.toleranceView);
        }

        @Override
        public void bindCursor(Cursor cursor) {
            mPointSequence.setText( cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_SEQUENCE));
            //mPointSequence.setText( String.format(Locale.US,"%d", cursor.getPosition()+1));
            mPointName.setText(     cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_POINT));
            mPointLatitude.setText( cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_LATITUDE));
            mPointLongitude.setText(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_LONGITUDE));
            mPointTolerance.setText(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_TOLERANCE));
        }
    }

    @Override
    public boolean onCheckCanStartDrag(@NonNull PointsAdapter.PointsViewHolder holder, int position, int x, int y) {

        // Начинать Drag&Drop только в случае нажатия (короткого или длительного) на специальный элемент DragHandle (т.е. mDragHandle).
        // x, y --- relative from the itemView's top-left
        final View containerView  = holder.mContainer;
        final View dragHandleView = holder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (containerView.getTranslationX() + 0.5f);
        final int offsetY = containerView.getTop() + (int) (containerView.getTranslationY() + 0.5f);

        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);

        // Начинать Drag&Drop в любом случае, где бы ни было касание (или длительное нажатие) на элементе списка.
        //return true;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        int ii = 0;
        // ToDo: реализовать код в базе данных по пермещению записи...
        // List<MyItem> items;
        // MyItem removed = items.remove(fromPosition);
        // items.add(toPosition, removed);

        int point_id = mCursorAdapter.getCursor().getInt(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_ID);

        if (onPointsSwapCallback != null) onPointsSwapCallback.OnSwap(point_id, toPosition + 1);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(@NonNull PointsAdapter.PointsViewHolder holder, int position) {
        // just return null for default behavior
        return null;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        // this method is not used unless calling `RecyclerViewDragDropManager.setCheckCanDropEnabled(true)` explicitly.
        return true;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }
}
