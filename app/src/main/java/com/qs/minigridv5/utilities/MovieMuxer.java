package com.qs.minigridv5.utilities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.ANameMovie;
import com.qs.minigridv5.activities.AVideoPlayer;
import com.qs.minigridv5.activities.MyActivity;
import com.qs.minigridv5.entities.Movie;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.qs.minigridv5.misc.Helpers.getTimeInSecs;

public class MovieMuxer extends AsyncTask<Void, Integer, Boolean> {

    private final   Project                 project;
    private WeakReference<MyActivity> activity;

    private MyMuxer muxer;
    private long videoTimeOffset = 0, audioTimeOffset = 0;

    // UI stuff
    private int noofFilesToProcess;
    private AlertDialog cancellingDialog;

    public boolean muxing = false;

    private Movie movie;

    public MovieMuxer(final MyActivity activity, Project project) {

        this.activity = new WeakReference<>(activity);
        this.project = project;
        this.movie = new Movie(project);
        activity.myApp.getMovies().add(movie);

        cancellingDialog = new android.support.v7.app.AlertDialog.Builder(activity)
                .setMessage("Cancelling...")
                .setCancelable(false)
                .create();

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        final RelativeLayout muxingLayout = activity.get().findViewById(R.id.a_muxer_muxing_layout);
        final ProgressBar progressDialog = activity.get().findViewById(R.id.a_muxer_progress_bar);

        muxingLayout.setVisibility(View.VISIBLE);
        noofFilesToProcess = 0;
        for (Scene c : project.movieTemplate.scenes) {
            if (c.type == Scene.SELFIE) {
                noofFilesToProcess += c.videoClips.size();
            } else {
                noofFilesToProcess += c.videoClips.size();
                noofFilesToProcess++; // for audio file
            }
        }
        progressDialog.setMax(noofFilesToProcess);

    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        try {

            muxing = true;
            return doTheMux();

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            if(muxer != null) {
                muxer.stopAndRelease();
            }
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);

        muxing = false;

        if (success) {

            final Intent intent = new Intent(activity.get(), AVideoPlayer.class);
            intent.putExtra(AVideoPlayer.KEY_FILE_NAME, movie.getMovieFilePath());
            intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
            intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
            activity.get().startActivity(intent);

            // set movie thumbnail
            movie.thumbnail = Helpers.createThumbnailAtTime(movie.getMovieFilePath(), 1);

            AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                @Override
                public void onContinueClicked(Activity activity) {

                    final Intent intent = new Intent(activity, ANameMovie.class);
                    intent.putExtra(ANameMovie.KEY_PROJECT_ID, project.ID);
                    intent.putExtra(ANameMovie.KEY_MOVIE_ID, movie.ID);
                    activity.startActivity(intent);

                }
            });


        } else {
            Toast.makeText(activity.get(), "Failed :(", Toast.LENGTH_SHORT).show();
            // delete the muxing-failed movieTemplate file
            final File file = movie.getMovieFile();
            if (file.exists()) {
                file.delete();
            }

        }
    }

    @Override
    protected void onCancelled(Boolean muxingComplete) {
        super.onCancelled(muxingComplete);

        muxing = false;

        try {
            cancellingDialog.dismiss();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        if (!muxingComplete) {

            final File file = movie.getMovieFile();
            if (file.exists()) {
                file.delete();
            }

        }


    }

    public void showCancellingDialog() {
        cancellingDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        final ProgressBar progressDialog = activity.get().findViewById(R.id.a_muxer_progress_bar);
        final TextView progressBarText = activity.get().findViewById(R.id.a_muxer_progress_text);
        progressDialog.setProgress(values[0]);
        progressBarText.setText(values[0] + " / " + noofFilesToProcess);
    }

    private boolean doTheMux() throws Exception {

        ArrayList<Scene> scenes         = project.movieTemplate.scenes;
        int              processedFiles = 0;

        //<editor-fold desc="setup Muxer and tracks">
        muxer = new MyMuxer(movie.getMovieFilePath());

        // tracks
        final int[] tracks     = setupTracks();
        final int   videoTrack = tracks[0];
        final int   audioTrack = tracks[1];
        //</editor-fold>

        muxer.start();
        publishProgress(0);

        for (Scene scene : scenes) {

            if(isCancelled()) {
                muxer.stopAndRelease();
                return false;
            }

            final boolean selfieCategory = scene.type == Scene.SELFIE;

            Log.i(C.T, "Scene: " + scene.title + ", selfie: " + (selfieCategory ? "Yes" : "No"));

            if (selfieCategory) {

                //<editor-fold desc="mux video in selfie category">

                String videoFile = scene.videoClips.get(0);

                videoTimeOffset += muxer.addSamples(videoFile, videoTrack, videoTrack, videoTimeOffset, -1);

                audioTimeOffset += muxer.addSamples(videoFile, audioTrack, audioTrack, audioTimeOffset, -1);

                publishProgress(++processedFiles);

                //</editor-fold>

            } else {

                final long sceneLength = scene.calculateAllVideosLength();

                final long audioLength   = Helpers.calculateClipLength(scene.getAudioFile());
                final long bgTrackLength = (sceneLength - audioLength) * 1000;
                final long bgTimeLimit   = audioTimeOffset + (bgTrackLength);

                final ArrayList<String> sceneVideoClips = scene.videoClips;

                // mux only video first and it's background audio
                for (String videoClip : sceneVideoClips) {

                    publishProgress(++processedFiles);

                    videoTimeOffset += muxer.addSamples(videoClip, videoTrack, videoTrack, videoTimeOffset, -1);

                    // extract and mux video clip's audio data i.e. background track

                    audioTimeOffset += muxer.addSamples(videoClip, audioTrack, audioTrack, audioTimeOffset, bgTimeLimit);


                }

                audioTimeOffset += muxer.addSamples(scene.getAudioFile(), 0, audioTrack, audioTimeOffset, videoTimeOffset);
                publishProgress(++processedFiles);

            }

        }
        Log.e(C.T, "\nvo: " + getTimeInSecs(videoTimeOffset) + "\nao: " + getTimeInSecs(audioTimeOffset));

        muxer.stopAndRelease();

        return true;

    }

    private int[] setupTracks() throws Exception {

        final int[] tracks = new int[2];

        // get a non-selfie scene
        int   catIdx = 0;
        Scene scene  = project.movieTemplate.scenes.get(catIdx);
        while (scene.type == Scene.SELFIE) {
            catIdx++;
            scene = project.movieTemplate.scenes.get(catIdx);
        }

        final MediaExtractor videoExtractor = new MediaExtractor();
        final MediaExtractor audioExtractor = new MediaExtractor();

        videoExtractor.setDataSource(scene.videoClips.get(0));
        audioExtractor.setDataSource(scene.getAudioFile());

        final MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
        final MediaFormat audioFormat = audioExtractor.getTrackFormat(0);

        tracks[0] = muxer.addTrack(videoFormat);
        tracks[1] = muxer.addTrack(audioFormat);

        videoExtractor.release();
        audioExtractor.release();

        return tracks;

    }


}
