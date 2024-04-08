package com.qs.minigridv5.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import com.qs.minigridv5.misc.ShrePrefs;

public class AAudioRecorder extends MyActivity implements View.OnClickListener {
    private final       int    THE_REQUEST_CODE = 23745;
    public static final String KEY_PROJECT_ID   = "proj_id";
    public static final String KEY_SCENE_ID     = "cat_id";
    public static final String KEY_EDIT_MODE    = "edit_mode";

    private Project project;
    private long    project_id;
    private Scene   scene;
    private int     scene_id;
    private long    sceneMaxAudioLength = 0;

    private MediaRecorder mediaRecorder;

    // Views
    private ImageView helpBtn;

    private TextView sceneHeaderText, sceneAudioDescText;

    private TextView    timerText;
    private ImageView   helpContainer;
    private FrameLayout recordBtn;
    private TextView    remainingTimeText;
    private ImageView   startIcon, stopIcon;
    private VideoView   waveAnimation;
    private ProgressBar progressBar;
    private SeekBar     projectProgressBar;

    private boolean editMode = false;

    private MediaPlayer mediaPlayer;
    private boolean     isRecording           = false;
    private boolean     isPlaying             = false;
    private boolean     audioPaused           = false;
    private int         elapsedRecordingTimeS = 0;
    private int         secs                  = 0;
    private int         mins                  = 0;
    private Handler     timerHandler          = new Handler();
    private String      tempAudioFile         = C.TEMP_DIR + "/tmp." + C.AUDIO_EXTENSION;

