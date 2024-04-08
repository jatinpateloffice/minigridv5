package com.qs.minigridv5.activities;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MovieMuxer;

public class AMuxer extends MyActivity {

    long    project_id;
    Project project;

    private RelativeLayout muxingProcessLayout, allFinishCongo;
    private VideoView animation;
    private MovieMuxer movieMuxer;
    private Button startMuxingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_muxer);

        project_id = getIntent().getLongExtra("proj_id", -1);

        project = myApp.getProjectForID(project_id);

        boolean showAllFinishCongo = ShrePrefs.readData(this, C.sp_all_finish_congo, true);
        allFinishCongo = findViewById(R.id.a_all_complete_congo);
        startMuxingBtn = findViewById(R.id.a_muxer_start_muxing_button);
//        if (showAllFinishCongo) {
//            allFinishCongo.setVisibility(View.VISIBLE);
//            allFinishCongo.setOnClickListener(new View.OnClickListener(){
//
//                @Override
//                public void onClick(View view) {
//                    allFinishCongo.setVisibility(View.GONE);
//                    ShrePrefs.writeData(AMuxer.this, C.sp_all_finish_congo, false);
//                }
//            });
//        }

        animation = findViewById(R.id.a_muxer_making_movie_animation);
        animation.setVideoURI(Uri.parse(Helpers.getAssetString(this, C.MUXING_ANIMATION)));
        animation.start();
        animation.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        muxingProcessLayout = findViewById(R.id.a_muxer_muxing_layout);
        muxingProcessLayout.setVisibility(View.GONE);

        movieMuxer = new MovieMuxer(this, project);

    }

    public void startMuxing(View view) {
        view.setVisibility(View.GONE);
        allFinishCongo.setVisibility(View.GONE);
        animation.setVisibility(View.VISIBLE);

        try {
            movieMuxer.execute();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            movieMuxer = new MovieMuxer(this, project);
            movieMuxer.execute();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (movieMuxer.muxing) {
            movieMuxer.cancel(true);
        }

    }

    @Override
    public void onBackPressed() {

        if (movieMuxer.muxing) {

            new android.support.v7.app.AlertDialog.Builder(this)
                    .setMessage("Cancel making movie?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            startMuxingBtn.setVisibility(View.VISIBLE);
                            allFinishCongo.setVisibility(View.VISIBLE);
                            animation.setVisibility(View.GONE);
                            muxingProcessLayout.setVisibility(View.GONE);
                            movieMuxer.showCancellingDialog();
                            movieMuxer.cancel(true);


                        }
                    })
                    .setNegativeButton("no", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    String getName() {
        return "AMuxer";
    }
}
