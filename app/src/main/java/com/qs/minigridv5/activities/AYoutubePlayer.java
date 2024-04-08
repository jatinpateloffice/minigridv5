package com.qs.minigridv5.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;

public class AYoutubePlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    String videoKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_youtube_player);

        videoKey = getIntent().getStringExtra("video_key");
        if (videoKey == null || videoKey.isEmpty()) {
            finish();
        }

        final YouTubePlayerView ytpv = findViewById(R.id.a_youtubeplayer_view);
        ytpv.initialize(C.DEVELOPER_KEY, this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        if(videoKey != null) {

            youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
            youTubePlayer.setFullscreen(true);
            youTubePlayer.cueVideo(videoKey);
            youTubePlayer.play();

            Log.i(C.T, "youtube playback started");

        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        finish();

    }

}
