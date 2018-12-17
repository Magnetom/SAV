package odyssey.projects.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import odyssey.projects.db.Db;
import odyssey.projects.sav.driver.R;

public class VehiclesCursorAdapterNew extends SimpleCursorAdapter {

    public VehiclesCursorAdapterNew(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView vehicleIdView = (TextView) view.findViewById(R.id.vehicleItemView);
        vehicleIdView.setText(cursor.getString(Db.TABLE_VEHICLES_COLUMNS.ID_COLUMN_VEHICLE));
    }
}
