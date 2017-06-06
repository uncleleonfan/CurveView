package com.leon.curveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leon on 2017/6/2.
 */

public class CurveView extends View {

    private static final String TAG = "CurveView";

    private final Paint mAxisPaint;
    private final int mGap;
    private final int mRadius;
    private Path mCurvePath;
    private Paint mPaint;
    private Paint mControlPaint;
    private int[] mDataList;
    private int mMax;
    private String[] mHorizontalAxis;

    private int mNormalDotColor = Color.BLACK;


    private List<Dot> mDots = new ArrayList<Dot>();
    private List<Dot> mControlDotsList = new ArrayList<Dot>();

    private Rect mTextRect;
    private int mStep;
    private Paint mDotPaint;

    private float smoothnessRatio = 0.16f;

    private float[][] mControlDots = new float[2][2];


    public CurveView(Context context) {
        this(context, null);
    }

    public CurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.BLUE);

        mControlPaint = new Paint();
        mControlPaint.setAntiAlias(true);
        mControlPaint.setColor(Color.GREEN);

        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);
        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        mGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mTextRect = new Rect();

        mCurvePath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDots.clear();
        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();
        mStep = width / (mDataList.length - 1);


        mAxisPaint.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length(), mTextRect);
        int barHeight = height - mTextRect.height() - mGap;
        float heightRatio = barHeight / mMax;

        for (int i = 0; i < mDataList.length; i++) {

            Dot dot = new Dot();
            dot.value = mDataList[i];
            dot.transformedValue = dot.value * heightRatio;

            dot.x = mStep * i + getPaddingLeft();
            dot.y = getPaddingTop() + barHeight - dot.transformedValue;

            mDots.add(dot);
        }

        for (int i = 0; i < mDataList.length - 1; i++) {

            if (i == 0) {
                mCurvePath.moveTo(mDots.get(0).x, mDots.get(0).y);
            }

            calculateControlPoints(i);

            mCurvePath.cubicTo(mControlDots[0][0], mControlDots[0][1],
                    mControlDots[1][0], mControlDots[1][1],
                    mDots.get(i + 1).x, mDots.get(i + 1).y);

        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mCurvePath, mPaint);
        for (int i = 0; i < mDots.size(); i++) {
            String axis = mHorizontalAxis[i];
            int x = getPaddingLeft() + i * mStep;
            int y = getHeight() - getPaddingBottom();
            canvas.drawText(axis, x, y, mAxisPaint);
            Dot dot = mDots.get(i);
            mDotPaint.setColor(mNormalDotColor);
            canvas.drawCircle(dot.x, dot.y, mRadius, mDotPaint);
        }

        for (int i = 0; i < mControlDotsList.size(); i++) {
            Dot dot = mControlDotsList.get(i);
            canvas.drawCircle(dot.x, dot.y, mRadius, mControlPaint);
            Log.d(TAG, "draw controll: ");
        }
    }

    public void setDataList(int[] dataList, int max) {
        mDataList = dataList;
        mMax = max;
    }

    public void setHorizontalAxis(String[] horizontalAxis) {
        mHorizontalAxis = horizontalAxis;
    }

    public void calculateControlPoints(int i) {
        float x1 = Float.NaN;
        float y1 = Float.NaN;
        float x2 = Float.NaN;
        float y2 = Float.NaN;

        Dot currentDot = mDots.get(i);
        Dot nextDot = null;
        Dot previousDot = null;
        Dot nextNextDot = null;


        if (i > 0) {
            previousDot = mDots.get(i - 1);
        } else {
            previousDot = currentDot;
        }


        if (i < mDots.size() -1) {
            nextDot = mDots.get(i + 1);
        } else {
            nextDot = currentDot;
        }


        if (i < mDots.size() - 2) {
            nextNextDot = mDots.get(i + 2);
        } else {
            nextNextDot = nextDot;
        }


        Log.d(TAG, "calculateControlPoints: " + previousDot.x  + " " + currentDot.x + "  " + nextDot.x + "  " + nextNextDot.x);
        x1 = currentDot.x + smoothnessRatio * (nextDot.x - previousDot.x);
        y1 = currentDot.y + smoothnessRatio * (nextDot.y - previousDot.y);

        x2 = nextDot.x - smoothnessRatio * (nextNextDot.x - currentDot.x);
        y2 = nextDot.y - smoothnessRatio * (nextNextDot.y - currentDot.y);

        Log.d(TAG, "calculateControlPoints: " + x1 + " " + y1 + " " + x2 + " " + y2);

        mControlDots[0][0] = x1;
        mControlDots[0][1] = y1;
        mControlDots[1][0] = x2;
        mControlDots[1][1] = y2;



        Dot firstControl = new Dot();
        firstControl.x = x1;
        firstControl.y = y1;

        Dot secondControl = new Dot();
        secondControl.x = x2;
        secondControl.y = y2;

        mControlDotsList.add(firstControl);
        mControlDotsList.add(secondControl);
    }

    private class Dot {
        float x = Float.NaN;
        float y = Float.NaN;
        int value;
        float transformedValue;
    }


}
