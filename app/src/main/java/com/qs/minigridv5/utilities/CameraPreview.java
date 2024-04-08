package com.qs.minigridv5.utilities;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.C;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Camera        camera;
    private Camera.Size   previewSize;


    public CameraPreview(Context context) {
        super(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.d(C.T, "camera preview surface created");

        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(C.T, "Error setting camera preview: " + e.getMessage());
            Crashlytics.logException(e);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.d(C.T, "camera preview surface changed: " + width + " x " + height);

        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();

            // set preview size and make any resize
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            parameters.setRecordingHint(false);
            camera.setParameters(parameters);

            // start preview with new settings 

            camera.setPreviewDisplay(holder);
            camera.startPreview();

        } catch (Exception e) {
            Log.d(C.T, "Error starting camera preview: " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(C.T, "camera preview surface destroyed");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(C.T, "camera preview measured");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width  = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        previewSize = getBestSize(width, height);

    }

    private Camera.Size getBestSize(final int screenW, final int screenH) {

        if (camera == null) return null;

        final List<Camera.Size> sizes            = camera.getParameters().getSupportedPreviewSizes();
        final float             ASPECT_TOLERANCE = 0.1f;
        float                   targetRatio      = (float) screenH / screenW;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double      minDiff     = Double.MAX_VALUE;

        int targetHeight = screenH;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;

    }

    public void setCam(Camera cam) {
        this.camera = cam;
        this.holder = getHolder();
        this.holder.addCallback(this);
    }
}
