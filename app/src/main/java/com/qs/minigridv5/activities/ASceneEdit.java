package com.qs.minigridv5.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;

public class ASceneEdit extends MyActivity implements View.OnClickListener, DiscreteScrollView.ScrollStateChangeListener<ASceneEdit.MyAdapter.ViewHolder> {

    public static final String KEY_PROJECT_ID = "proj_id";
    public static final String KEY_SCENE_ID = "cat_id";

    long   project_id;
    int   scene_id;
    Scene scene;

    TextView           sceneText;
    DiscreteScrollView clipsList;
    ImageView          closeBtn;
    ImageView          helpBtn;
    Button             deleteAllBtn;
    Button             reRecordBtn;
    RecyclerView       dotsList;
    ImageView          nextClipBtn, prevClipBtn;


    float videoViewWidth, videoViewHeight;
    String currentVideoPlaying;
    int    currentClipPosition = 0;

    public FrameLayout introOverlay;
    public TextView    naiSamjaText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_scene_edit);

        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        scene_id = getIntent().getIntExtra(KEY_SCENE_ID, -1);

        final Project project = myApp.getProjectForID(project_id);
        scene = project.movieTemplate.getSceneWithID(scene_id);

        sceneText = findViewById(R.id.a_scene_edit_scene_text);
        closeBtn = findViewById(R.id.a_scene_edit_close);
        helpBtn = findViewById(R.id.a_scene_edit_help);
        deleteAllBtn = findViewById(R.id.a_scene_edit_delete_all_btn);
        reRecordBtn = findViewById(R.id.a_scene_edit_re_record_btn);
        nextClipBtn = findViewById(R.id.a_scene_edit_next_clip_btn);
        prevClipBtn = findViewById(R.id.a_scene_edit_prev_clip_btn);

        closeBtn.setOnClickListener(this);
        helpBtn.setOnClickListener(this);
        deleteAllBtn.setOnClickListener(this);
        reRecordBtn.setOnClickListener(this);
        nextClipBtn.setOnClickListener(this);
        prevClipBtn.setOnClickListener(this);
        prevClipBtn.setVisibility(View.INVISIBLE);
        if (scene.videoClips.size() <= 1) {
            nextClipBtn.setVisibility(View.INVISIBLE);
        }

        sceneText.setText(scene.title);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int width = displayMetrics.widthPixels;
        videoViewWidth = (width * (0.5f));
        videoViewHeight = (width * (720f / 480f));

        final int padding = (int) ((width / 2) - (videoViewWidth / 2));
        clipsList = findViewById(R.id.a_scene_edit_clips_list);
        clipsList.setAdapter(new MyAdapter());
        clipsList.setPaddingRelative(padding, 0, padding, 0);
        clipsList.addScrollStateChangeListener(this);
        clipsList.setItemTransitionTimeMillis(100);
        clipsList.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1f)
                .setMinScale(0.8f)
                .build());

        dotsList = findViewById(R.id.a_scene_edit_dots_container);
        dotsList.setAdapter(new MyDotsAdapter());
        dotsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        introOverlay = findViewById(R.id.a_scene_edit_intro_overlay);
        introOverlay.setOnClickListener(this);
        naiSamjaText = findViewById(R.id.a_scene_edit_nai_samja_text);
        naiSamjaText.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    String getName() {
        return "ASceneEdit";
    }

    @Override
    public void onClick(View view) {

        if (view == closeBtn) {

            finish();

        }

        if (view == helpBtn) {

            introOverlay.setVisibility(View.VISIBLE);
        }

        if (view == reRecordBtn) {

            final Intent intent = new Intent(this, AClipCam.class);
            intent.putExtra(AClipCam.KEY_PROJECT_ID, project_id);
            intent.putExtra(AClipCam.KEY_SCENE_ID, scene_id);
            intent.putExtra(AClipCam.KEY_CLIP_FILENAME, currentVideoPlaying);
            startActivity(intent);

        }

        if (view == nextClipBtn) {

            if (currentClipPosition < scene.videoClips.size() - 1) {
                clipsList.smoothScrollToPosition(currentClipPosition + 1);
            }

        }

        if (view == prevClipBtn) {


            if (currentClipPosition > 0) {
                clipsList.smoothScrollToPosition(currentClipPosition - 1);
            }

        }

        if (view == deleteAllBtn) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    this,
                    R.style.MyLightDialog
            );
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    boolean deleteSuccess = true;
                    int noofClips = 0;
                    for (String clip : scene.videoClips) {

                        final File file = new File(clip);

                        if (!file.delete()) {
                            deleteSuccess = false;
                        } else {
                            noofClips++;
                        }

                    }
                    if (deleteSuccess) {

                        final Project project = myApp.getProjectForID(project_id);
                        project.updateCompleteStatus();

                        // return to camera
                        final Intent intent = new Intent(ASceneEdit.this, ACam.class);
                        intent.putExtra(ACam.KEY_SHOW_PROJECT_PROGRESS, false);
                        intent.putExtras(getIntent());
                        startActivity(intent);


                        scene.remainingVideoClipLengthMs = scene.video_length;
                        scene.videoClips.clear();
                        Toast.makeText(ASceneEdit.this, noofClips + " clips deleted!", Toast.LENGTH_SHORT).show();
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

        // overlays
        if (view == introOverlay) {
            introOverlay.setVisibility(View.GONE);
        }

        if (view == naiSamjaText) {

            introOverlay.setVisibility(View.GONE);
            final String dikhoTutorialVideo = Helpers.getAssetString(this, C.EDIT_TUTORIAL_VIDEO_NAME);

            final Intent intent = new Intent(this, AVideoPlayer.class);
            intent.putExtra(AVideoPlayer.KEY_FILE_NAME, dikhoTutorialVideo);
            intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
            intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
            intent.putExtra(AVideoPlayer.KEY_KEEP_ASPECT_RATIO, false);
            intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
            startActivity(intent);

            AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                @Override
                public void onContinueClicked(Activity activity) {

                    ShrePrefs.writeData(activity, C.sp_show_dekho_tuto, false);

                    final Intent intent = new Intent(activity, ASceneEdit.class);
                    intent.putExtras(getIntent());
                    activity.startActivity(intent);

                }
            });


        }

    }

    @Override
    public void onScrollStart(@NonNull MyAdapter.ViewHolder viewHolder, int i) {

        viewHolder.videoView.pause();
        viewHolder.mask.setVisibility(View.VISIBLE);

        final MyDotsAdapter.ViewHolder dotVH = ((MyDotsAdapter.ViewHolder) dotsList.getChildViewHolder(dotsList.getLayoutManager().findViewByPosition(i)));
        if (dotVH != null) {
            dotVH.dot.setBackgroundResource(R.drawable.ic_radio_button_unchecked_white_24dp);
        }

    }

    @Override
    public void onScrollEnd(@NonNull MyAdapter.ViewHolder viewHolder, int i) {

        currentClipPosition = i;

        viewHolder.videoView.seekTo(0);
        viewHolder.videoView.start();
        viewHolder.mask.setVisibility(View.INVISIBLE);

        currentVideoPlaying = scene.videoClips.get(i);
        final MyDotsAdapter.ViewHolder dotVH = ((MyDotsAdapter.ViewHolder) dotsList.getChildViewHolder(dotsList.getLayoutManager().findViewByPosition(i)));
        if (dotVH != null) {
            dotVH.dot.setBackgroundResource(R.drawable.indicator_selected);
        }


        prevClipBtn.setVisibility(i > 0 ? View.VISIBLE : View.INVISIBLE);

        nextClipBtn.setVisibility(i < scene.videoClips.size() - 1 ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public void onScroll(float v, int i, int i1, @Nullable MyAdapter.ViewHolder currVH, @Nullable MyAdapter.ViewHolder newVH) {

    }

    public class MyAdapter extends RecyclerView.Adapter<ASceneEdit.MyAdapter.ViewHolder> {


        public MyAdapter() {

        }

        @Override
        public ASceneEdit.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.x_scene_edit_clip_item, parent, false);

            return new ASceneEdit.MyAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ASceneEdit.MyAdapter.ViewHolder holder, int pos) {

            final int position = holder.getAdapterPosition();

            final String videoFile = scene.videoClips.get(position);
            holder.videoView.setVideoPath(videoFile);
            holder.videoView.seekTo(100);
            if (position == 0) {
                holder.videoView.start();
                holder.mask.setVisibility(View.INVISIBLE);
                currentVideoPlaying = videoFile;
            } else {
                holder.videoView.stopPlayback();
                holder.mask.setVisibility(View.VISIBLE);
            }
            holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {

//                    final int newPosition = position < (getItemCount() - 1) ? 0 : position + 1;
                    int newPosition = 0;
//                    if (position < (getItemCount() - 1)) {
//
//                        clipsList.smoothScrollToPosition(position + 1);
//                    }


                }
            });

            // calculate the time of the clip
            new ClipDataLoader(scene.videoClips.get(position), holder.timeText).execute();

        }

        @Override
        public int getItemCount() {

            return scene.videoClips.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView  timeText;
            public ImageView mask;
            public VideoView videoView;


            public ViewHolder(View v) {
                super(v);

                timeText = v.findViewById(R.id.x_scene_edit_clip_time_text);
                mask = v.findViewById(R.id.x_scene_edit_clip_mask);
                videoView = v.findViewById(R.id.x_scene_edit_clip_videoview);

                Log.e(C.T, "w: " + videoViewWidth + ", h: " + videoViewHeight);
                videoView.setLayoutParams(
                        new FrameLayout.LayoutParams(
                                (int) videoViewWidth, (int) videoViewHeight)
                );


            }


        }

        class ClipDataLoader extends AsyncTask<Void, Void, Integer> {

            String   videoFile;
            TextView textView;


            public ClipDataLoader(String videoFile, TextView textView) {
                this.videoFile = videoFile;
                this.textView = textView;
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                Log.i(C.T, "fetching image for " + videoFile);

                return (int) Math.ceil(Helpers.calculateClipLength(videoFile) / 1000f);

            }

            @Override
            protected void onPostExecute(Integer time) {
                super.onPostExecute(time);

                if (textView != null) {
                    textView.setText(time + "s");
                }

            }
        }
    }

    public class MyDotsAdapter extends RecyclerView.Adapter<ASceneEdit.MyDotsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.x_dot_item, parent, false);

            return new ASceneEdit.MyDotsAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

            if (i == 0) {

                viewHolder.dot.setBackgroundResource(R.drawable.indicator_selected);

            }

        }

        @Override
        public int getItemCount() {
            return scene.videoClips.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView dot;

            public ViewHolder(View itemView) {
                super(itemView);

                dot = itemView.findViewById(R.id.x_dot);

            }
        }
    }

}
