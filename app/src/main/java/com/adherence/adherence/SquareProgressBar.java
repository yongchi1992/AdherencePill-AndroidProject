package com.adherence.adherence;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class SquareProgressBar extends ProgressBar{


    public SquareProgressBar(Context context) {
        super(context);
    }

    public SquareProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SquareProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = 0;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        // set the dimensions
//        if (widthWithoutPadding > heightWithoutPadding) {
//            size = heightWithoutPadding;
//        } else {
//            size = widthWithoutPadding;
//        }

        size = widthWithoutPadding;
        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
    }
}
