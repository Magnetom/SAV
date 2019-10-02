package odyssey.projects.sav.test;

import android.database.Cursor;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder to be used with a RecyclerViewCursorAdapter.
 *
 * Created by adammcneilly on 12/8/15.
 */
public abstract class RecyclerViewCursorViewHolder extends RecyclerView.ViewHolder {

    /**
     * Constructor.
     * @param view The root view of the ViewHolder.
     */
    public RecyclerViewCursorViewHolder(View view) {
        super(view);
    }

    /**
     * Binds the information from a Cursor to the various UI elements of the ViewHolder.
     * @param cursor A Cursor representation of the data to be displayed.
     */
    public abstract void bindCursor(Cursor cursor);
}