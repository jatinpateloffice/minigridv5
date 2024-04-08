package com.qs.minigridv5.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Company;
import com.qs.minigridv5.entities.Movie;
import com.qs.minigridv5.entities.State;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.workers.WFileMover;
import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static com.android.volley.toolbox.Volley.newRequestQueue;
import static com.qs.minigridv5.activities.AMain.states;

public class ASplash extends MyActivity {

    private final int THE_REQUEST_CODE = 289;

    private StuffLoader stuffLoader;

    private final ArrayList<StringRequest> requests            = new ArrayList<>();
    private       int                      noofPendingRequests = 0;

    private DefaultRetryPolicy retryPolicy;
    private ImageView          bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.a_splash);

        hideStatusBar();

        AMain.states = new ArrayList<>();

        bg = findViewById(R.id.a_splash_bg);
        retryPolicy = new DefaultRetryPolicy();// timeout = 2500ms

        int random       = new Random().nextInt(3);
        int splashImgRes = R.drawable.splash_bg_3;
        switch (random) {
            case 0:
                splashImgRes = R.drawable.splash_bg_1;
                break;
            case 1:
                splashImgRes = R.drawable.splash_bg_2;
                break;
            default:
                break;
        }
        Glide.with(this).load(splashImgRes).into(bg);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, THE_REQUEST_CODE);
        } else {
            stuffLoader = new StuffLoader();
            stuffLoader.execute();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == THE_REQUEST_CODE) {

            for (int i = 0; i < permissions.length; i++) {

                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        stuffLoader = new StuffLoader();
                        stuffLoader.execute();

                    } else {

                        Helpers.showAlert(
                                this,
                                false,
                                "External Storage is required to save your video, audio clips and movieTemplate files. Please grant the permission to write and read your SD Card.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        ActivityCompat.requestPermissions(ASplash.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, THE_REQUEST_CODE);
                                    }
                                });

                    }

                }

            }

        }


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (bg != null) {
            bg.setImageBitmap(null);
        }

    }

    @Override
    String getName() {
        return "ASplash";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (stuffLoader != null) {
            stuffLoader.cancel(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finishAffinity();
        }

    }

    class StuffLoader extends AsyncTask<Void, Void, Void> implements RequestQueue.RequestFinishedListener<StringRequest> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /**
             * code: https://github.com/google/volley/blob/master/src/main/java/com/android/volley/RequestQueue.java
             */

            myApp.getRequestQueue().addRequestFinishedListener(this);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            // first check permissions
            if (isCancelled()) return null;
            checkPermissions();

            // check for settings
            if (isCancelled()) return null;
            checkForSettings();

            // setup file structure
            if (isCancelled()) return null;
            setupFileStructureAndFiles();

            // load movieTemplate data based on selected language
            if (isCancelled()) return null;
            loadMovieDataFromJSON();

            if (isCancelled()) return null;
            loadProjects();

            if (isCancelled()) return null;
            loadMoviesData();

            if (isCancelled()) return null;
            updateMovieStatus();

            if (isCancelled()) return null;
            requestAndUpdateBackendData();

            if (isCancelled()) return null;

            // wait for pending requests to clear before moving all
            while (noofPendingRequests > 0) {

                if (isCancelled()) return null;

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            myApp.getRequestQueue().removeRequestFinishedListener(this);
            final Intent intent = new Intent(ASplash.this, getActivityToGoto());
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);


        }

        @Override
        public void onRequestFinished(Request<StringRequest> request) {

            noofPendingRequests--;
            Log.i(C.T, "noof Requests: " + noofPendingRequests + ", delivered " + request.getUrl());

        }

    }

    private Class<? extends AppCompatActivity> getActivityToGoto() {

        final boolean loadLangSelect = ShrePrefs.readData(ASplash.this, C.sp_load_lang_select, true);
        if (loadLangSelect) {
            return ALanguageSelect.class;
        }

        final boolean loadCaro = ShrePrefs.readData(ASplash.this, C.sp_load_intro_caro, true);
        if (loadCaro) {
            return AIntroCarousel.class;
        }

        final boolean loadSignup = ShrePrefs.readData(ASplash.this, C.sp_load_signup, true);
        if (loadSignup) {
            return ASignUp.class;
        }

        return AMain.class;


    }


    private void checkPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE},
                    1);
        }

    }

    private void setupFileStructureAndFiles() {

        C.INTERNAL_ROOT = getFilesDir().getAbsolutePath() + "/" + C.WORKING_FOLDER;
        C.EXTERNAL_ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/" + C.WORKING_FOLDER;

        C.PROJECTS_DIR = C.EXTERNAL_ROOT + "/projects";
        C.COMMON_DIR = C.EXTERNAL_ROOT + "/common";
        C.MOVIES_EXAMPLES_DIR = C.EXTERNAL_ROOT + "/movies_egs";
        C.TEMP_DIR = C.EXTERNAL_ROOT + "/temp";
        C.MOVIES_DIR = C.EXTERNAL_ROOT + "/movies";

        Helpers.clearTempFiles();

        // create root, movies, common directories
        String[] directoryList = {
                C.INTERNAL_ROOT,
                C.EXTERNAL_ROOT,
                C.PROJECTS_DIR,
                C.COMMON_DIR,
                C.TEMP_DIR,
                C.MOVIES_DIR
        };

        for (String directory : directoryList) {

            final File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

        }

        // TODO extract thumbnail images from learn videos and store

        boolean testFolderOrg = false;
        if(testFolderOrg) {
            File file = new File(Environment.getExternalStorageDirectory(), "tempMGS");
            if (!file.exists()) {
                file.mkdir();
            }
            try {
                final File tmpFile = new File(C.MOVIES_DIR).listFiles()[0];
                new WFileMover(tmpFile.getAbsolutePath(), file.getAbsolutePath() + "/t.mp4", false, new WFileMover.FileMoveListener() {
                    @Override
                    public void onMovingComplete(boolean success) {
                        Log.d(C.T, "done moving");
                    }
                }).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(C.T, "crash");
            }
        }

        Log.d(C.T, "Internal:\n" + Helpers.viewFileStructure(C.INTERNAL_ROOT));
        Log.d(C.T, "External:\n" + Helpers.viewFileStructure(C.EXTERNAL_ROOT));

    }

    private void requestAndUpdateBackendData() {

        if (!Helpers.isNetworkAvailable(this)) {

            fillupCompanyArrayFromSharedPrefs();
            fillupStatesArrayFromSharedPrefs();

            return;
        }


        StringRequest companyListRequest = new StringRequest(
                Request.Method.GET,
                C.API_COMPANY_LIST_DEMO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            Log.e(C.T, "company list response: " + response);

                            fillupCompanyArray(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            fillupCompanyArrayFromSharedPrefs();

                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(ASplash.this, "Error fetching Company List", Toast.LENGTH_SHORT).show();
                        fillupCompanyArrayFromSharedPrefs();
                        Crashlytics.logException(error.fillInStackTrace());

                    }
                });

        StringRequest stateListRequest = new StringRequest(
                Request.Method.GET,
                C.API_STATE_LIST_DEMO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            fillupStatesArray(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                            fillupStatesArrayFromSharedPrefs();

                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        fillupStatesArrayFromSharedPrefs();

                    }
                });

        StringRequest featuredVideoRequest = new StringRequest(
                Request.Method.GET,
                C.API_FEATURED_VIDEO_DEMO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(C.T, "recieved featured videos response: " + response);
                        ShrePrefs.writeData(ASplash.this, C.sp_featured_videos_response, response);


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Crashlytics.logException(error.fillInStackTrace());
                    }
                });
        StringRequest userVideoRequest = new StringRequest(
                Request.Method.GET,
                C.API_USER_VIDEO_DEMO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(C.T, "recieved user videos response: " + response);
                        ShrePrefs.writeData(ASplash.this, C.sp_user_videos_response, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Crashlytics.logException(error.fillInStackTrace());
                    }
                });

        makeARequest(companyListRequest);
        makeARequest(stateListRequest);
        makeARequest(featuredVideoRequest);
        makeARequest(userVideoRequest);

    }

    private void updateMovieStatus() {

        if (!Helpers.isNetworkAvailable(this)) {
            return;
        }

        final HashMap<Integer, Movie> platfromMovies = new HashMap<>();
        for (Movie movie : myApp.getMovies()) {

            if (movie.getVideoId() > 0) {
                platfromMovies.put(movie.getVideoId(), movie);
            }

        }


        for (final int videoId : platfromMovies.keySet()) {


            final StringRequest request = new StringRequest(
                    Request.Method.GET,
                    C.API_MOVIE_STATUS + videoId,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {

                                Log.e(C.T, "\nresponse for video_id  " + videoId + " - " + response);
                                JSONObject jsonData = new JSONObject(response).getJSONArray("data").getJSONObject(0);

                                final String youtube_id = jsonData.getString("youtube_id");
                                final int    mStatus    = jsonData.getInt("video_status");
                                String       comments   = jsonData.getString("admin_remarks");


                                int localStatus = C.MOVIE_STATUS_UPLOADED;

                                if (mStatus == C.MOVIE_UPLOADED_RESPONSE || mStatus == C.MOVIE_AWAITING_RESPONSE) {
                                    localStatus = C.MOVIE_STATUS_UPLOADED;
                                    comments = "";
                                }

                                if (mStatus == C.MOVIE_PUBLISHED_RESPONSE) {
                                    localStatus = C.MOVIE_STATUS_PUBLISHED;
                                    comments = "";
                                }

                                if (mStatus == C.MOVIE_UNPUBLISHED_BY_ADMIN_RESPONSE || mStatus == C.MOVIE_UNPUBLISHED_BY_MASTER_RESPONSE) {
                                    localStatus = C.MOVIE_STATUS_UNPUBLISHED;
                                }

                                // update all the remote data of the movie
                                final Movie movie = platfromMovies.get(videoId);
                                movie.setYoutubeLink(youtube_id);
                                movie.setMovieStatus(localStatus);
                                movie.setAdminComments(comments);
                                movie.saveIntoJson();


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                                fillupCompanyArrayFromSharedPrefs();

                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Crashlytics.logException(volleyError.fillInStackTrace());
                        }
                    });

            makeARequest(request);

        }


    }

    private void makeARequest(StringRequest request) {
        if (!requests.contains(request)) {
            requests.add(request);
        }
        request.setRetryPolicy(retryPolicy);
        myApp.addToRequestQueue(request);
        noofPendingRequests++;

    }


    private void fillupCompanyArray(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray  a          = jsonObject.getJSONArray("data");

        myApp.getCompanies().clear();
        for (int i = 0; i < a.length(); i++) {
            final JSONObject jObj = a.getJSONObject(i);
            myApp.getCompanies().add(Company.extractFromJson(jObj));
        }

        ShrePrefs.writeData(ASplash.this, C.sp_company_list_json, jsonString);

    }

    private void fillupCompanyArrayFromSharedPrefs() {

        final String jsonString = ShrePrefs.readData(ASplash.this, C.sp_company_list_json, null);

        if (jsonString != null) {

            try {
                fillupCompanyArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }

    }

    private void fillupStatesArray(String jsonString) throws JSONException {

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray  a          = jsonObject.getJSONArray("data");

        states.clear();
        for (int i = 0; i < a.length(); i++) {

            final JSONObject jObj = a.getJSONObject(i);
            states.add(State.extractFromJson(jObj));

        }

        ShrePrefs.writeData(ASplash.this, C.sp_state_list_json, jsonString);

    }

    private void fillupStatesArrayFromSharedPrefs() {

        final String jsonString = ShrePrefs.readData(ASplash.this, C.sp_state_list_json, null);

        if (jsonString != null) {

            try {
                fillupStatesArray(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }

    }

    @Deprecated
    private void checkPlatformMovieStatus() {

        final File   moviesDir  = new File(C.MOVIES_DIR);
        final File[] movieFiles = moviesDir.listFiles();

        if (movieFiles != null) {

            for (int i = movieFiles.length - 1; i >= 0; i--) {

                final String movieFileName = movieFiles[i].getAbsolutePath();

                final String[] strs      = movieFileName.split("_");
                final String   timeStamp = (strs[strs.length - 2]);

                final ArrayList<String> movieData = ShrePrefs.readArrayData(this, C.sp_movie_prefix + timeStamp);

                if (movieData != null) {

                    final int video_id = Integer.parseInt(movieData.get(2));
                    Log.i("us", video_id + "   name: " + movieData.get(0) + ", status: " + movieData.get(3));

                    if (video_id > -1) {

                        StringRequest movieStatusRequest = new StringRequest(
                                Request.Method.GET,
                                C.API_MOVIE_STATUS_URL + video_id,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        try {

                                            final JSONObject json = new JSONObject(response);

                                            final int mStatus = json.getInt("video_status");

                                            String comments    = json.getString("comments");
                                            int    localStatus = C.MOVIE_STATUS_UPLOADED;

                                            if (mStatus == C.MOVIE_UPLOADED_RESPONSE || mStatus == C.MOVIE_AWAITING_RESPONSE) {
                                                localStatus = C.MOVIE_STATUS_UPLOADED;
                                                comments = "";
                                            }

                                            if (mStatus == C.MOVIE_PUBLISHED_RESPONSE) {
                                                localStatus = C.MOVIE_STATUS_PUBLISHED;
                                                comments = "";
                                            }

                                            if (mStatus == C.MOVIE_UNPUBLISHED_BY_ADMIN_RESPONSE || mStatus == C.MOVIE_UNPUBLISHED_BY_MASTER_RESPONSE) {
                                                localStatus = C.MOVIE_STATUS_UNPUBLISHED;
                                            }

                                            movieData.set(3, String.valueOf(localStatus));
                                            try {
                                                movieData.set(6, comments);
                                            } catch (IndexOutOfBoundsException e) {
                                                movieData.add(comments);
                                                Crashlytics.logException(e);
                                            }


                                            ShrePrefs.writeArrayData(ASplash.this, C.sp_movie_prefix + timeStamp, movieData);


                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Crashlytics.logException(e);
                                        }

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {


                                    }
                                });

                        makeARequest(movieStatusRequest);

                    }


                }
            }

        }

    }


}
