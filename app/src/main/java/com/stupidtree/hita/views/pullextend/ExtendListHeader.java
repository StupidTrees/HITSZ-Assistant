package com.stupidtree.hita.views.pullextend;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.stupidtree.hita.R;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;


/**
 * 这个类封装了下拉刷新的布局
 */
public class ExtendListHeader extends ExtendLayout {


    float containerHeight = dip2px(HContext, 120);
    float listHeight = dip2px(HContext, 240);
    boolean arrivedListHeight = false;
    boolean expanded = false;
    OnExpandListener onExpandListener;
    private RecyclerView mRecyclerView;
    /**
     * 原点
     */

    private ExpendPoint mExpendPoint;

    /**
     * 构造方法
     *
     * @param context context
     */
    public ExtendListHeader(Context context) {
        super(context);

    }

    /**
     * 构造方法
     *
     * @param context context
     * @param attrs   attrs
     */
    public ExtendListHeader(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public boolean isExpanded() {
        return expanded;
        //return mCurState==State.arrivedListHeight||mCurState==State.beyondListHeight;
    }

    public void setOnExpandListener(OnExpandListener onExpandListener) {
        this.onExpandListener = onExpandListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 在此处获取listHeight 和 containerHeight值
        listHeight = mRecyclerView == null ? dip2px(getContext(), 240) : mRecyclerView.getMeasuredHeight();
        containerHeight = listHeight / 2;
    }

    @Override
    protected void bindView(View container) {
        mRecyclerView = findViewById(R.id.list);
        mExpendPoint = findViewById(R.id.expend_point);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @SuppressLint("InflateParams")
    @Override
    protected View createLoadingView(Context context, AttributeSet attrs) {
        return LayoutInflater.from(context).inflate(R.layout.extend_header, null);
    }

    @Override
    public int getContentSize() {
        return (int) (containerHeight);
    }

    @Override
    public int getListSize() {
        return (int) (listHeight);
    }

    @Override
    protected void onReset() {
        mExpendPoint.setVisibility(VISIBLE);
        mExpendPoint.setAlpha(1);
        mExpendPoint.setTranslationY(0);
        mRecyclerView.setTranslationY(0);
        arrivedListHeight = false;
        expanded = false;
        if (onExpandListener != null) onExpandListener.onCollapse();
    }

    @Override
    protected void onReleaseToRefresh() {
    }

    @Override
    protected void onPullToRefresh() {

    }

    @Override
    protected void onArrivedListHeight() {
        arrivedListHeight = true;
    }

    @Override
    protected void onRefreshing() {
    }

    @Override
    public void onPull(int offset) {
        if (!arrivedListHeight) {
            mExpendPoint.setVisibility(VISIBLE);
            float percent = Math.abs(offset) / containerHeight;
            int moreOffset = Math.abs(offset) - (int) containerHeight;
            if (percent <= 1.0f) {
                if (expanded) {
                    if (onExpandListener != null) onExpandListener.onCollapse();
                    expanded = false;
                }
                mExpendPoint.setPercent(percent);
                mExpendPoint.setTranslationY((float) (-Math.abs(offset) / 2.0 + mExpendPoint.getHeight() / 2.0));
                mRecyclerView.setTranslationY(-containerHeight);
            } else {
                if (!expanded) {
                    if (onExpandListener != null) onExpandListener.onExpand();
                    performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    expanded = true;
                }
                float subPercent = (moreOffset) / (listHeight - containerHeight);
                subPercent = Math.min(1.0f, subPercent);
                mExpendPoint.setTranslationY((-(int) containerHeight >> 1) + (mExpendPoint.getHeight() >> 1) + (int) containerHeight * subPercent / 2);
                mExpendPoint.setPercent(1.0f);
                float alpha = (1 - subPercent * 2);
                mExpendPoint.setAlpha(Math.max(alpha, 0));
                mRecyclerView.setTranslationY(-(1 - subPercent) * containerHeight);
                for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
                    View v = mRecyclerView.getChildAt(i);
                    v.setScaleX((1 + subPercent) / 2);
                    v.setScaleY((1 + subPercent) / 2);
                }
            }
        }
        if (Math.abs(offset) >= listHeight) {
            mExpendPoint.setVisibility(INVISIBLE);
            mRecyclerView.setTranslationY(-(Math.abs(offset) - listHeight) / 2);
        }
    }


    public interface OnExpandListener {
        void onExpand();

        void onCollapse();
    }


}
