package com.qs.minigridv5.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.utilities.CameraPreview;
import com.qs.minigridv5.workers.WFileMover;


public class AClipCam extends CamActivity implements
        View.OnClickListener, WFileMover.FileMoveListener, View.OnTouchListener {

    public static final String KEY_PROJECT_ID    = "proj_id";
    public static final String KEY_SCENE_ID      = "scene_id";
    public static final String KEY_CLIP_FILENAME = "clip_filename";

    // Category Stuff
    private long    project_id;
    private Project project;
    private int     scene_id;
    private Scene   scene;
    private String  clipFileName;
    private int     elapsedRecordingTimeS = 0;
    private int     ms                    = 0;
    private int     secs                  = 0;
    private int     mins                  = 0;
    private int     maxRecordingTimeS;

    private String tempVideoFileName = C.TEMP_DIR + "/tmp." + C.VIDEO_EXTENSION;// only used when selfie category

    // Views
    private FrameLayout  cameraPreviewLayout;
    private ImageView    recordDot;
    private FrameLayout  recordBtn;
    private ImageView    closeBtn;
    private ProgressBar  recordProgress;
    private TextView     camTimerText;
    private TextView     remainingTimeText;
    private TextView     sceneVideoText;
    private LinearLayout sceneVideoTextContainer;
    private LinearLayout sceneMaximizer;

    private WFileMover fileMover;
    ProgressDialog fileMovingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_clip_cam);

        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        scene_id = getIntent().getIntExtra(KEY_SCENE_ID, -1);
        clipFileName = getIntent().getStringExtra(KEY_CLIP_FILENAME);
        maxRecordingTimeS = (int) Math.ceil((Helpers.calculateClipLength(clipFileName) / 1000f)) * 1000;

        shouldOpenFrontCam = false;

        project = myApp.getProjectForID(project_id);
        scene = project.movieTemplate.scenes.get(scene_id);

        cameraPreviewLayout = findViewById(R.id.a_clip_cam_preview);
        cameraPreviewLayout.addView(cameraPreview);

        closeBtn = findViewById(R.id.a_clip_cam_close);
        recordDot = findViewById(R.id.a_clip_cam_rec_dot);
        camTimerText = findViewById(R.id.a_clip_cam_timer);
        recordBtn = findViewById(R.id.a_clip_cam_record_btn);
        recordProgress = findViewById(R.id.a_clip_cam_progress);
        remainingTimeText = findViewById(R.id.a_clip_cam_remaining_time);
        sceneVideoText = findViewById(R.id.a_clip_cam_scene_hint_text);
        sceneVideoTextContainer = findViewById(R.id.a_clip_cam_scene_hint_container);
        focusCircle = findViewById(R.id.a_clip_cam_focus_circle);
        sceneMaximizer = findViewById(R.id.a_clip_cam_scene_hint_container_maximizer);


        closeBtn.setOnClickListener(this);
        recordBtn.setOnClickListener(this);
        recordDot.setVisibility(View.GONE);
        recordProgress.setProgress(0);
        recordProgress.setMax(maxRecordingTimeS);
        sceneVideoText.setText(scene.getHtmlVideoText());
        sceneVideoTextContainer.setOnClickListener(this);
        cameraPreviewLayout.setOnTouchListener(this);
        sceneMaximizer.setVisibility(View.INVISIBLE);
        sceneMaximizer.setOnClickListener(this);

        fileMovingProgressDialog = new ProgressDialog(this);
        fileMovingProgressDialog .setTitle("Saving...");
        fileMover = new WFileMover(tempVideoFileName, clipFileName, this);

        rRecordingTimer = new Runnable() {
            @Override
            public void run() {

                setElapsedTime(elapsedRecordingTimeS + TIMER_DELAY);
                recordingTimeHandler.postDelayed(this, TIMER_DELAY);

                final int clipCompleted = maxRecordingTimeS - elapsedRecordingTimeS;

                final int progressMade = (maxRecordingTimeS - clipCompleted);

                recordProgress.setProgress(progressMade);

                remainingTimeText.setText((clipCompleted) / 1000 + "s");

                recordDot.setVisibility((elapsedRecordingTimeS % 1000 == 0) ? View.VISIBLE : View.INVISIBLE);


                if (clipCompleted < 0) {
                    recordProgress.setProgress(maxRecordingTimeS);
                    remainingTimeText.setText("0s");
                    stopRecording();
                    fileMovingProgressDialog.show();
                    fileMover.execute();
                    updateCameraViews();
                }

            }
        };

    }

    @Override
    public void onBackPressed() {

        returnToEditActivity();

    }

    @Override
    public void onClick(View view) {

        if (view == closeBtn) {

            if (isRecording) {
                stopRecording();
            }
            fileMover.cancel(true);
            returnToEditActivity();
        }

        if (view == recordBtn) {
            if (!isRecording) {

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, THE_REQUEST_CODE);

                } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);

                } else {

                    prepareMediaRecorderAndStartRecording(tempVideoFileName);
                    updateCameraViews();

                }

            }
        }

        if (view == sceneMaximizer) {
            sceneVideoTextContainer.setVisibility(View.VISIBLE);
            sceneMaximizer.setVisibility(View.INVISIBLE);
        }

        if (view == sceneVideoTextContainer) {
            sceneVideoTextContainer.setVisibility(View.GONE);
            sceneMaximizer.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent me) {

        if (me.getActionMasked() == MotionEvent.ACTION_UP) {

            super.doTheAutoFocus(
                    me.getX(),
                    me.getY(),
                    cameraPreviewLayout.getWidth(),
                    cameraPreviewLayout.getHeight()
            );

        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        updateCameraViews();
        setElapsedTime(0);

    }

    @Override
    protected void onResume() {
        super.onResume();

        final int clipCompleted = (maxRecordingTimeS - elapsedRecordingTimeS);
        recordProgress.setProgress((maxRecordingTimeS - clipCompleted) * 100 / maxRecordingTimeS);
        remainingTimeText.setText((int) (clipCompleted / 1000f) + "s");

    }

    protected void stopRecording() {
        // stop recording and release camera
        try {
            super.stopRecording();

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            // inform the user that recording has stopped
            isRecording = false;
            recordingTimeHandler.removeCallbacks(rRecordingTimer);

        }
    }

    private void updateCameraViews() {

        if (isRecording) {
            recordDot.setVisibility(View.VISIBLE);
            sceneVideoTextContainer.setVisibility(View.GONE);
            recordBtn.setVisibility(View.GONE);
            sceneMaximizer.setVisibility(View.VISIBLE);

        } else {
            recordDot.setVisibility(View.INVISIBLE);
            sceneVideoTextContainer.setVisibility(View.VISIBLE);
            recordBtn.setVisibility(View.VISIBLE);
            sceneMaximizer.setVisibility(View.INVISIBLE);

        }

    }

    private void setElapsedTime(int time) {
        elapsedRecordingTimeS = time;

        if (time <= 0) {
            ms = 0;
            secs = 0;
            mins = 0;
            camTimerText.setText("00:00");
        } else {

            ms += 100;

            if (ms >= 1000) {
                secs++;
                ms = 0;
            }

            if (secs >= 60) {
                mins++;
                secs = 0;
            }

            String timerStr;

            if (mins < 1) {
                timerStr = ((secs < 10) ? "00:0" : "00:") + secs + "";
            } else {
                if (secs < 10) {
                    timerStr = mins + ":0" + secs;
                } else {
                    timerStr = mins + ":" + secs;
                }
            }
            camTimerText.setText(timerStr);

        }
    }

    /**
     * Camera and Media Recorder Stuff Below
     */

    @Override
    String getName() {
        return "AClipCam";
    }

    @Override
    public void onMovingComplete(boolean success) {

        fileMovingProgressDialog.dismiss();

        if (success) {
            returnToEditActivity();
        } else {
            Toast.makeText(this, "Failed to save the clip", Toast.LENGTH_SHORT).show();
        }

    }

    private void returnToEditActivity() {

        finish();

    }

}