    boolean     overlaySequenceActive = false;
    boolean     showIntroOverlay      = false;
    TextView    naiSamjaText;
    FrameLayout introOverlay;
    FrameLayout overviewOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_audio_recorder);

        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        scene_id = getIntent().getIntExtra(KEY_SCENE_ID, -1);
        editMode = getIntent().getBooleanExtra(KEY_EDIT_MODE, false);


        project = myApp.getProjectForID(project_id);
        scene = project.movieTemplate.getSceneWithID(scene_id);

        final String sceneAudioHeaderText = scene.audio_header;
        sceneMaxAudioLength = scene.max_audio_length;

        helpBtn = findViewById(R.id.a_recorder_help);
        helpBtn.setOnClickListener(this);

        sceneHeaderText = findViewById(R.id.a_recorder_scene_header);
        sceneHeaderText.setText(sceneAudioHeaderText);
        if (sceneAudioHeaderText.isEmpty()) sceneHeaderText.setVisibility(View.GONE);// TODO remove the header later

        sceneAudioDescText = findViewById(R.id.a_recorder_scene_desc);
        sceneAudioDescText.setText(scene.getHtmlAudioText());

        timerText = findViewById(R.id.a_recorder_timer_text);
        timerText.setText("0");
        timerText.setVisibility(View.INVISIBLE);

        helpContainer = findViewById(R.id.a_recorder_hold_close_help_container);
        helpContainer.setVisibility(View.VISIBLE);

        recordBtn = findViewById(R.id.a_recorder_record_btn);
        recordBtn.setOnClickListener(this);

        stopIcon = findViewById(R.id.a_recorder_stop_icon);
        stopIcon.setVisibility(View.GONE);

        startIcon = findViewById(R.id.a_recorder_start_icon);

        progressBar = findViewById(R.id.a_recorder_progress);
        progressBar.setProgress(0);

        remainingTimeText = findViewById(R.id.a_recorder_remaining_time);
        remainingTimeText.setText(sceneMaxAudioLength + "s");

        projectProgressBar = findViewById(R.id.a_recorder_project_progress);

        if (!editMode) {

            projectProgressBar.setMax(project.getMaxProgress());
            projectProgressBar.setProgress(project.calculateProgress());

        } else {
            projectProgressBar.setVisibility(View.GONE);
        }

        // overlay stuff
        introOverlay = findViewById(R.id.a_audio_rec_intro_overlay);
        introOverlay.setOnClickListener(this);
        naiSamjaText = findViewById(R.id.a_audio_rec_nai_samja_text);
        naiSamjaText.setOnClickListener(this);
        overviewOverlay = findViewById(R.id.a_audio_rec_overview_overlay);
        overviewOverlay.setOnClickListener(this);

        showIntroOverlay = ShrePrefs.readData(this, C.sp_show_audio_intro_overlay, true);
        if (showIntroOverlay) {
            introOverlay.setVisibility(View.VISIBLE);
            ShrePrefs.writeData(this, C.sp_show_audio_intro_overlay, false);
        }

        waveAnimation = findViewById(R.id.a_recorder_wave);
        waveAnimation.setVideoURI(Uri.parse(Helpers.getAssetString(this, C.WAVE_ANIMATION)));
        waveAnimation.seekTo(1);
        waveAnimation.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatusBarColor(R.color.bg_color);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if permission to camera is granted, only then access it otherwise ask for the permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMediaRecording();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == THE_REQUEST_CODE) {

            for (int i = 0; i < permissions.length; i++) {


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
                                        ActivityCompat.requestPermissions(AAudioRecorder.this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);
                                    }
                                });

                    }

                }

            }

        }

    }


    @Override
    public void onClick(View view) {

//        if (view == overviewBtn) {
//            final Intent intent = new Intent(this, AOverview.class);
//            intent.putExtra("proj_id", project_id);
//            startActivity(intent);
//        }

        if (view == helpBtn) {

            introOverlay.setVisibility(View.VISIBLE);
            overlaySequenceActive = true;

        }

        if (view == recordBtn) {

            if (isRecording) {

                stopMediaRecording();

                gotoAudioPlayer();

            } else {

                startRecording();

            }

            updateViews();


        }

        if (view == introOverlay) {

            introOverlay.setVisibility(View.GONE);
            if (overlaySequenceActive) {
                overviewOverlay.setVisibility(View.VISIBLE);
            }

        }

        if (view == naiSamjaText) {

            introOverlay.setVisibility(View.GONE);
            final String tutorialVideo = Helpers.getAssetString(this, C.AUDIO_TUTORIAL_VIDEO_NAME);

            final Intent intent = new Intent(this, AVideoPlayer.class);
            intent.putExtra(AVideoPlayer.KEY_FILE_NAME, tutorialVideo);
            intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
            intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
            intent.putExtra(AVideoPlayer.KEY_KEEP_ASPECT_RATIO, false);
            intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
            startActivity(intent);

            AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                @Override
                public void onContinueClicked(Activity activity) {

                    ShrePrefs.writeData(activity, C.sp_show_sunao_audio_tuto, false);

                    final Intent intent = new Intent(activity, AAudioRecorder.class);
                    intent.putExtras(getIntent());
                    activity.startActivity(intent);

                }
            });

        }

        if (view == overviewOverlay) {
            overviewOverlay.setVisibility(View.GONE);
        }

    }

    private void startRecording() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, THE_REQUEST_CODE);
            return;
        }

        // start recording
        prepareMediaRecorder();

        try {

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;

            timerHandler.removeCallbacks(updateTimer);
            timerHandler.postDelayed(updateTimer, 1000);

            waveAnimation.setVisibility(View.VISIBLE);
            waveAnimation.start();

        } catch (Exception e) {

            e.printStackTrace();
            Crashlytics.logException(e);
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();

        }

    }

    private void stopMediaRecording() {

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                Crashlytics.logException(e);
            } finally {

                mediaRecorder.release();
                mediaRecorder = null;
            }
        }
        isRecording = false;
        timerHandler.removeCallbacks(updateTimer);
        setElapsedTime(0);
    }

    private void stopRecording() {
        progressBar.setProgress(100);
        stopMediaRecording();
        updateViews();
    }

    private void gotoAudioPlayer() {

        final Intent intent = new Intent(this, AAudioPlayer.class);
        intent.putExtra(AAudioPlayer.KEY_PROJECT_ID, project_id);
        intent.putExtra(AAudioPlayer.KEY_SCENE_ID, scene_id);
        intent.putExtra(KEY_EDIT_MODE, editMode);
        startActivity(intent);

    }

    public void prepareMediaRecorder() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (C.TRY_WEBM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.WEBM);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.VORBIS);
            } else {
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
        } else {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }


        mediaRecorder.setAudioChannels(2);
        mediaRecorder.setAudioSamplingRate(48000);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setOutputFile(tempAudioFile);

    }

    private void setElapsedTime(int time) {
        elapsedRecordingTimeS = time;

        if (time <= 0) {
            secs = 0;
            mins = 0;
            timerText.setText("0");
        } else {

            // logic below based on hoping the elapsed time is incremented by 1
            secs++;
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
            timerText.setText(elapsedRecordingTimeS + "");

        }

    }

    private void updateViews() {

        if (isRecording) {

            helpContainer.setVisibility(View.INVISIBLE);
            timerText.setVisibility(View.VISIBLE);
            stopIcon.setVisibility(View.VISIBLE);
            startIcon.setVisibility(View.GONE);

            helpBtn.setVisibility(View.INVISIBLE);

        } else {

            helpContainer.setVisibility(View.VISIBLE);
            timerText.setVisibility(View.GONE);
            startIcon.setVisibility(View.VISIBLE);
            stopIcon.setVisibility(View.GONE);

            helpBtn.setVisibility(View.VISIBLE);

        }

    }

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {

            setElapsedTime(elapsedRecordingTimeS + 1);

            final int clipCompleted = (int) ((sceneMaxAudioLength) - elapsedRecordingTimeS);
            progressBar.setProgress((int) ((sceneMaxAudioLength - clipCompleted) * 100 / sceneMaxAudioLength));

            if (clipCompleted >= 0)
                remainingTimeText.setText((clipCompleted) + "s");

            timerHandler.postDelayed(this, 1000);

            if (elapsedRecordingTimeS > sceneMaxAudioLength) {
                stopRecording();
                gotoAudioPlayer();
            }

        }
    };

    @Override
    String getName() {
        return "AudioRecorder";
    }

}
