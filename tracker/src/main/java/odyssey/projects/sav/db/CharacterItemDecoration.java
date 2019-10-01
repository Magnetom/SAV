package odyssey.projects.sav.db;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CharacterItemDecoration extends RecyclerView.ItemDecoration{

    private int offset;

    CharacterItemDecoration(int offset) { this.offset = offset; }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, View view, @NonNull RecyclerView parent, RecyclerView.State state){

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();

        outRect.top   = offset;
        outRect.right = offset;
        outRect.left  = offset/2;
    }
}
