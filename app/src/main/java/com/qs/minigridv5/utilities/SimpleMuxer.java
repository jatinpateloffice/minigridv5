package com.qs.minigridv5.utilities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;

import java.util.ArrayList;

/**
 * input => video files, output => one muxed video file
 */

public class SimpleMuxer extends AsyncTask<Void, Void, Boolean> {

    ArrayList<String> videoFiles;
    String dstFile;
    Activity activity;
    SimpleMuxFinishListener smfl;

    MyMuxer muxer;
    long videoTimeOffset = 0, audioTimeOffset = 0;

    // UI stuff
    ProgressDialog progressDialog;

    public SimpleMuxer(Activity activity, ArrayList<String> videoFiles, String dstFile, SimpleMuxFinishListener smfl) {
        this.videoFiles = videoFiles;
        this.dstFile = dstFile;
        this.activity = activity;
        this.smfl = smfl;

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getResources().getString(R.string.starting_playback));
        progressDialog.setCancelable(false);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        try {
            doTheMux();
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            if(muxer != null) {
                muxer.stopAndRelease();
            }
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        progressDialog.dismiss();

        smfl.onMuxFinish(aBoolean);

    }

    private void doTheMux() throws Exception {

        //<editor-fold desc="setup Muxer and Extractor Stuff">
        muxer = new MyMuxer(dstFile);


        final int[] tracks = setupTracks();
        final int videoTrack = tracks[0];
        final int audioTrack = tracks[1];

        //</editor-fold>

        muxer.start();

        // mux only video first
        for (String videoFile : videoFiles) {

            videoTimeOffset += muxer.addSamples(videoFile, videoTrack, videoTrack, videoTimeOffset, -1);

            audioTimeOffset += muxer.addSamples(videoFile, audioTrack, audioTrack, audioTimeOffset, videoTimeOffset);

        }

        muxer.stopAndRelease();

    }

    private int[] setupTracks() throws Exception {

        final int[] tracks = new int[2];

        final MediaExtractor extractor = new MediaExtractor();

        extractor.setDataSource(videoFiles.get(0));
        final MediaFormat videoFormat = extractor.getTrackFormat(0);
        final MediaFormat audioFormat = extractor.getTrackFormat(1);
        tracks[0] = muxer.addTrack(videoFormat);
        tracks[1] = muxer.addTrack(audioFormat);

        extractor.release();

        return tracks;

    }

    public interface SimpleMuxFinishListener {
        void onMuxFinish(boolean success);
    }

}
