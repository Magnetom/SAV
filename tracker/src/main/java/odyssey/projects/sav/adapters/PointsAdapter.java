package odyssey.projects.sav.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;


public class PointsAdapter extends SimpleCursorAdapter {

    public PointsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view,context,cursor);

        /*
        if (view != null) {

            TextView itemLongitudeView = view.findViewById(R.id.itemLongitudeView);

            if (isPointSelected(cursor)) {
                //view.setBackgroundColor(view.getResources().getColor(R.color.colorSwipeMenuEditMode));
                //view.setBackgroundColor(Color.BLUE);
                itemLongitudeView.setBackgroundColor(Color.BLUE);
            } else {
                //view.setBackgroundColor(view.getResources().getColor(R.color.colorWhite));
                itemLongitudeView.setBackgroundColor(Color.WHITE);
            }
        }
        */
    }
}