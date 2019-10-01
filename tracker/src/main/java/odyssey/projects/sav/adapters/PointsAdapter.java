package odyssey.projects.sav.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import odyssey.projects.sav.activity.R;
import odyssey.projects.sav.db.Db;

public class PointsAdapter extends RecyclerViewCursorAdapter<PointsAdapter.PointsViewHolder> {

    /**
     * Constructor.
     * @param context The Context the Adapter is displayed in.
     */
    public PointsAdapter(Context context) {
        super(context);

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
    public class PointsViewHolder extends RecyclerViewCursorViewHolder {

        final TextView mPointSequence;
        final TextView mPointName;
        final TextView mPointLatitude;
        final TextView mPointLongitude;
        final TextView mPointTolerance;


        PointsViewHolder(View view) {
            super(view);

            mPointSequence  = view.findViewById(R.id.itemPointSequenceView);
            mPointName      = view.findViewById(R.id.pointNameView);
            mPointLatitude  = view.findViewById(R.id.itemLatitudeView);
            mPointLongitude = view.findViewById(R.id.itemLongitudeView);
            mPointTolerance = view.findViewById(R.id.toleranceView);
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
}
