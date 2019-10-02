package odyssey.projects.sav.widget.advrecyclerview.utils;

import android.database.Cursor;
import android.view.View;

import androidx.annotation.NonNull;

import odyssey.projects.sav.adapters.RecyclerViewCursorViewHolder;
import odyssey.projects.sav.widget.advrecyclerview.draggable.DraggableItemState;
import odyssey.projects.sav.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import odyssey.projects.sav.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

public abstract class AbstractDraggableItemCursorViewHolder extends RecyclerViewCursorViewHolder implements DraggableItemViewHolder {

    private final DraggableItemState mDragState = new DraggableItemState();

    public AbstractDraggableItemCursorViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDragStateFlags(@DraggableItemStateFlags int flags) {
        mDragState.setFlags(flags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DraggableItemStateFlags
    public int getDragStateFlags() {
        return mDragState.getFlags();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DraggableItemState getDragState() {
        return mDragState;
    }

    /**
     * Binds the information from a Cursor to the various UI elements of the ViewHolder.
     * @param cursor A Cursor representation of the data to be displayed.
     */
    public abstract void bindCursor(Cursor cursor);

}
