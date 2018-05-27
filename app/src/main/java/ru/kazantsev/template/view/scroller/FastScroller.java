package ru.kazantsev.template.view.scroller;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import ru.kazantsev.template.R;
import xyz.danoz.recyclerviewfastscroller.AbsRecyclerViewFastScroller;
import xyz.danoz.recyclerviewfastscroller.RecyclerViewScroller;
import xyz.danoz.recyclerviewfastscroller.calculation.VerticalScrollBoundsProvider;
import xyz.danoz.recyclerviewfastscroller.calculation.position.VerticalScreenPositionCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.progress.TouchableScrollProgressCalculator;
import xyz.danoz.recyclerviewfastscroller.calculation.progress.VerticalScrollProgressCalculator;

/**
 * Created by 0shad on 14.05.2017.
 */
public class FastScroller extends AbsRecyclerViewFastScroller implements RecyclerViewScroller {

    @Nullable
    private VerticalScrollProgressCalculator mScrollProgressCalculator;
    @Nullable
    private VerticalScreenPositionCalculator mScreenPositionCalculator;

    public FastScroller(Context context) {
        this(context, null);
    }

    public FastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.vertical_recycler_fast_scroller_layout;
    }

    @Override
    @Nullable
    protected TouchableScrollProgressCalculator getScrollProgressCalculator() {
        return mScrollProgressCalculator;
    }

    @Override
    public void moveHandleToPosition(float scrollProgress) {
        if (mScreenPositionCalculator == null) {
            return;
        }
        mHandle.setY(mScreenPositionCalculator.getYPositionFromScrollProgress(scrollProgress));
    }

    protected void onCreateScrollProgressCalculator() {
        VerticalScrollBoundsProvider boundsProvider =
                new VerticalScrollBoundsProvider(mBar.getY(), mBar.getY() + mBar.getHeight() - mHandle.getHeight());
        mScrollProgressCalculator = new VerticalScrollProgressCalculator(boundsProvider) {

            float previousPosition = 0;

            @Override
            public float calculateScrollProgress(RecyclerView recyclerView) {
                LinearLayoutManager linearLayout = (LinearLayoutManager) recyclerView.getLayoutManager();
                int first = linearLayout.findFirstVisibleItemPosition();
                int last = linearLayout.findLastVisibleItemPosition();
                return (float) first / (recyclerView.getAdapter().getItemCount() - (last - first + 1));
            }
        };
        mScreenPositionCalculator = new VerticalScreenPositionCalculator(boundsProvider);
    }
}
