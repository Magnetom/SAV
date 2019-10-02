package odyssey.projects.sav.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import odyssey.projects.sav.activity.R;
import odyssey.projects.sav.db.Db;
import odyssey.projects.sav.widget.advrecyclerview.draggable.DraggableItemAdapter;
import odyssey.projects.sav.widget.advrecyclerview.draggable.ItemDraggableRange;
import odyssey.projects.sav.widget.advrecyclerview.utils.AbstractDraggableItemCursorViewHolder;

public class PointsAdapter extends RecyclerViewCursorAdapter<PointsAdapter.PointsViewHolder>
                            implements DraggableItemAdapter<PointsAdapter.PointsViewHolder> {

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
    }

    /**
     * ViewHolder used to display a data.
     */
    //public class PointsViewHolder extends RecyclerViewCursorViewHolder
    //public class PointsViewHolder extends AbstractDraggableItemViewHolder {
    public class PointsViewHolder extends AbstractDraggableItemCursorViewHolder {

        final TextView mPointSequence;
        final TextView mPointName;
        final TextView mPointLatitude;
        final TextView mPointLongitude;
        final TextView mPointTolerance;

        View dragHandle;

        public PointsViewHolder(View view) {
            super(view);

            mPointSequence  = view.findViewById(R.id.itemPointSequenceView);
            mPointName      = view.findViewById(R.id.pointNameView);
            mPointLatitude  = view.findViewById(R.id.itemLatitudeView);
            mPointLongitude = view.findViewById(R.id.itemLongitudeView);
            mPointTolerance = view.findViewById(R.id.toleranceView);

            dragHandle = view.findViewById(R.id.drag_handle);
        }



        @Override
        public void bindCursor(Cursor cursor) {
            //mMovieName.setText(cursor.getString(NAME_INDEX));

            mPointSequence.setText( cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_SEQUENCE));
            mPointName.setText(     cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_POINT));
            mPointLatitude.setText( cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_LATITUDE));
            mPointLongitude.setText(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_LONGITUDE));
            mPointTolerance.setText(cursor.getString(Db.TABLE_POINTS_COLUMNS.ID_COLUMN_GPS_TOLERANCE));
        }

    }

    @Override
    public boolean onCheckCanStartDrag(PointsAdapter.PointsViewHolder holder, int position, int x, int y) {

        View itemView = holder.itemView;
        View dragHandle = holder.dragHandle;

        int handleWidth  = dragHandle.getWidth();
        int handleHeight = dragHandle.getHeight();
        int handleLeft   = dragHandle.getLeft();
        int handleTop    = dragHandle.getTop();

        /*
        return (x >= handleLeft) && (x < handleLeft + handleWidth) &&
                (y >= handleTop) && (y < handleTop + handleHeight);
          */
         return true;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        int ii = 0;
        // ToDo: реализовать код в базе данных по пермещению записи...
        // List<MyItem> items;
        // MyItem removed = items.remove(fromPosition);
        // items.add(toPosition, removed);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(PointsAdapter.PointsViewHolder holder, int position) {
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
