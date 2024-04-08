package com.qs.minigridv5.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AAudioRecorder;
import com.qs.minigridv5.activities.ACam;
import com.qs.minigridv5.activities.AProjectEdit;
import com.qs.minigridv5.activities.ASelfiePlaybackScreen;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;

import java.util.ArrayList;
import java.util.HashMap;

public class FProjectTellScenes extends Fragment {


    private Project          project;
    private ArrayList<Scene> tellScenes;

    private MediaPlayer currentAudioMediaPlayer;
    private VideoView   currentPlaybackVideoView;
    private FrameLayout currentVideoPlayingIcon;
    private FrameLayout currentAudioPlayingIcon;
    private String      currentPlaybackAudioFile;

    private boolean videoPlaying = false;
    private boolean audioPlaying = false;

    private HashMap<String, Bitmap> thumbnails;
    private MyAdapter adapter;

    public FProjectTellScenes() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        project = ((AProjectEdit) getActivity()).getProject();

        tellScenes = project.movieTemplate.scenes;

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

        stopCurrentAudioPlayback();
        stopCurrentVideoPlayback();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_project_tell_scenes, container, false);

        RecyclerView scenesList = view.findViewById(R.id.f_edit_tell_scenes_list);
        scenesList.setLayoutManager(new LinearLayoutManager(getContext()));
        scenesList.setHasFixedSize(true);
        scenesList.setAdapter(adapter);

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

            final Scene scene = tellScenes.get(i);

            vh.categoryText.setText(scene.title);

            if (scene.type == Scene.SELFIE) {

                vh.audioImg.setVisibility(View.INVISIBLE);

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

                vh.playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        stopCurrentAudioPlayback();

                        if (currentPlaybackVideoView != null) {
                            // if other video clip is select to play, pause the current playing one
                            if (currentPlaybackVideoView != vh.videoView) {
                                stopCurrentVideoPlayback();
                            }
                        }


                        currentPlaybackVideoView = vh.videoView;
                        currentPlaybackVideoView.setBackground(null);
                        currentVideoPlayingIcon = vh.playIcon;

                        // toggle play and pause upon alternative taps
                        if (!videoPlaying) {
                            currentPlaybackVideoView.start();
                            currentVideoPlayingIcon.setVisibility(View.INVISIBLE);
                            videoPlaying = true;
                        } else {
                            stopCurrentVideoPlayback();
                        }

                    }
                });

                vh.reRecordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ASelfiePlaybackScreen.gotoToNextScene = false;
                        final Intent intent = new Intent(getContext(), ACam.class);
                        intent.putExtra(ACam.KEY_PROJECT_ID, project.ID);
                        intent.putExtra(ACam.KEY_SCENE_ID, scene.ID);
                        intent.putExtra(ACam.KEY_SHOW_PROJECT_PROGRESS, false);
                        startActivity(intent);


                    }
                });

            } else {

                vh.audioImg.setVisibility(View.VISIBLE);
                vh.videoView.setVisibility(View.GONE);
                vh.typeIcon.setImageResource(R.drawable.ic_keyboard_voice_white_24dp);

                vh.playBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        stopCurrentVideoPlayback();

                        final String audioFileToPlay = scene.getAudioFile();

                        // pause any other audio file being played
                        if (currentPlaybackAudioFile != null) {
                            if (!currentPlaybackAudioFile.equals(audioFileToPlay)) {
                                stopCurrentAudioPlayback();
                            }
                        }

                        currentPlaybackAudioFile = audioFileToPlay;
                        currentAudioPlayingIcon = vh.playIcon;

                        if (audioPlaying) {

                            stopCurrentAudioPlayback();

                        } else {

                            currentAudioPlayingIcon.setVisibility(View.INVISIBLE);

                            startAudioPlayBack(audioFileToPlay, new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {

                                    stopCurrentAudioPlayback();


                                }
                            });
                        }


                    }
                });

                vh.reRecordButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        final Intent intent = new Intent(getContext(), AAudioRecorder.class);
                        intent.putExtra(AAudioRecorder.KEY_PROJECT_ID, project.ID);
                        intent.putExtra(AAudioRecorder.KEY_SCENE_ID, scene.ID);
                        intent.putExtra(AAudioRecorder.KEY_EDIT_MODE, true);
                        startActivity(intent);

                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return tellScenes.size();
        }
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

    private void startAudioPlayBack(String audioFile, MediaPlayer.OnCompletionListener onCompletionListener) {

        MediaPlayer mediaPlayer = new MediaPlayer();

        currentAudioMediaPlayer = mediaPlayer;

        mediaPlayer.setOnCompletionListener(onCompletionListener);
        try {
            mediaPlayer.setDataSource(audioFile);
            mediaPlayer.prepare();
            mediaPlayer.start();
            audioPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private void stopCurrentAudioPlayback() {

        if (currentAudioMediaPlayer != null) {
            currentAudioMediaPlayer.seekTo(currentAudioMediaPlayer.getDuration() + 100);
            currentAudioMediaPlayer.setOnCompletionListener(null);
        }
        if (currentAudioPlayingIcon != null) {
            currentAudioPlayingIcon.setVisibility(View.VISIBLE);
        }
        audioPlaying = false;

    }

    private void stopCurrentVideoPlayback() {
        if (currentPlaybackVideoView != null) {

            currentPlaybackVideoView.pause();
            new AProjectEdit.ThumbnailLoader(currentPlaybackVideoView, thumbnails).execute();

        }
        if (currentVideoPlayingIcon != null) {
            currentVideoPlayingIcon.setVisibility(View.VISIBLE);
        }
        videoPlaying = false;
    }


}
