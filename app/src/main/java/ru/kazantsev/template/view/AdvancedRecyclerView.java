package ru.kazantsev.template.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import ru.kazantsev.template.adapter.MultiItemListAdapter;

/**
 * Created by 0shad on 14.05.2017.
 */
public class AdvancedRecyclerView extends RecyclerView {


    public AdvancedRecyclerView(Context context) {
        super(context);
    }

    public AdvancedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public int findFirstVisibleItemPosition(boolean completelyVisible) {
        final View child = findOneVisibleChild(0,   getLayoutManager().getChildCount(), completelyVisible, !completelyVisible);
        return child == null ? RecyclerView.NO_POSITION : getChildAdapterPosition(child);
    }

    public int findLastVisibleItemPosition(boolean completelyVisible) {
        final View child = findOneVisibleChild(  getLayoutManager().getChildCount() - 1, -1, completelyVisible, !completelyVisible);
        return child == null ? RecyclerView.NO_POSITION : getChildAdapterPosition(child);
    }

    public View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                                       boolean acceptPartiallyVisible) {
        OrientationHelper helper;
        if (  getLayoutManager().canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(getLayoutManager());
        } else {
            helper = OrientationHelper.createHorizontalHelper(getLayoutManager());
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child =   getLayoutManager().getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }

    public boolean isItemVisible(int index) {
        if(getAdapter() instanceof MultiItemListAdapter) {
            index += ((MultiItemListAdapter) getAdapter()).getFirstIsHeader();
        }
        int first = findFirstVisibleItemPosition(true);
        int last = findLastVisibleItemPosition(true);
        if (first > index || index > last) {
            return false;
        }
        return true;
    }

    public void smoothScrollToIndex(int position, int offset) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(getContext()) {

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                if(getLayoutManager() instanceof LinearLayoutManager) {
                    PointF calculate = ((LinearLayoutManager)getLayoutManager()).computeScrollVectorForPosition(targetPosition);
                    calculate.y += offset;
                    return calculate;
                } else {
                    return super.computeScrollVectorForPosition(targetPosition);
                }
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 25f / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, displayMetrics.densityDpi, displayMetrics);
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        getLayoutManager().startSmoothScroll(linearSmoothScroller);
    }

}
