package com.ticauto.home.ui.view;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.mobvoi.android.common.utils.LogUtil;


/**
 * @user lydon
 * @date 3/11/18.
 */
public class SweepView extends ViewGroup {
    private static final String TAG = "SweepView";
    private int downX, moveX, moved;
    private Scroller scroller = new Scroller(getContext());
    VelocityTracker mVelocityTracker = null;
    float xVelocity, yVelocity;
    /* Content View */
    private View mContentView;

    private static final int MINIMUM_DISTANCE = 5;
    private static final int X_VELOCITY_THRESHOLD = 1100;


    public SweepView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public SweepView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SweepView(Context context) {
        this(context, null);
    }

    /* When XML is loaded, it is generally getChildAt or initialization member */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        View child = getChildAt(0);
        int margin =
                ((MarginLayoutParams) child.getLayoutParams()).topMargin +
                        ((MarginLayoutParams) child.getLayoutParams()).bottomMargin;
        setMeasuredDimension(width, getChildAt(0).getMeasuredHeight() + margin);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (i == 0) {
                child.layout(l, t, r, b);
            } else if (i == 1) {
                child.layout(r, t, r + child.getMeasuredWidth(), b);
            }
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);

                downX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                xVelocity = mVelocityTracker.getXVelocity(pointerId);
                LogUtil.d(TAG, ">>>>>>>ACTION_MOVE xVelocity " + xVelocity);
                if (xVelocity >= X_VELOCITY_THRESHOLD) {
                    LogUtil.d(TAG, ">>>>>>>ACTION_MOVE X_VELOCITY_THRESHOLD " + xVelocity);
                    smoothScrollTo(getChildAt(0).getMeasuredWidth(), 0);
                    mListener.onCardDelete();
                } else {
                    int moved = (int) (event.getRawX() - downX);
                    scrollTo(-moved, 0);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                LogUtil.d(TAG, ">>>>>>> ACTION_CANCEL ");
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.d(TAG, ">>>>>>> ACTION_UP ");
//                mVelocityTracker.recycle();
//                mVelocityTracker = null;
                break;

        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    //缓慢滚动到指定位置
    private void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        //1000ms内滑动destX，效果就是慢慢滑动
        scroller.startScroll(scrollX, 0, delta, 0, 100);
        invalidate();
    }

    public void closeInternel() {
        // Layout content area
        mContentView.layout(0, 0, mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
    }

    private class MoveDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dy, float dx) {
            return Math.abs(dy) + Math.abs(dx) > MINIMUM_DISTANCE;
        }
    }

    private onCardDeleteListener mListener;

    public void setOnMediaCardDeleteListener(onCardDeleteListener listener) {
        this.mListener = listener;
    }

    public interface onCardDeleteListener {
        /**
         * card clip to delete
         */
        void onCardDelete();
    }

}