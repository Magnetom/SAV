package odyssey.projects.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import odyssey.projects.db.Db;
import odyssey.projects.sav.driver.R;

public class MarksCursorAdapter extends SimpleCursorAdapter {

    public MarksCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView timestamp = view.findViewById(R.id.tvTimestamp);
        timestamp.setText(cursor.getString(Db.TABLE_MARKS_COLUMNS.ID_COLUMN_TIMESTAMP));

    }
}
