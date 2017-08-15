package com.example.gooview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.example.gooview.util.EvaluateUtil;
import com.example.gooview.util.GeometryUtil;
import com.example.gooview.util.Utils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class GooView extends View {

    private Paint mPaint;

    public GooView(Context context) {
        this(context, null);
    }

    public GooView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GooView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPath = new Path();
    }

    PointF mDragCenter = new PointF(300f, 300f); //拖拽小球圆心的初始位置
    float mDragRadius = 20f; //拖拽小球半径
    PointF[] mDragPoints = new PointF[] { new PointF(50f, 250f), new PointF(50f, 350f) };
    PointF[] mStickPoints = new PointF[] { new PointF(250f, 250f), new PointF(250f, 350f) };
    PointF mControlPoint = new PointF(150f, 300f);
    PointF mStickCenter = new PointF(300f, 300f); //外圆圆心位置
    float mStickRadius = 15f;
    private Path mPath;
    private int mStatusBarHeight;
    private int mFarthestDistance = 200; //外圆半径
    private boolean mIsOutOfRange;
    private boolean mIsDisappear;
    public interface OnReleaseListener {
        void onDisappear();
        void onReset(boolean isOutOfRange);
    }
    private OnReleaseListener mOnReleaseListener;

    public OnReleaseListener getOnReleaseListener() {
        return mOnReleaseListener;
    }

    public void setOnReleaseListener(OnReleaseListener onReleaseListener) {
        mOnReleaseListener = onReleaseListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 保存画布状态
        canvas.save();
        // 移动画布
        canvas.translate(0, -mStatusBarHeight);
        if (!mIsDisappear) {
            if (!mIsOutOfRange) {
                float tempStickRadius = updateStickRadius();
                // 画连接部分
                float dy = mStickCenter.y - mDragCenter.y;
                float dx = mStickCenter.x - mDragCenter.x;
                Double lineK = null;
                if (dx != 0f) {
                    // 圆心连线斜率
                    lineK = (double) (dy / dx);
                }
                Double lineK2 = null;
                // 计算与圆心连线垂直的直线的斜率
                if (lineK == null) {
                    lineK2 = 0.0;
                } else if (lineK == 0.0) {
                    lineK2 = null;
                } else {
                    lineK2 = -1.0 / lineK;
                }
                mStickPoints = GeometryUtil.getIntersectionPoints(mStickCenter, tempStickRadius,
                        lineK2);
                mDragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, mDragRadius, lineK2);
                mControlPoint = GeometryUtil.getMiddlePoint(mDragCenter, mStickCenter);
                // Path表示一条路径
                // 重置路径
                mPath.reset();
                // 移动到某个点
                mPath.moveTo(mStickPoints[0].x, mStickPoints[0].y);
                // 画二阶贝塞尔曲线
                mPath.quadTo(mControlPoint.x, mControlPoint.y, mDragPoints[0].x, mDragPoints[0].y);
                // 画线段
                mPath.lineTo(mDragPoints[1].x, mDragPoints[1].y);
                mPath.quadTo(mControlPoint.x, mControlPoint.y, mStickPoints[1].x, mStickPoints[1].y);
                // 封闭曲线
                mPath.close();
                canvas.drawPath(mPath, mPaint);
                // 画固定圆

                canvas.drawCircle(mStickCenter.x, mStickCenter.y, tempStickRadius, mPaint);
            }
            // 画拖拽圆
            canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, mPaint);
        }

        mPaint.setStyle(Style.STROKE);
        canvas.drawCircle(mStickCenter.x, mStickCenter.y, mFarthestDistance, mPaint);
        mPaint.setStyle(Style.FILL);
        // 恢复画布
        canvas.restore();
    }

    private float updateStickRadius() {
        // 14f, 8f
        float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
        distance = Math.min(distance, mFarthestDistance);
        float percent = distance * 1.0f / mFarthestDistance;
        return EvaluateUtil.evaluateFloat(percent, 14f, 8f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float rawX;
        float rawY;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mIsOutOfRange = false;
            mIsDisappear = false;
            rawX = event.getRawX();
            rawY = event.getRawY();
            updateDragCenter(rawX, rawY);
            break;
        case MotionEvent.ACTION_MOVE:
            rawX = event.getRawX();
            rawY = event.getRawY();
            float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
            if (distance > mFarthestDistance) {
                // 断开
                mIsOutOfRange = true;
                invalidate();
            }
            updateDragCenter(rawX, rawY);
            break;
        case MotionEvent.ACTION_UP:
            if (mIsOutOfRange) {
                // 拖拽过程超出范围
                float distanceUp = GeometryUtil
                        .getDistanceBetween2Points(mDragCenter, mStickCenter);
                if (distanceUp > mFarthestDistance) {
                    // 消失
                    mIsDisappear = true;
                    invalidate();
                    if(mOnReleaseListener != null) {
                        mOnReleaseListener.onDisappear();
                    }
                } else {
                    updateDragCenter(mStickCenter.x, mStickCenter.y);
                    if(mOnReleaseListener != null) {
                        mOnReleaseListener.onReset(mIsOutOfRange);
                    }
                }
            } else {
                // 拖拽过程没超出范围
                final PointF startPoint = new PointF(mDragCenter.x, mDragCenter.y);
                ValueAnimator animator = ValueAnimator.ofFloat(1.0f);
                animator.addUpdateListener(new AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        float percent = animator.getAnimatedFraction();
                        PointF pointByPercent = GeometryUtil.getPointByPercent(startPoint,
                                mStickCenter, percent);
                        updateDragCenter(pointByPercent.x, pointByPercent.y);
                    }
                });
                // BaseXxx, SimpleXxx
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if(mOnReleaseListener != null) {
                            mOnReleaseListener.onReset(mIsOutOfRange);
                        }
                    }
                });
                animator.setDuration(500);
                animator.setInterpolator(new OvershootInterpolator(4.0f));
                animator.start();
                
            }
            break;
        default:
            break;
        }
        return true;
    }

    private void updateDragCenter(float x, float y) {
        mDragCenter.set(x, y);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStatusBarHeight = Utils.getStatusBarHeight(this);
    }
}