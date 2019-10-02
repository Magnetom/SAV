package odyssey.projects.sav.widget.advrecyclerview.utils;

import android.database.Cursor;
import android.view.View;

import odyssey.projects.sav.adapters.RecyclerViewCursorViewHolder;
import odyssey.projects.sav.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import odyssey.projects.sav.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

public abstract class AbstractDraggableItemCursorViewHolder extends RecyclerViewCursorViewHolder implements DraggableItemViewHolder {

    @DraggableItemStateFlags
    private int mDragStateFlags;

    public AbstractDraggableItemCursorViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public abstract void bindCursor(Cursor cursor);

    @Override
    public void setDragStateFlags(@DraggableItemStateFlags int flags) {
        mDragStateFlags = flags;
    }

    @Override
    @DraggableItemStateFlags
    public int getDragStateFlags() {
        return mDragStateFlags;
    }
}
