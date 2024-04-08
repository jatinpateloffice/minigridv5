package com.qs.minigridv5.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AProjectEdit;
import com.qs.minigridv5.activities.ASceneEdit;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.utilities.SimpleMuxer;
import com.qs.minigridv5.workers.WVideoViewThumbnailLoader;

import java.util.ArrayList;
import java.util.HashMap;


public class FProjectShowScenes extends Fragment {

    private Project          project;
    private ArrayList<Scene> showScenes;

    private VideoView   currentPlayingVideoView;
    private FrameLayout currentVideoviewIcon;

    private boolean videoPlaying = false;

    private HashMap<String, Bitmap> thumbnails;
    private MyAdapter               adapter;

    public FProjectShowScenes() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        project = ((AProjectEdit) getActivity()).getProject();

        showScenes = project.movieTemplate.getNonSelfieScenes();

        if (thumbnails == null) {
            thumbnails = new HashMap<>();
        }

        if (adapter == null) {
            adapter = new MyAdapter();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        stopCurrentVideoPlayback();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_project_show_scenes, container, false);

        final RecyclerView scenesList = view.findViewById(R.id.f_edit_dikhao_scenes_list);
        scenesList.setLayoutManager(new LinearLayoutManager(getContext()));
        scenesList.setHasFixedSize(true);
        scenesList.setAdapter(new MyAdapter());

        return view;
    }

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

            final View rootView = LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.x_edit_item, parent, false);

            return new ViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder vh, int i) {

            final Scene scene = showScenes.get(i);

            final String videoFile = scene.videoClips.get(0);

            vh.videoView.setVideoPath(videoFile);
            vh.videoView.setTag(videoFile);

            new AProjectEdit.ThumbnailLoader(vh.videoView, thumbnails).execute();

            vh.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopCurrentVideoPlayback();
                }
            });

            vh.audioImg.setVisibility(View.INVISIBLE);

            vh.playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (currentPlayingVideoView != null) {
                        // if other video clip is select to play, pause the current playing one
                        if (currentPlayingVideoView != vh.videoView) {
                            stopCurrentVideoPlayback();
                        }
                    }

                    currentPlayingVideoView = vh.videoView;
                    currentPlayingVideoView.requestFocus();
                    currentPlayingVideoView.setBackground(null);
                    currentVideoviewIcon = vh.playIcon;

                    if (!videoPlaying) {
                        if (scene.videoClips.size() > 1) {

                            // play after muxing
                            final String tmpPlaybackFile = C.TEMP_DIR + "/tmp_c" + scene.ID + "." + C.VIDEO_EXTENSION;
                            new SimpleMuxer(getActivity(), scene.videoClips, tmpPlaybackFile, new SimpleMuxer.SimpleMuxFinishListener() {
                                @Override
                                public void onMuxFinish(boolean success) {

                                    currentPlayingVideoView.setVideoPath(tmpPlaybackFile);
                                    currentPlayingVideoView.start();
                                    currentVideoviewIcon.setVisibility(View.INVISIBLE);
                                    videoPlaying = true;


                                }
                            }).execute();
                        } else {

                            currentPlayingVideoView.start();
                            currentVideoviewIcon.setVisibility(View.INVISIBLE);
                            videoPlaying = true;

                        }
                    } else {
                        stopCurrentVideoPlayback();
                    }
                }
            });

            vh.categoryText.setText(scene.title);

            vh.reRecordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final Intent intent = new Intent(getContext(), ASceneEdit.class);
                    intent.putExtra(ASceneEdit.KEY_PROJECT_ID, project.ID);
                    intent.putExtra(ASceneEdit.KEY_SCENE_ID, scene.ID);
                    startActivity(intent);

                }
            });


        }

        @Override
        public int getItemCount() {
            return showScenes.size();
        }
    }

    private void stopCurrentVideoPlayback() {
        if (currentPlayingVideoView != null) {
            currentPlayingVideoView.pause();
            new AProjectEdit.ThumbnailLoader(currentPlayingVideoView, thumbnails).execute();
        }
        if (currentVideoviewIcon != null) {
            currentVideoviewIcon.setVisibility(View.VISIBLE);
        }
        videoPlaying = false;

    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        ImageView   typeIcon;
        FrameLayout playBtn;
        VideoView   videoView;
        ImageView   audioImg;
        TextView    categoryText;
        Button      reRecordButton;
        FrameLayout playIcon;

        public ViewHolder(View v) {
            super(v);

            typeIcon = v.findViewById(R.id.x_edit_item_type_icon);
            playBtn = v.findViewById(R.id.x_edit_item_play_btn);
            videoView = v.findViewById(R.id.x_edit_item_video);
            audioImg = v.findViewById(R.id.x_edit_item_audio_img);
            categoryText = v.findViewById(R.id.x_edit_category_title);
            reRecordButton = v.findViewById(R.id.x_edit_item_re_record_btn);
            playIcon = v.findViewById(R.id.x_edit_item_play_icon);

        }
    }

}
