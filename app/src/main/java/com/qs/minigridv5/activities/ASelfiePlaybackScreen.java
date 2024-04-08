package com.qs.minigridv5.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.utilities.MyApplication;
import com.qs.minigridv5.workers.WFileMover;

import java.io.File;

public class ASelfiePlaybackScreen extends MyActivity implements MediaPlayer.OnCompletionListener, WFileMover.FileMoveListener {

    private Button playBtn, retakeBtn, saveBtn;

    private String tmpVideoFileString;
    private String videoFileString;

    private VideoView videoView;

    private long project_id;
    private int scene_id;

    public static boolean gotoToNextScene = true;

    private ProgressDialog fileMovingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_selfie_playback);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

//        playBtn = (Button) findViewById(R.id.button_play);
        retakeBtn = findViewById(R.id.a_selfie_playback_retake_btn);
        saveBtn = findViewById(R.id.a_selfie_playback_save_btn);

        tmpVideoFileString = getIntent().getStringExtra("tmp_file");
        videoFileString = getIntent().getStringExtra("file");
        project_id = getIntent().getLongExtra("proj_id", -1);
        scene_id = getIntent().getIntExtra("cat_id", -1);

        if (videoFileString != null) {


            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            final float newWidth = width;
            final float newHeight = width * (720f / 480f);

            videoView = findViewById(R.id.a_player_videoview);
            videoView.setVideoPath(tmpVideoFileString);
            videoView.setOnCompletionListener(this);
            videoView.seekTo(100);

            videoView.start();

//            videoView.setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, (int) newHeight));

        } else {
            Toast.makeText(this, "No video found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    String getName() {
        return "APlayer";
    }

    public void retake(View view) {

        final File file = new File(tmpVideoFileString);
        if (file.delete()) {
            Toast.makeText(this, "Video Discarded", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, ACam.class);
        intent.putExtra(ACam.KEY_PROJECT_ID, project_id);
        intent.putExtra(ACam.KEY_SCENE_ID, scene_id);
        startActivity(intent);

    }

    public void save(View view) {
        if (fileMovingProgressDialog == null) {
            fileMovingProgressDialog = new ProgressDialog(this);
            fileMovingProgressDialog.setMessage(getString(R.string.wait_a_sec));
            fileMovingProgressDialog.setCancelable(false);
        }

        fileMovingProgressDialog.show();
        new WFileMover(tmpVideoFileString, videoFileString, this).execute();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onMovingComplete(boolean success) {

        fileMovingProgressDialog.dismiss();

        if (success) {

            // first add video to category videos array
            final Project project = myApp.getProjectForID(project_id);
            final Scene scene = project.movieTemplate.getSceneWithID(scene_id);
            scene.addVideo(videoFileString);

            if(gotoToNextScene) {
                MyApplication.getInstance().flowManager.gotoNextActivity(this, project_id);
            } else {

                final Intent intent = new Intent(ASelfiePlaybackScreen.this, AProjectEdit.class);
                intent.putExtra(AProjectEdit.KEY_PROJECT_ID, project_id);
                intent.putExtra(AProjectEdit.KEY_LOAD_PAGE, AProjectEdit.PROJECT_TELL_PAGE);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }

        } else {
            Toast.makeText(this, "Save failed :(\nPlease try again", Toast.LENGTH_SHORT).show();
        }

    }
}
