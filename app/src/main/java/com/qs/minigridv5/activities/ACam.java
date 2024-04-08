package com.qs.minigridv5.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.CameraPreview;
import com.qs.minigridv5.utilities.MyApplication;
import com.qs.minigridv5.utilities.SimpleMuxer;

import java.io.File;
import java.util.ArrayList;


public class ACam extends CamActivity implements View.OnClickListener, View.OnTouchListener {

    // intent parameters
    public static final String KEY_SHOW_PROJECT_PROGRESS = "show_project_progress";
    public static final String KEY_PROJECT_ID            = "proj_id";
    public static final String KEY_SCENE_ID              = "cat_id";

    // Category Stuff
    private long    project_id;
    private Project project;
    private int     scene_id;
    private Scene   scene;
    private long    maxSceneRecordingTimeMs;
    private int     elapsedRecordingTimeMS = 0;
    private int     ms                     = 0;
    private int     secs                   = 0;
    private int     mins                   = 0;

    private boolean selfieScene = false;

    // Camera & Recording stuff
    private boolean canStopRecording = true;
    private boolean allowFocusing    = true;

    private String tempVideoFileName = C.TEMP_DIR + "/tmp." + C.VIDEO_EXTENSION;// only used when selfie category

    private long MAX_PROGRESS_LIMIT;

    private boolean showProjectProgress = true;

    // Views
    private FrameLayout cameraPreviewLayout;
    private ImageView   recordDot;
    private ImageView   stopIcon, startIcon;
    private ImageView    clipPlaybackBtn;
    private ImageView    helpBtn;
    private FrameLayout  camRecordBtn;
    private FrameLayout  progressBarContainer;
    private ProgressBar  recordProgress;
    private TextView     camTimerText;
    private TextView     remainingTimeText;
    private TextView     sceneVideoText;
    private TextView     selfieSceneVideoText;
    private TextView     sceneNoText;
    private LinearLayout sceneVideoTextContainer;
    private LinearLayout selfieSceneVideoTextContainer;
    private LinearLayout sceneMaximizer;
    private SeekBar      projectProgressBar;

    // clip player stuff
    private FrameLayout clipPlayer;
    private VideoView   clipVideoView;
    private Button      clipSaveBtn, clipDeletBtn;

    private String lastSavedVideoFileName;

    private ArrayList<ImageView> checkPoints = new ArrayList<>();
    ArrayList<Integer> clipMarkers = new ArrayList<>();
    private int displayWidth   = 0;
    private int prevClipMargin = 0;

    // Congo stuff
    boolean        showFirstClipCongo;
    boolean        showFirstSceneCongo;
    boolean        showDikhaoCompleteCongo;
    RelativeLayout congo;
    TextView       congo_text;
    ImageView      congo_image;

