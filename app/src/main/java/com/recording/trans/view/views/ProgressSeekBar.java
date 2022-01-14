package com.recording.trans.view.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/11/19 18:21
 */
public class ProgressSeekBar extends AppCompatSeekBar {
    /**
     * 是否支持拖动进度
     */
    private boolean touch = false;

    public ProgressSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
//        LogUtil.getLog().d("voice progressbar onDraw");
        super.onDraw(canvas);

    }

    public void setTouch(boolean touch) {
        this.touch = touch;
    }

    /**
     * onTouchEvent 是在 SeekBar 继承的抽象类 AbsSeekBar
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (touch) {
            return super.onTouchEvent(event);
        }
        return false;
    }
}
