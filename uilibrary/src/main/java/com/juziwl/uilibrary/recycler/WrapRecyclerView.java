package com.juziwl.uilibrary.recycler;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;

/**
 * Created by LK on 2017/6/15.
 *
 * @author LK
 * @modify Neil
 * 可以添加header和footer的RecyclerView
 */
public class WrapRecyclerView extends RecyclerView {
    private ArrayList<View> mHeaderViewInfos = new ArrayList<>();
    private ArrayList<View> mFooterViewInfos = new ArrayList<>();
    private Adapter mAdapter;
    private Context mContext;

    private int touchSlop;
    private int INVALID_POINTER = -1;
    private int scrollPointerId = INVALID_POINTER;
    private int initialTouchX;
    private int initialTouchY;

    public WrapRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledEdgeSlop();
        mContext = context;
    }

    @Override
    public void setScrollingTouchSlop(int slopConstant) {
        super.setScrollingTouchSlop(slopConstant);
        ViewConfiguration vc = ViewConfiguration.get(mContext);
        switch (slopConstant) {
            case TOUCH_SLOP_DEFAULT:
                touchSlop = vc.getScaledTouchSlop();
                break;
            case TOUCH_SLOP_PAGING:
                touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(vc);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (e == null) {
            return false;
        }
        int action = MotionEventCompat.getActionMasked(e);
        int actionIndex = MotionEventCompat.getActionIndex(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                scrollPointerId = MotionEventCompat.getPointerId(e, 0);
                initialTouchX = Math.round(e.getX() + 0.5f);
                initialTouchY = Math.round(e.getY() + 0.5f);
                return super.onInterceptTouchEvent(e);
            case MotionEvent.ACTION_POINTER_DOWN:
                scrollPointerId = MotionEventCompat.getPointerId(e, actionIndex);
                initialTouchX = Math.round(MotionEventCompat.getX(e, actionIndex) + 0.5f);
                initialTouchY = Math.round(MotionEventCompat.getY(e, actionIndex) + 0.5f);
                return super.onInterceptTouchEvent(e);
            case MotionEvent.ACTION_MOVE:
                int index = MotionEventCompat.findPointerIndex(e, scrollPointerId);
                if (index < 0) {
                    return false;
                }
                int x = Math.round(MotionEventCompat.getX(e, index) + 0.5f);
                int y = Math.round(MotionEventCompat.getY(e, index) + 0.5f);
                if (getScrollState() != SCROLL_STATE_DRAGGING) {
                    int dx = x - initialTouchX;
                    int dy = y - initialTouchY;
                    boolean startScroll = false;
                    //将斜率添加进来，这样可以减少 startScroll 为true 的机会。这个机会就会给需要这个返回值
                    if (getLayoutManager().canScrollHorizontally() && Math.abs(dx) > touchSlop &&
                            (getLayoutManager().canScrollVertically() || Math.abs(dx) > Math.abs(dy))) {
                        startScroll = true;
                    }
                    if (getLayoutManager().canScrollVertically() && Math.abs(dy) > touchSlop &&
                            (getLayoutManager().canScrollHorizontally() || Math.abs(dy) > Math.abs(dx))) {
                        startScroll = true;
                    }
                    return startScroll && super.onInterceptTouchEvent(e);
                }
                return super.onInterceptTouchEvent(e);
            default:
                return super.onInterceptTouchEvent(e);
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
//        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public void addHeaderView(View v) {
        mHeaderViewInfos.add(v);

        if (mAdapter != null) {
            if (!(mAdapter instanceof HeaderViewRecyclerAdpater)) {
                mAdapter = new HeaderViewRecyclerAdpater(mHeaderViewInfos, mFooterViewInfos, mAdapter);
                super.setAdapter(mAdapter);
            } else {
                ((HeaderViewRecyclerAdpater) mAdapter).addHeaderView(v);
            }
        }
    }

    public void removeHeaderView(View v) {
        mHeaderViewInfos.remove(v);
        if (mAdapter != null && mAdapter instanceof HeaderViewRecyclerAdpater) {
            ((HeaderViewRecyclerAdpater) mAdapter).removeHeaderView(v);
        }
    }

    public void addFooterView(View v) {
        mFooterViewInfos.add(v);

        if (mAdapter != null) {
            if (!(mAdapter instanceof HeaderViewRecyclerAdpater)) {
                mAdapter = new HeaderViewRecyclerAdpater(mHeaderViewInfos, mFooterViewInfos, mAdapter);
                super.setAdapter(mAdapter);
            } else {
                ((HeaderViewRecyclerAdpater) mAdapter).addFooterView(v);
            }
        }
    }

    public void removeFooterView(View v) {
        mFooterViewInfos.remove(v);
        if (mAdapter != null && mAdapter instanceof HeaderViewRecyclerAdpater) {
            ((HeaderViewRecyclerAdpater) mAdapter).removeFooterView(v);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
            mAdapter = new HeaderViewRecyclerAdpater(mHeaderViewInfos, mFooterViewInfos, adapter);
            adapter.registerAdapterDataObserver(dataObserver);
        } else {
            mAdapter = adapter;
        }
        super.setAdapter(mAdapter);
//        dataObserver.onChanged();
    }

    private AdapterDataObserver dataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mAdapter.notifyItemRangeChanged(positionStart + mHeaderViewInfos.size(), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mAdapter.notifyItemRangeChanged(positionStart + mHeaderViewInfos.size(), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mAdapter.notifyItemRangeInserted(positionStart + mHeaderViewInfos.size(), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mAdapter.notifyItemRangeRemoved(fromPosition + mHeaderViewInfos.size(), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mAdapter.notifyItemRangeRemoved(positionStart + mHeaderViewInfos.size(), itemCount);
        }
    };

    public int getHeaderViewCount() {
        return mHeaderViewInfos.size();
    }

    public int getFooterViewCount() {
        return mFooterViewInfos.size();
    }
}
