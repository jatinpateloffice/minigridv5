package com.qs.minigridv5.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.MyActivity;
import com.qs.minigridv5.entities.Movie;
import com.qs.minigridv5.fragments.FLocalMovies;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class VideoUploader extends AsyncTask<Void, Object, String> {

    private WeakReference<MyActivity> activity;

    private Movie movie;

    private FLocalMovies.MovieViewHolder vh;
    private HashMap<String, String>      formFields;

    private MultipartPOSTRequester multipart;

    public boolean isUploading = false;

    public final String cancelledResponseString  = "{\"responses\":600,\"description\":\"Could not complete the request due to invalid header\"}";
    public final String errorResponseString      = "{\"responses\":500,\"description\":\"Could not complete the request due to invalid header\"}";
    public final String noInternetResponseString = "{\"responses\":700,\"description\":\"Could not complete  the request due to invalid header\"}";

    public final int ERROR_INTERNET_LOST    = -1;
    public final int ERROR_UPLOAD_CANCELLED = -2;

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!Helpers.isNetworkAvailable(context)) {
                isUploading = false;
                vh.uploadStatusText.setText("Internet connection lost.");
                vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.red));
            }

        }
    };

    public VideoUploader(final MyActivity activity, final HashMap<String, String> formFields, FLocalMovies.MovieViewHolder vh, Movie movie) {
        this.formFields = formFields;
        this.activity = new WeakReference<>(activity);
        this.movie = movie;
        this.vh = vh;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.vh.uploadStatusContainer.setVisibility(View.VISIBLE);
        this.vh.progressStuffContainer.setVisibility(View.VISIBLE);
        this.vh.uploadStatusText.setText(R.string.upload_happening);
        this.vh.publishButton.setVisibility(View.GONE);
        this.vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.translu_dark_blue));
        this.vh.uploadProgressBar.setMax((int) movie.getMovieFile().length());
        this.vh.uploadProgressBar.setIndeterminate(true);
        this.vh.cancelUploadButton.setVisibility(View.VISIBLE);
        vh.deleteBtn.setVisibility(View.GONE);

        isUploading = true;

        registerNetworkListener();
        publishProgress("waiting for upload...", -1);
    }

    @Override
    protected void onProgressUpdate(final Object... values) {
        super.onProgressUpdate(values);

        // values[0] = status text
        // values[1] = upload progress

        activity.get().runOnUiThread(new Runnable() {

            @Override
            public void run() {

                vh.uploadStatusText.setText(values[0].toString());
                if ((int) values[1] >= 0) {
                    vh.uploadProgressBar.setIndeterminate(false);
                    vh.uploadProgressBar.setProgress((Integer) values[1]);
                } else {
                    vh.uploadProgressBar.setIndeterminate(true);
                }


            }
        });

    }

    @Override
    protected String doInBackground(Void... voids) {

        Thread.currentThread().setName("video uploader thread");
        String charset    = "UTF-8";
        String requestURL = C.API_VIDEO_UPLOAD;

        try {

            final String auth_key = ShrePrefs.readData(activity.get(), C.sp_authorisation_key, null);
            multipart = new MultipartPOSTRequester(this, requestURL, charset, auth_key);

            for (Map.Entry<String, String> e : formFields.entrySet()) {
                multipart.addFormField(e.getKey(), e.getValue());
            }
            int fileUploadStatus = multipart.addFilePart("videoFile", movie.getMovieFile());
            switch (fileUploadStatus) {
                case ERROR_INTERNET_LOST:
                    return noInternetResponseString;
                case ERROR_UPLOAD_CANCELLED:
                    return cancelledResponseString;
                default:
                    final String response = multipart.finish(); // response from server.
                    Log.e(C.T, "response: " + response);
                    return response;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String responseString) {
        super.onPostExecute(responseString);

        isUploading = false;
        unRegisterNetworkListener();

        try {

            // first reset the basic UI
            resetUI();


            final JSONObject json = new JSONObject(responseString);
            int              code = json.getInt("responses");

            switch (code) {
                case 200:

                    final JSONObject data = json.getJSONObject("data");

                    final String youtubeLink = getDataFromJSON(data, "youtube_link", "NA");
                    final int videoId = getDataFromJSON(data, "video_id", -1);

                    this.vh.publishButton.setVisibility(View.GONE);
                    this.vh.uploadStatusText.setText(R.string.awating_approval);
                    this.vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.orange));

                    movie.movieStatus = C.MOVIE_STATUS_UPLOADED;
                    
                    movie.setVideoID(videoId);
                    movie.setYoutubeLink(youtubeLink);
                    movie.saveIntoJson();

                    break;

                case 400:
                    this.vh.uploadStatusText.setText("Error uploading due to incomplete data.");
                    this.vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.red));
                    break;

                case 500:
                    this.vh.uploadStatusText.setText("Internal Server Error.");
                    this.vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.red));
                    break;
                case 700:
                    this.vh.uploadStatusText.setText("Internet connection lost.");
                    this.vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.red));
                    break;
                default:
                    this.vh.uploadStatusText.setText("Something went wrong. Try again.");
                    this.vh.uploadStatusContainer.setBackgroundColor(activity.get().getResources().getColor(R.color.red));

            }

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

    }

    private String getDataFromJSON(final JSONObject data, final String key, final String defaultValue) {

        try {
            return data.getString(key);
        } catch (JSONException e) {
            Crashlytics.logException(e);
            return defaultValue;
        }

    }

    private int getDataFromJSON(final JSONObject data, final String key, final int defaultValue) {

        try {
            return data.getInt(key);
        } catch (JSONException e) {
            Crashlytics.logException(e);
            return defaultValue;
        }

    }

    private void terminateUpload() {
        if (multipart != null) {
            try {
                multipart.terminateUpload();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        terminateUpload();
        this.vh.uploadStatusContainer.setVisibility(View.GONE);
        resetUI();
        isUploading = false;
        unRegisterNetworkListener();
    }

    private void resetUI() {

        this.vh.progressStuffContainer.setVisibility(View.GONE);
        this.vh.cancelUploadButton.setVisibility(View.GONE);
        this.vh.publishButton.setVisibility(View.VISIBLE);
        this.vh.deleteBtn.setVisibility(View.VISIBLE);

    }

    private void registerNetworkListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        activity.get().registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unRegisterNetworkListener() {
        activity.get().unregisterReceiver(networkChangeReceiver);
    }


}
