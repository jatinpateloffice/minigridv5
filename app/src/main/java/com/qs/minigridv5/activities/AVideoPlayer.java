package com.qs.minigridv5.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.*;
import com.qs.minigridv5.R;

public class AVideoPlayer extends MyActivity implements
        SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    public static final String KEY_FILE_NAME            = "file_name";
    public static final String KEY_CAN_EDIT             = "can_edit";
    public static final String KEY_PROJECT_ID           = "proj_id";
    public static final String KEY_SCENE_ID             = "cat_id";
    public static final String KEY_KEEP_ASPECT_RATIO    = "keep_aspect_ratio";
    public static final String KEY_SHOW_TEXT            = "show_text";
    public static final String KEY_CONTINUE_BTN_TEXT_ID = "continue_btn_text";
    public static final String KEY_FROM_ASSETS          = "from_assets";

    String    videoFileString;
    SeekBar   seekBar;
    VideoView videoView;
    ImageView play_pause_Btn;
    Button    continueBtn, reRecordBtn;
    LinearLayout   controlsContainer;
    RelativeLayout uiContainer;

    Handler  handler        = new Handler();
    Runnable updateTimeTask = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(videoView.getCurrentPosition());
            seekBar.setMax(videoView.getDuration());
            handler.postDelayed(this, 100);
        }
    };

    /**
     * any Activity that calls for this VideoPlayer activity to play a video, should to set this
     * continueClickListener and provide implementation of onContinueClicked() method which specifies
     * what should happen if continue button on the video player is clicked. It is mostly commonly
     * used provide the next Activity to go to.
     */
    private static ContinueClickListener continueClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_video_player);

        View decorView = getWindow().getDecorView();
        int  uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        seekBar = findViewById(R.id.a_video_player_seekbar);

        videoFileString = getIntent().getStringExtra(KEY_FILE_NAME);
        boolean       fromAssets        = getIntent().getBooleanExtra(KEY_FROM_ASSETS, false);
        boolean       canEdit           = getIntent().getBooleanExtra(KEY_CAN_EDIT, false);
        final long     project_id        = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        final int     scene_id          = getIntent().getIntExtra(KEY_SCENE_ID, -1);
        final boolean keepAspectRatio   = getIntent().getBooleanExtra(KEY_KEEP_ASPECT_RATIO, true);
        final int     continueBtnTextID = getIntent().getIntExtra(KEY_CONTINUE_BTN_TEXT_ID, R.string.save);
        boolean       showText          = getIntent().getBooleanExtra(KEY_SHOW_TEXT, false);
        if (!showText) {
            findViewById(R.id.a_video_player_text).setVisibility(View.GONE);
        }

        continueBtn = findViewById(R.id.a_video_player_next_btn);
        continueBtn.setText(continueBtnTextID);
        if (continueBtnTextID == R.string._continue) {
            continueBtn.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    this.getResources().getDrawable(R.drawable.ic_arrow_forward_white_24dp),
                    null
            );
        }

        controlsContainer = findViewById(R.id.a_video_player_controls_container);
        uiContainer = findViewById(R.id.a_video_player_ui_container);
        uiContainer.setOnClickListener(this);

        play_pause_Btn = findViewById(R.id.a_video_player_media_control);
        play_pause_Btn.setOnClickListener(this);

        if (canEdit && project_id > -1 && scene_id > -1) {

            reRecordBtn = findViewById(R.id.a_video_player_edit_btn);
            reRecordBtn.setVisibility(View.VISIBLE);
            reRecordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Intent intent = new Intent(AVideoPlayer.this, ASceneEdit.class);

                    intent.putExtras(AVideoPlayer.this.getIntent());
                    startActivity(intent);


                }
            });


        }


        if (videoFileString != null) {

            videoView = findViewById(R.id.a_video_player_videoview);
            if (fromAssets) {
                videoView.setVideoURI(Uri.parse(videoFileString));
            } else {
                videoView.setVideoPath(videoFileString);
            }
            videoView.seekTo(0);

            if (!keepAspectRatio) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width  = displayMetrics.widthPixels;

                final float newWidth  = width;
                final float newHeight = width * (720f / 480f);

                videoView.setLayoutParams(new RelativeLayout.LayoutParams((int) newWidth, (int) newHeight));
            }

            videoView.start();

            seekBar.setMax(videoView.getDuration());
            seekBar.setProgress(0);
            seekBar.setOnSeekBarChangeListener(this);

            updateProgressBar();

        } else {
            Toast.makeText(this, "No video found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

    }

    @Override
    public void onClick(View view) {

        if (view == uiContainer) {

            if (controlsContainer.getVisibility() == View.VISIBLE) {
                controlsContainer.setVisibility(View.GONE);
                continueBtn.setVisibility(View.GONE);
                if (reRecordBtn != null) {
                    reRecordBtn.setVisibility(View.GONE);
                }

            } else {
                controlsContainer.setVisibility(View.VISIBLE);
                continueBtn.setVisibility(View.VISIBLE);
                if (reRecordBtn != null) {
                    reRecordBtn.setVisibility(View.VISIBLE);
                }
            }

        }

        if (view == play_pause_Btn) {

            if (videoView.isPlaying()) {

                videoView.pause();
                play_pause_Btn.setImageResource(R.drawable.ic_play_arrow_white_24dp);

            } else {

                videoView.start();
                play_pause_Btn.setImageResource(R.drawable.ic_pause_white_24dp);
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        videoView.stopPlayback();
        handler.removeCallbacks(updateTimeTask);
    }

    private void updateProgressBar() {
        handler.postDelayed(updateTimeTask, 100);
    }

    public static void setContinueClickListener(ContinueClickListener continueClickListener) {
        AVideoPlayer.continueClickListener = continueClickListener;
    }

    public void onContinueClick(View view) {

        if (continueClickListener != null) {
            continueClickListener.onContinueClicked(this);
            setContinueClickListener(null);
        } else {
            finish();
        }

    }

    @Override
    String getName() {
        return "AVideoPlayer";
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(updateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        handler.removeCallbacks(updateTimeTask);
        videoView.seekTo(seekBar.getProgress());
        updateProgressBar();
        if (!videoView.isPlaying()) {
            videoView.start();
            play_pause_Btn.setImageResource(R.drawable.ic_pause_white_24dp);
        }
    }

    public interface ContinueClickListener {
        void onContinueClicked(Activity activity);
    }

}
