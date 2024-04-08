package com.qs.minigridv5.workers;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.VideoView;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.Helpers;

public class WVideoViewThumbnailLoader extends AsyncTask<Void, Void, Bitmap> {


    private VideoView videoView;


    public WVideoViewThumbnailLoader(VideoView videoView) {

        this.videoView = videoView;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        try {
            final String videoFile = (String) videoView.getTag();
            return Helpers.createThumbnailAtTime(videoFile, 100);
        } catch (Exception e) {
            Crashlytics.logException(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (bitmap != null) {
            videoView.setBackground(new BitmapDrawable(bitmap));
        }

        videoView = null;

    }
}
