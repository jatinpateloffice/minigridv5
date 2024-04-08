package com.qs.minigridv5.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.utilities.CameraPreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class CamActivity extends MyActivity {

    protected final int           THE_REQUEST_CODE     = 23745;
    protected       Camera        cam;
    protected       CameraPreview cameraPreview;
    protected       MediaRecorder mediaRecorder;
    protected       Handler       recordingTimeHandler = new Handler();
    protected       ImageView     focusCircle;
    protected       boolean       shouldOpenFrontCam   = false;
    protected       boolean       isRecording          = false;
    public          Runnable      rRecordingTimer;
    protected       int          TIMER_DELAY          = 100;

    ////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cameraPreview = new CameraPreview(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // if permission to camera is granted, only then access it otherwise ask for the permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cam = getCameraInstance(this, shouldOpenFrontCam);
            cameraPreview.setCam(cam);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, THE_REQUEST_CODE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        hideStatusBar();

        checkForSettings();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isRecording) {
            this.stopRecording();

        }

        if (rRecordingTimer != null) {
            recordingTimeHandler.removeCallbacks(rRecordingTimer);
        }
        releaseMediaRecorder();
        releaseCamera();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == THE_REQUEST_CODE) {

            for (int i = 0; i < permissions.length; i++) {

                if (permissions[i].equals(Manifest.permission.CAMERA)) {

                    // once camera permission is granted, ask for audio recording permisison if not granted
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        cam = getCameraInstance(this, shouldOpenFrontCam);
                        cameraPreview.setCam(cam);

                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);

                        }

                    } else {

                        Helpers.showAlert(
                                this,
                                false,
                                "Camera is required to record the video. Please grant the permission",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        ActivityCompat.requestPermissions(CamActivity.this, new String[]{Manifest.permission.CAMERA}, THE_REQUEST_CODE);
                                    }
                                });

                    }

                }

                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {

                    // once camera permission is granted, ask for audio recording permisison if not granted
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                        Helpers.showAlert(
                                this,
                                false,
                                "Microphone access is required to record the audio. Please grant the permission",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        ActivityCompat.requestPermissions(CamActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);
                                    }
                                });

                    }

                }

            }

        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean cameraPresent() {

        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

    }

    protected Camera getCameraInstance(Context context, final boolean frontCam) {

        if (cameraPresent()) {

            // this device has a camera
            Camera c = null;
            try {

                // get the camera
                if (frontCam) {
                    c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                // Camera is not available (in use or does not exist)
            }
            return c; // returns null if camera is unavailable
        } else {
            // no camera on this device
            Toast.makeText(context, "No Camera Found :(", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    protected void releaseCamera() {
        cameraPreview.setCam(null);
        if (cam != null) {
            cam.lock();
            cam.stopPreview();
            cam.release();
            cam = null;
        }
    }

    protected void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            if (cam != null)
                cam.lock();           // lock camera for later use
        }
    }

    protected CamcorderProfile getCamcorderProfile() {

        CamcorderProfile cp = CamcorderProfile.get(C.HD_RECORD_MODE ? CamcorderProfile.QUALITY_720P : CamcorderProfile.QUALITY_480P);

        if (C.TRY_WEBM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cp.fileFormat = MediaRecorder.OutputFormat.WEBM;
                cp.videoCodec = MediaRecorder.VideoEncoder.VP8;
                cp.audioCodec = MediaRecorder.AudioEncoder.VORBIS;
            } else {
                cp.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
                cp.videoCodec = MediaRecorder.VideoEncoder.H264;
                cp.audioCodec = MediaRecorder.AudioEncoder.AAC;
            }
        } else {
            cp.fileFormat = MediaRecorder.OutputFormat.MPEG_4;
            cp.videoCodec = MediaRecorder.VideoEncoder.H264;
            cp.audioCodec = MediaRecorder.AudioEncoder.AAC;
        }

        return cp;

    }

    void prepareMediaRecorderAndStartRecording(final String outputFile) {

        mediaRecorder = new MediaRecorder();

        // Unlock and set camera to MediaRecorder
        if (cam == null) return;
        cam.unlock();

        mediaRecorder.setCamera(cam);

        // Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Set a CamcorderProfile (requires API Level 8 or higher)
        final CamcorderProfile cp = getCamcorderProfile();

        Log.e(
                C.T, "Video Codec: " + cp.videoCodec + "\n" +
                        "Bit Rate: " + cp.videoBitRate + "\n" +
                        "Frame rate: " + cp.videoFrameRate + "\n" +
                        "Resolution: " + cp.videoFrameWidth + " x " + cp.videoFrameHeight
        );

        mediaRecorder.setProfile(cp);

        mediaRecorder.setOutputFile(outputFile);

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(C.T, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            Crashlytics.logException(e);
            releaseMediaRecorder();
            return;
        } catch (IOException e) {
            Log.d(C.T, "IOException preparing MediaRecorder: " + e.getMessage());
            Crashlytics.logException(e);
            releaseMediaRecorder();
            return;
        }
        mediaRecorder.start();

        // inform the user that recording has started
        isRecording = true;
        recordingTimeHandler.removeCallbacks(rRecordingTimer);
        recordingTimeHandler.postDelayed(rRecordingTimer, 500);// delay this to capture the last second

    }

    protected void stopRecording() {

        mediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object

        isRecording = false;

    }

    protected void doTheAutoFocus(final float x, final float y, final float previewW, final float previewH) {

        if (cam != null) {

            final Camera.Parameters params     = cam.getParameters();
            List<String>            focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            focusCircle.setVisibility(View.VISIBLE);
            focusCircle.setX(x - 100);
            focusCircle.setY(y - 100);

            if (params.getMaxNumFocusAreas() > 0) {
                final List<Camera.Area> focusAreas = new ArrayList<>();
                final Rect              fRect      = getFocusRect((int) x, (int) y, 200, previewW, previewH);
                focusAreas.add(new Camera.Area(fRect, 1000));
                params.setFocusAreas(focusAreas);
                params.setRecordingHint(false);

                try {
                    cam.setParameters(params);
                    cam.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            Log.e(C.T, "auto focus success: " + success);
                            focusCircle.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    Log.e(C.T, e.getMessage());
                    Crashlytics.logException(e);
                }


            }

        }


    }

    private Rect getFocusRect(final float x, final float y, final float focusSize, final float previewWidth, final float previewHeight) {

        final float rx = ((x / previewWidth) * 2000 - 1000);
        final float ry = ((y / previewHeight) * 2000 - 1000);

        final Rect rect = new Rect(
                (int) (rx),
                (int) (ry),
                (int) (rx + focusSize),
                (int) (ry + focusSize)
        );
        return rect;

    }

}
