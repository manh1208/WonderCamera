package com.superapp.wondercamera.activity;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by ManhNV on 2/13/17.
 */

public class CameraViewCustom extends SurfaceView {

    public CameraViewCustom(Context context) {
        super(context);
    }

    public CameraViewCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraViewCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraViewCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float targetRatio =(float) 4/3;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int)(width*targetRatio);
        setMeasuredDimension(width,height);
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
