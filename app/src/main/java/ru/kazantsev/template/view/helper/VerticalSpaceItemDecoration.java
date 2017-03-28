package ru.kazantsev.template.view.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 0shad on 02.08.2016.
 */
public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mVerticalSpaceHeight;
    private final boolean lustItemSpace;

    public VerticalSpaceItemDecoration(int mVerticalSpaceHeight, boolean lastItemSpace) {
        this.mVerticalSpaceHeight = mVerticalSpaceHeight;
        this.lustItemSpace = lastItemSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        if (lustItemSpace || parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = mVerticalSpaceHeight;
        }
    }
}
