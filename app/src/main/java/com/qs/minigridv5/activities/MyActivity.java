package com.qs.minigridv5.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Movie;
import com.qs.minigridv5.entities.MovieTemplate;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MyApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public abstract class MyActivity extends AppCompatActivity {


    protected String TAG      = "TAAAAAAAAG";
    private   String LIFE_TAG = C.T + "-life";
    protected int    lang     = C.LANG_ENGLISH;
    protected String langStr  = "en";

    SharedPreferences sp;
    public MyApplication myApp = MyApplication.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LIFE_TAG, getName() + " created");
        checkForSettings();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LIFE_TAG, getName() + " started");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LIFE_TAG, getName() + " resumed");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LIFE_TAG, getName() + " paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LIFE_TAG, getName() + " stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LIFE_TAG, getName() + " re-started");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LIFE_TAG, getName() + " destroyed");
    }

    protected void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        int  uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    void changeStatusBarColor(final int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            final Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, color));
        }
    }

    String getName() {
        return this.getClass().getSimpleName();
    }

    public void checkForSettings() {


        // Language Settings
        lang = ShrePrefs.readData(this, C.sp_lang, C.LANG_ENGLISH);
        switch (lang) {
            case C.LANG_ENGLISH:
                langStr = "en";
                break;
            case C.LANG_HINDI:
                langStr = "en";
                break;
        }

        Locale locale = new Locale(langStr);
        Locale.setDefault(locale);
        Resources      res  = getResources();
        DisplayMetrics dm   = res.getDisplayMetrics();
        Configuration  conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);


    }

    void exitApp() {

        for (Project project : myApp.getProjects()) {
            project.saveProjectJson();
        }
        myApp.getProjects().clear();

        Log.e(C.T, "projects cleared");
        myApp.getMovieTemplates().clear();
        myApp.getCompanies().clear();


        for (Movie movie : myApp.getMovies()) {
            movie.saveIntoJson();
        }
        myApp.getMovies().clear();

        Helpers.clearTempFiles();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finishAffinity();
        }
    }

    void loadMovieDataFromJSON() {

        if (!myApp.getMovieTemplates().isEmpty()) return;

        try {

            String     jsonFile = "MoviesData_" + langStr + ".json";
            JSONObject jsonData = new JSONObject(Helpers.loadJSONFromAsset(this, jsonFile));

            final int noofMovies = jsonData.length();

            myApp.getMovieTemplates().clear();
            for (int i = 0; i < noofMovies; i++) {

                final JSONObject movieObj = jsonData.getJSONObject("M" + i);

                final int       ID          = movieObj.getInt("ID");
                final String    title       = movieObj.getString("title");
                final boolean   forCustomer = movieObj.getInt("for") == 1;
                final JSONArray categories  = movieObj.getJSONArray("scenes");

                final ArrayList<Scene.ScenePackage> scenePackages = new ArrayList<>();

                for (int j = 0; j < categories.length(); j++) {

                    final JSONObject sceneObj       = categories.getJSONObject(j).getJSONObject("C" + j);
                    final String     catTitle       = sceneObj.getString("title");
                    final int        catType        = sceneObj.getInt("type");
                    final int        catVideoLength = sceneObj.getInt("video_length");
                    final String     catVideoText   = sceneObj.getString("video_text");
                    final String     catAudioHeader = sceneObj.getString("audio_header");

                    final JSONArray audios          = sceneObj.getJSONArray("audio");
                    final String[]  catAudioTexts   = new String[audios.length()];
                    final long[]    catAudioLengths = new long[audios.length()];
                    for (int k = 0; k < audios.length(); k++) {

                        final JSONObject audio          = audios.getJSONObject(k).getJSONObject("a" + (k + 1));
                        final String     catAudioText   = audio.getString("audio_text");
                        final int        catAudioLength = audio.getInt("audio_length");

                        catAudioTexts[k] = catAudioText;
                        catAudioLengths[k] = catAudioLength;

                    }

                    final Scene.ScenePackage scenePackage = new Scene.ScenePackage(
                            catTitle,
                            catType,
                            catVideoText,
                            catAudioHeader,
                            catAudioTexts,
                            catVideoLength,
                            catAudioLengths[0]
                    );
                    scenePackages.add(scenePackage);
                }

                final MovieTemplate.MoviePackage moviePackage = new MovieTemplate.MoviePackage(
                        this,
                        ID,
                        title,
                        forCustomer,
                        scenePackages
                );

                myApp.getMovieTemplates().add(new MovieTemplate(moviePackage));

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


    }

    /**
     * Loads projects from the disk if no projects found
     */
    void loadProjects() {

        if (!myApp.getProjects().isEmpty()) return;

        // check for project files in root folder
        final File projectsFolder = new File(C.PROJECTS_DIR);
        if (!projectsFolder.exists()) {
            projectsFolder.mkdirs();
            return;
        }

        final File[] projectFolders = projectsFolder.listFiles();

        for (final File projectFolder : projectFolders) {

            final File   projectJsonFile = new File(projectFolder, "project.json");
            final String jsonString      = Helpers.readFromJSON(projectJsonFile.getAbsolutePath());

            if (jsonString != null) {
                if (jsonString.equals("no file")) {
                    // nuke project folder if project.json is not found
                    Helpers.nukeDirectory(projectsFolder);
                } else {

                    try {
                        final Project project = Project.extractFromJson(jsonString);
                        if (project != null) {// this can be null if jsonString = "NA"
                            myApp.getProjects().add(project);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

        }


    }

    void loadMoviesData() {

        final File moviesDir = new File(C.MOVIES_DIR);

        final File[] files = moviesDir.listFiles();

        for (File file : files) {

            // skip if the file name does not contain .json
            if (!file.getName().contains(".json")) {
                continue;
            }

            try {

                final JSONObject movieJSON = new JSONObject(Helpers.readFromJSON(file.getAbsolutePath()));

                final Movie movie = Movie.extractMovieFromJson(movieJSON);

                // check if movie file exists
                final File movieFile = movie.getMovieFile();
                if (!movieFile.exists()) {// if not, delete the json file
                    file.delete();
                } else {

                    myApp.getMovies().add(movie);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    // NOTE: Parked for now
    public class DataRestorer extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        public DataRestorer() {

            progressDialog = new ProgressDialog(MyActivity.this);
            progressDialog.setMessage(getString(R.string.wait_a_sec));
            progressDialog.setCancelable(false);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            loadMovieDataFromJSON();
            loadProjects();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

}