    // Overlay stuff
    boolean     overlaySequenceActive = false;
    boolean     showViewClipOverlay;
    boolean     showOverviewOverlay;
    boolean     showDikhaoCamOverlay  = false;
    boolean     showBataoCamOverlay   = false;
    FrameLayout introOverlay;
    TextView    introOverlayHeader;
    TextView    introOverlayText;
    TextView    naiSamjaText;
    FrameLayout viewLastClipOverlay;
    FrameLayout overviewOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_cam);

        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        scene_id = getIntent().getIntExtra(KEY_SCENE_ID, -1);
        showProjectProgress = getIntent().getBooleanExtra(KEY_SHOW_PROJECT_PROGRESS, true);

        project = myApp.getProjectForID(project_id);
        Log.e(C.T, "project: " + project.toString());
        scene = project.movieTemplate.getSceneWithID(scene_id);
        Log.e(C.T, "category: " + scene.toString());
        maxSceneRecordingTimeMs = scene.video_length;
        MAX_PROGRESS_LIMIT = maxSceneRecordingTimeMs;

        selfieScene = shouldOpenFrontCam = scene.type == Scene.SELFIE;

        cameraPreviewLayout = findViewById(R.id.a_cam_preview);
        cameraPreviewLayout.addView(cameraPreview);

        clipPlaybackBtn = findViewById(R.id.a_cam_delete_clip);
        recordDot = findViewById(R.id.a_cam_rec_dot);
        camTimerText = findViewById(R.id.a_cam_timer);
        camRecordBtn = findViewById(R.id.a_cam_action);
        sceneNoText = findViewById(R.id.a_cam_scene_no);
        stopIcon = findViewById(R.id.a_cam_stop_icon);
        startIcon = findViewById(R.id.a_cam_start_icon);
        helpBtn = findViewById(R.id.a_cam_help);
        progressBarContainer = findViewById(R.id.a_cam_progressbar_container);
        recordProgress = findViewById(R.id.a_cam_progress);
        remainingTimeText = findViewById(R.id.a_cam_remaining_time);
        sceneVideoText = findViewById(R.id.a_cam_scene_hint);
        selfieSceneVideoText = findViewById(R.id.a_cam_selfie_scene_hint);
        sceneVideoTextContainer = findViewById(R.id.a_cam_scene_hint_container);
        selfieSceneVideoTextContainer = findViewById(R.id.a_cam_selfie_scene_hint_container);
        focusCircle = findViewById(R.id.a_cam_focus_circle);
        sceneMaximizer = findViewById(R.id.a_cam_scene_hint_container_maximizer);
        projectProgressBar = findViewById(R.id.a_cam_project_progress);

        clipPlayer = findViewById(R.id.a_cam_clip_player);
        clipVideoView = findViewById(R.id.a_cam_clip_player_videoview);
        clipSaveBtn = findViewById(R.id.a_cam_clip_player_save_btn);
        clipDeletBtn = findViewById(R.id.a_cam_clip_player_delete_btn);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayWidth = displayMetrics.widthPixels;
        final float ratio     = C.HD_RECORD_MODE ? (1280f / 720f) : (720f / 480f);
        final float newHeight = displayWidth * ratio;
        clipVideoView.setLayoutParams(new FrameLayout.LayoutParams((int) (float) displayWidth, (int) newHeight));// todo move this in screen size change


        camTimerText.setVisibility(View.INVISIBLE);
        clipPlaybackBtn.setOnClickListener(this);
        clipPlaybackBtn.setVisibility(selfieScene ? View.INVISIBLE : View.VISIBLE);
        recordDot.setVisibility(View.GONE);
        camRecordBtn.setOnClickListener(this);
        stopIcon.setVisibility(View.GONE);
        if (selfieScene) stopIcon.setImageResource(R.drawable.ic_stop_white_24dp);
        recordProgress.setProgress(0);
        recordProgress.setMax((int) MAX_PROGRESS_LIMIT);
        sceneVideoText.setText(scene.getHtmlVideoText());
        selfieSceneVideoText.setText(scene.getHtmlVideoText());
        clipSaveBtn.setOnClickListener(this);
        clipDeletBtn.setOnClickListener(this);
        progressBarContainer.setVisibility(selfieScene ? View.INVISIBLE : View.VISIBLE);
        helpBtn.setOnClickListener(this);
        sceneVideoTextContainer.setOnClickListener(this);
        selfieSceneVideoTextContainer.setOnClickListener(this);
        cameraPreviewLayout.setOnTouchListener(this);
        sceneMaximizer.setVisibility(View.INVISIBLE);
        sceneMaximizer.setOnClickListener(this);

        // drawable for progressbar


        if (showProjectProgress) {

            final int progress = project.calculateProgress();

            projectProgressBar.setMax(project.getMaxProgress());
            projectProgressBar.setProgress(progress);
        } else {
            projectProgressBar.setVisibility(View.GONE);
        }

        // Congo stuff
        showFirstClipCongo = ShrePrefs.readData(this, C.sp_show_first_clip_congo, true);
        showFirstSceneCongo = ShrePrefs.readData(this, C.sp_show_first_scene_congo, true);
        showDikhaoCompleteCongo = ShrePrefs.readData(this, C.sp_show_dikhao_complete_congo, true);
        congo = findViewById(R.id.a_cam_congo);
        congo_text = findViewById(R.id.a_cam_congo_text);
        congo.setOnClickListener(this);
        congo_image = findViewById(R.id.a_cam_congo_img);


        // Overlay stuff
        showBataoCamOverlay = ShrePrefs.readData(this, C.sp_show_batao_cam_intro_overlay, true);
        showDikhaoCamOverlay = ShrePrefs.readData(this, C.sp_show_dikhao_cam_intro_overlay, true);
        introOverlay = findViewById(R.id.a_cam_intro_overlay);
        introOverlay.setOnClickListener(this);

        naiSamjaText = findViewById(R.id.a_cam_nai_samja_text);
        naiSamjaText.setOnClickListener(this);
        introOverlayHeader = findViewById(R.id.a_cam_intro_overlay_header);
        introOverlayText = findViewById(R.id.a_cam_intro_overlay_text);

        if (selfieScene) {
            if (showBataoCamOverlay) {
                introOverlayHeader.setText(R.string.batao);
                introOverlayText.setText(R.string.cam_overlay_batao_intro_text);
                introOverlay.setVisibility(View.VISIBLE);
                ShrePrefs.writeData(this, C.sp_show_batao_cam_intro_overlay, false);
            }
        } else {
            if (showDikhaoCamOverlay) {
                introOverlayHeader.setText(R.string.dikhao);
                introOverlayText.setText(R.string.cam_overlay_dikhao_intro_text);
                introOverlay.setVisibility(View.VISIBLE);
                ShrePrefs.writeData(this, C.sp_show_dikhao_cam_intro_overlay, false);
            }
        }


        showViewClipOverlay = ShrePrefs.readData(this, C.sp_show_view_clip_overlay, true);
        showOverviewOverlay = false;// before, was reading from sharedprefs
        viewLastClipOverlay = findViewById(R.id.a_cam_view_clip_overlay);
        viewLastClipOverlay.setOnClickListener(this);

        overviewOverlay = findViewById(R.id.a_cam_overview_overlay);
        overviewOverlay.setOnClickListener(this);
        if (showOverviewOverlay) {
            showOverviewOverlay = false;
            ShrePrefs.writeData(this, C.sp_show_overview_overlay, false);
            overviewOverlay.setVisibility(View.VISIBLE);
        }

        int i = 0;
        for (String clip : scene.videoClips) {

            final int clipLength = (int) Helpers.calculateClipLength(clip);
            int       clipMarkerPosition;
            if (clipMarkers.size() == 0) {
                clipMarkerPosition = (int) ((clipLength * displayWidth) / scene.video_length);
            } else {
                clipMarkerPosition = clipMarkers.get(i) + (int) ((clipLength * displayWidth) / scene.video_length);
                i++;
            }
            clipMarkers.add(clipMarkerPosition);

            placeCheckpoint(clipMarkerPosition);

        }


        rRecordingTimer = new Runnable() {
            @Override
            public void run() {
                setElapsedTime(elapsedRecordingTimeMS + TIMER_DELAY);
                recordingTimeHandler.postDelayed(this, TIMER_DELAY);

                final int clipSize = (int) (scene.remainingVideoClipLengthMs - elapsedRecordingTimeMS);

                final int progressbarValue = (int) (MAX_PROGRESS_LIMIT - clipSize);

                recordProgress.setProgress(progressbarValue);

                remainingTimeText.setText((clipSize) / 1000 + "s");

                if (selfieScene) {
                    canStopRecording = elapsedRecordingTimeMS > Scene.MIN_SELFIE_CLIP_LENGTH;
                } else {

                    // if selfie scene, no video recording limit (or max limit?)
                    if (clipSize < 0) {
                        recordProgress.setProgress((int) MAX_PROGRESS_LIMIT);
                        remainingTimeText.setText("0s");
                        stopRecording();
                        updateCameraViews();
                    }
                }

            }
        };

    }

    private void placeCheckpoint(int leftMargin) {

        final ImageView img = new ImageView(this);
        img.setBackgroundResource(R.drawable.accent_rect);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                12,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.leftMargin = leftMargin;
        progressBarContainer.addView(img, params);
        prevClipMargin = params.leftMargin;
        checkPoints.add(img);

    }

    /**
     * Activity LifeCycle methods
     */

    @Override
    protected void onResume() {
        super.onResume();

        updateProgressBarAndRemainingTimeText();

        // enable/disable delete clip button
        if (scene.getNoofVideoClipsPresent() > 0) {
            clipPlaybackBtn.setVisibility(selfieScene ? View.INVISIBLE : View.VISIBLE);
        } else {
            clipPlaybackBtn.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        updateCameraViews();
        setElapsedTime(0);

    }

    /***/

    private void updateProgressBarAndRemainingTimeText() {
        if (selfieScene) {
            recordProgress.setProgress(0);
            remainingTimeText.setText((maxSceneRecordingTimeMs / 1000) + "s");
        } else {
            final int clipCompleted    = (int) (scene.remainingVideoClipLengthMs - elapsedRecordingTimeMS);
            final int progressBarValue = (int) MAX_PROGRESS_LIMIT - clipCompleted;
            Log.e(C.T, "progress bar value: " + progressBarValue);
            recordProgress.setProgress(progressBarValue);
            remainingTimeText.setText((int) (clipCompleted / 1000f) + "s");
        }
    }

    @Override
    public void onClick(View v) {

        if (v == camRecordBtn) {

            if (isRecording) {

                // stop recording
                if (canStopRecording) {
                    stopRecording();
                    updateCameraViews();

                    if (showFirstClipCongo) {
                        showCongo(R.string.first_clip_congo_text, R.drawable.illustration_success_finish_all_sections);
                        ShrePrefs.writeData(this, C.sp_show_first_clip_congo, false);
                        showFirstClipCongo = false;
                    }

                }

            } else {

                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, THE_REQUEST_CODE);

                } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);

                } else {

                    // Set output file
                    final String videoFileName;
                    if (selfieScene) {
                        videoFileName = tempVideoFileName;
                    } else {

                        setElapsedTime(0);
                        final int noofVideosInScene = scene.videoClips.size();
                        videoFileName = project.absoluteFolderPath + "/c" + scene.ID + "_v" + (noofVideosInScene + 1) + "." + C.VIDEO_EXTENSION;

                    }
                    lastSavedVideoFileName = videoFileName;

                    if (selfieScene) {
                        scene.remainingVideoClipLengthMs = scene.video_length;
                        prepareMediaRecorderAndStartRecording(videoFileName);
                        updateCameraViews();
                    } else {
                        Log.i(C.T, scene.remainingVideoClipLengthMs + " -- out of -- " + scene.video_length);
                        if (scene.remainingVideoClipLengthMs > 0) {
                            prepareMediaRecorderAndStartRecording(videoFileName);
                            updateCameraViews();
                        }
                    }
                }
            }

        }

        if (v == clipPlaybackBtn) {

            final String lastVideoFile = scene.videoClips.get(scene.videoClips.size() - 1);
            clipVideoView.setVideoPath(lastVideoFile);
            showClipPlayer();
            clipVideoView.start();
        }

        if (v == clipSaveBtn) {
            clipVideoView.pause();
            dismissClipPlayer();
        }

        if (v == clipDeletBtn) {
            // delete previous clip

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    clipVideoView.pause();

                    final String lastVideoFile = scene.videoClips.get(scene.videoClips.size() - 1);

                    scene.removeVideoClip(lastVideoFile);

                    if (scene.getNoofVideoClipsPresent() <= 0) {
                        clipPlaybackBtn.setVisibility(View.INVISIBLE);
                    }

                    checkPoints.get(checkPoints.size() - 1).setVisibility(View.GONE);
                    checkPoints.remove(checkPoints.size() - 1);
                    clipMarkers.remove(clipMarkers.size() - 1);

                    setElapsedTime(0);

                    updateProgressBarAndRemainingTimeText();

                    dismissClipPlayer();

                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    hideStatusBar();
                }
            });
            builder.setTitle(R.string.confirm_delete_dialog_header);
            builder.setMessage(R.string.delete_clip_message);
            builder.show();
        }

        // congos
        if (v == congo) {
            congo.setVisibility(View.GONE);
            if (showViewClipOverlay) {

                viewLastClipOverlay.setVisibility(View.VISIBLE);
                showViewClipOverlay = false;
                overlaySequenceActive = false;
                ShrePrefs.writeData(this, C.sp_show_view_clip_overlay, false);

            }
        }

        if (v == helpBtn) {

            overlaySequenceActive = true;
            introOverlay.setVisibility(View.VISIBLE);
            if (selfieScene) {
                introOverlayHeader.setText(R.string.batao);
                introOverlayText.setText(R.string.cam_overlay_batao_intro_text);
            } else {
                introOverlayHeader.setText(R.string.dikhao);
                introOverlayText.setText(R.string.cam_overlay_dikhao_intro_text);
            }

        }

        // overlays
        if (v == introOverlay) {
            introOverlay.setVisibility(View.GONE);
            if (overlaySequenceActive) {
                if (scene.videoClips.size() == 0) {
                    overviewOverlay.setVisibility(View.VISIBLE);
                } else {
                    viewLastClipOverlay.setVisibility(View.VISIBLE);
                }
            }
        }

        if (v == viewLastClipOverlay) {
            viewLastClipOverlay.setVisibility(View.GONE);
            if (overlaySequenceActive) {
                overviewOverlay.setVisibility(View.VISIBLE);
                overlaySequenceActive = false;
            }
        }

        if (v == overviewOverlay) {
            overviewOverlay.setVisibility(View.GONE);
        }

        if (v == naiSamjaText) {
            introOverlay.setVisibility(View.GONE);
            final String tutorialVideo = Helpers.getAssetString(this,
                    selfieScene ? C.SELFIE_TUTORIAL_VIDEO_NAME : C.DIKHAO_TUTORIAL_VIDEO_NAME
            );

            final Intent intent = new Intent(this, AVideoPlayer.class);
            intent.putExtra(AVideoPlayer.KEY_FILE_NAME, tutorialVideo);
            intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
            intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
            intent.putExtra(AVideoPlayer.KEY_KEEP_ASPECT_RATIO, false);
            intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                @Override
                public void onContinueClicked(Activity activity) {

                    ShrePrefs.writeData(activity, selfieScene ?
                                    C.sp_show_sunao_tuto :
                                    C.sp_show_dekho_tuto,
                            false);

                    final Intent intent = new Intent(activity, ACam.class);
                    intent.putExtras(getIntent());
                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);

                }
            });

        }

        if (v == sceneMaximizer) {
            if (selfieScene) {
                if (isRecording) {
                    selfieSceneVideoTextContainer.setVisibility(View.VISIBLE);
                } else {
                    sceneVideoTextContainer.setVisibility(View.VISIBLE);
                }
            } else {
                sceneVideoTextContainer.setVisibility(View.VISIBLE);
            }
            sceneMaximizer.setVisibility(View.INVISIBLE);
        }

        if (v == sceneVideoTextContainer) {
            sceneVideoTextContainer.setVisibility(View.GONE);
            sceneMaximizer.setVisibility(View.VISIBLE);
            selfieSceneVideoTextContainer.setVisibility(View.GONE);
        }

        if (v == selfieSceneVideoTextContainer) {
            sceneVideoTextContainer.setVisibility(View.GONE);
            sceneMaximizer.setVisibility(View.VISIBLE);
            selfieSceneVideoTextContainer.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // TODO   can't play video after selfie pressing back when recording from selfie
        if (isRecording) {
            if (lastSavedVideoFileName != null) {
                final File file = new File(lastSavedVideoFileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent me) {

        if (me.getActionMasked() == MotionEvent.ACTION_UP) {

            if (view == cameraPreviewLayout) {

                if (shouldOpenFrontCam) {
                    focusCircle.setVisibility(View.GONE);
                    return false;
                }

                super.doTheAutoFocus(
                        me.getX(),
                        me.getY(),
                        cameraPreviewLayout.getWidth(),
                        cameraPreviewLayout.getHeight()
                );

            }

        }

        return true;
    }

    private void showClipPlayer() {
        clipPlayer.setVisibility(View.VISIBLE);
        cameraPreview.setVisibility(View.INVISIBLE);
    }

    private void dismissClipPlayer() {

        clipPlayer.setVisibility(View.GONE);
        cameraPreview.setVisibility(View.VISIBLE);

    }

    protected void stopRecording() {
        // stop recording and release camera
        try {

            super.stopRecording();

            /* if category type is selfie one,
                start playback of the recorded clip
               else
                just save the video file and move on
             */
            scene.updateCompletionStatus();
            if (selfieScene) {

                final String dstFile = project.absoluteFolderPath + "/c" + scene.ID + "_v1." + C.VIDEO_EXTENSION;

                final Intent i = new Intent(this, ASelfiePlaybackScreen.class);
                i.putExtra("tmp_file", tempVideoFileName);
                i.putExtra("file", dstFile);
                i.putExtra("proj_id", project_id);
                i.putExtra("cat_id", scene_id);
                startActivity(i);
            } else {


                scene.addVideo(lastSavedVideoFileName);

                final int clipLength = (int) Helpers.calculateClipLength(lastSavedVideoFileName);
                int       clipMarkerPosition;
                if (clipMarkers.size() == 0) {
                    clipMarkerPosition = (int) ((clipLength * displayWidth) / scene.video_length);
                } else {
                    clipMarkerPosition = clipMarkers.get(clipMarkers.size() - 1) + (int) ((clipLength * displayWidth) / scene.video_length);
                }
                clipMarkers.add(clipMarkerPosition);
                placeCheckpoint(clipMarkerPosition);

                final int progressBarValue = (int) (MAX_PROGRESS_LIMIT - scene.remainingVideoClipLengthMs);
                recordProgress.setProgress(progressBarValue);


                if (scene.remainingVideoClipLengthMs <= 1) {
                    scene.remainingVideoClipLengthMs = 0;

                    if (showFirstSceneCongo) {

                        showFirstSceneCongo = false;
                        showCongo(R.string.first_scene_congo_text, R.drawable.illustration_success_first_scene);
                        congo.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                proceedToPlayBack();
                                ShrePrefs.writeData(ACam.this, C.sp_show_first_scene_congo, false);
                                ShrePrefs.writeData(ACam.this, C.sp_show_overview_overlay, true);
                            }
                        });

                    } else {

                        // goto to video player to play all the recorded clips
                        proceedToPlayBack();

                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            // inform the user that recording has stopped
            recordingTimeHandler.removeCallbacks(rRecordingTimer);
        }
    }

    private void proceedToPlayBack() {

        final String tmpPlaybackFile = C.TEMP_DIR + "/temp." + C.VIDEO_EXTENSION;
        new SimpleMuxer(this, scene.videoClips, tmpPlaybackFile, new SimpleMuxer.SimpleMuxFinishListener() {
            @Override
            public void onMuxFinish(boolean success) {

                if (success) {

                    Intent intent = new Intent(ACam.this, AVideoPlayer.class);
                    intent.putExtra(AVideoPlayer.KEY_FILE_NAME, tmpPlaybackFile);
                    intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
                    intent.putExtra(AVideoPlayer.KEY_PROJECT_ID, project_id);
                    intent.putExtra(AVideoPlayer.KEY_SCENE_ID, scene_id);
                    intent.putExtra(AVideoPlayer.KEY_CAN_EDIT, true);
                    startActivity(intent);

                    // Upon video save
                    AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                        @Override
                        public void onContinueClicked(Activity activity) {

                            MyApplication.getInstance().flowManager.gotoNextActivity(activity, project_id);

                        }
                    });

                } else {
                    Toast.makeText(ACam.this, "Playback Failed.", Toast.LENGTH_SHORT).show();
                }

            }
        }).execute();

    }

    private void updateCameraViews() {

        if (isRecording) {
            recordDot.setVisibility(View.VISIBLE);
            stopIcon.setVisibility(View.VISIBLE);
            startIcon.setVisibility(View.GONE);
            sceneVideoTextContainer.setVisibility(View.GONE);
            if (selfieScene) {
                selfieSceneVideoTextContainer.setVisibility(View.VISIBLE);
                sceneMaximizer.setVisibility(View.INVISIBLE);
            } else {
                selfieSceneVideoTextContainer.setVisibility(View.GONE);
                sceneMaximizer.setVisibility(View.VISIBLE);
            }

            clipPlaybackBtn.setVisibility(View.INVISIBLE);
            helpBtn.setVisibility(View.INVISIBLE);
            sceneNoText.setVisibility(View.GONE);
            camTimerText.setVisibility(View.VISIBLE);

        } else {
            recordDot.setVisibility(View.INVISIBLE);
            stopIcon.setVisibility(View.GONE);
            startIcon.setVisibility(View.VISIBLE);
            sceneVideoTextContainer.setVisibility(View.VISIBLE);
            selfieSceneVideoTextContainer.setVisibility(View.GONE);
            clipPlaybackBtn.setVisibility(selfieScene ? View.INVISIBLE : View.VISIBLE);
            helpBtn.setVisibility(View.VISIBLE);
            sceneNoText.setVisibility(View.VISIBLE);
            camTimerText.setVisibility(View.INVISIBLE);
            sceneMaximizer.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    String getName() {
        return "ACam";
    }

    private void showCongo(int string, int img) {

        congo_text.setText(string);
        congo.setVisibility(View.VISIBLE);
        congo_image.setImageResource(img);

    }

    /**
     * Camera and Media Recorder Stuff Below
     */


    private void setElapsedTime(int time) {
        elapsedRecordingTimeMS = time;

        if (time <= 0) {
            ms = 0;
            secs = 0;
            mins = 0;
            camTimerText.setText("00:00");
        } else {

            ms += TIMER_DELAY;

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

}
