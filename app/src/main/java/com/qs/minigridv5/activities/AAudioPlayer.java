package com.qs.minigridv5.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.utilities.MyApplication;
import com.qs.minigridv5.workers.WFileMover;

import java.io.File;

/**
 * Plays audio and can save audio
 */
public class AAudioPlayer extends MyActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener, WFileMover.FileMoveListener {


    public static final String KEY_PROJECT_ID = "proj_id";
    public static final String KEY_SCENE_ID   = "cat_id";

    private long   project_id;
    private int    scene_id;
    private String tempAudioFile = C.TEMP_DIR + "/tmp." + C.AUDIO_EXTENSION;
    private String dstFile;
    private Scene  scene;

    // Views
    private FrameLayout playBtn;
    private Button      saveBtn, reRecordBtn;
    private ImageView playStartIcon, playStopIcon;
    private TextView headerText;

    private boolean     isPlaying      = false;
    private boolean     playbackPaused = false;
    private MediaPlayer mediaPlayer;

    private boolean editMode = false;

    ProgressDialog fileMovingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_audio_player);


        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        scene_id = getIntent().getIntExtra(KEY_SCENE_ID, -1);
        editMode = getIntent().getBooleanExtra(AAudioRecorder.KEY_EDIT_MODE, false);

        final Project project = myApp.getProjectForID(project_id);
        scene = project.movieTemplate.getSceneWithID(scene_id);
        dstFile = project.absoluteFolderPath + "/c" + scene.ID + "_a." + C.AUDIO_EXTENSION;

        headerText = findViewById(R.id.a_audio_player_header_text);
        headerText.setText(scene.title);
        playBtn = findViewById(R.id.a_audio_player_play_btn);
        playBtn.setOnClickListener(this);
        saveBtn = findViewById(R.id.a_audio_player_save_btn);
        saveBtn.setOnClickListener(this);
        reRecordBtn = findViewById(R.id.a_audio_player_re_record_btn);
        reRecordBtn.setOnClickListener(this);
        playStartIcon = findViewById(R.id.a_audio_player_start_icon);
        playStopIcon = findViewById(R.id.a_audio_player_stop_icon);
        playStopIcon.setVisibility(View.GONE);

        fileMovingProgressDialog = new ProgressDialog(this);
        fileMovingProgressDialog .setTitle("Saving...");

    }

    @Override
    protected void onResume() {
        super.onResume();

        changeStatusBarColor(R.color.bg_color);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mediaPlayer != null) {
            pausePlayBack();
        }

    }

    @Override
    String getName() {
        return "Audio Recorder";
    }

    @Override
    public void onClick(View view) {

        if (view == playBtn) {

            // if audio playback is happening, pause...
            if (isPlaying) {

                pausePlayBack();

            } else {// ... else if not happening, if paused, resume else start playback

                playStopIcon.setVisibility(View.VISIBLE);
                playStartIcon.setVisibility(View.GONE);

                if (playbackPaused) {
                    resumePlayBack();
                } else {
                    startPlayBack();
                }

            }

        }

        if (view == saveBtn) {

            fileMovingProgressDialog.show();
            new WFileMover(tempAudioFile, dstFile, this).execute();

        }

        if (view == reRecordBtn) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyLightDialog);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final File file = new File(tempAudioFile);
                    if (file.delete()) {

                        Intent intent = new Intent(AAudioPlayer.this, AAudioRecorder.class);
                        intent.putExtra(AAudioRecorder.KEY_PROJECT_ID, project_id);
                        intent.putExtra(AAudioRecorder.KEY_SCENE_ID, scene_id);
                        intent.putExtra(AAudioRecorder.KEY_EDIT_MODE, editMode);
                        startActivity(intent);
                    }

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
            builder.setMessage(R.string.all_clips_delete_message);
            builder.show();

        }

    }

    private void startPlayBack() {

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.setOnCompletionListener(this);
        try {
            mediaPlayer.setDataSource(tempAudioFile);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            playbackPaused = false;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void resumePlayBack() {
        mediaPlayer.start();
        playbackPaused = false;
        isPlaying = true;
    }

    private void pausePlayBack() {
        mediaPlayer.pause();
        playbackPaused = true;
        isPlaying = false;
        playStopIcon.setVisibility(View.GONE);
        playStartIcon.setVisibility(View.VISIBLE);

    }

    private void setBtnIcon(Button button, int iconRes) {

        button.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        isPlaying = false;
        playbackPaused = false;
        playStopIcon.setVisibility(View.GONE);
        playStartIcon.setVisibility(View.VISIBLE);
        mediaPlayer.release();
        this.mediaPlayer = null;
    }

    @Override
    public void onMovingComplete(boolean success) {

        fileMovingProgressDialog.dismiss();

        if (success) {

            scene.setAudio(dstFile);

            if (!editMode) {

                MyApplication.getInstance().flowManager.gotoNextActivity(this, project_id);

            } else {

                finish();

            }

        } else {
            Toast.makeText(this, "Save failed :(\nPlease try again", Toast.LENGTH_SHORT).show();
        }

    }
}
