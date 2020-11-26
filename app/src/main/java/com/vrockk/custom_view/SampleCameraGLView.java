package com.vrockk.custom_view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SampleCameraGLView extends GLSurfaceView implements View.OnTouchListener {

    public SampleCameraGLView(Context context) {
        this(context, null);
    }

    public SampleCameraGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    private TouchListener touchListener;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int actionMasked = event.getActionMasked();
        Log.e("touch" , "class "+event.getPointerCount());
        if (touchListener != null) {
            touchListener.onTouch(event.getPointerCount(), event, v.getWidth(), v.getHeight());
        }

        if (actionMasked != MotionEvent.ACTION_DOWN) {
            return true;
        }

        return true;
    }

    public interface TouchListener {
        void onTouch(int count ,MotionEvent event, int width, int height);
    }

    public void setTouchListener(TouchListener touchListener) {
        this.touchListener = touchListener;
    }
}

