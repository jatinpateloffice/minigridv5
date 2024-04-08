package com.qs.minigridv5.entities;

import android.graphics.Bitmap;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.utilities.MyApplication;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Project {

    private static final String JSON_ID          = "id";
    private static final String JSON_NAME        = "name";
    private static final String JSON_MOVIE_ID    = "movie_id";
    private static final String JSON_COMPLETE    = "complete";
    private static final String JSON_FOLDER_NAME = "project_folder";

    public  long          ID;
    public  String        name;
    private String        projectFolderName;
    public  MovieTemplate movieTemplate;
    public  String        absoluteFolderPath;
    public  boolean       isComplete = false;
    public  Bitmap        thumbnail  = null;

    public int noofShowScenesComplete = 0;
    public int noofTellScenesComplete = 0;

    private Project(final long ID, final String projectFolderName, final String name, final MovieTemplate movieTemplate, final boolean isComplete) {

        this.ID = ID;
        this.name = name;
        this.movieTemplate = movieTemplate;
        this.isComplete = isComplete;

        this.movieTemplate.assignedProject(this);
        this.projectFolderName = projectFolderName;
        this.absoluteFolderPath = C.PROJECTS_DIR + "/" + this.projectFolderName;

        setupMovieScenes();

        // create new folder if not exists for this project
        final File file = new File(absoluteFolderPath);
        if (!file.exists()) {
            if(file.mkdirs()){
                saveProjectJson();
            }
        }

    }


    private String getJsonToSaveString() {

        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_ID, ID);
            jsonObject.put(JSON_NAME, name);
            jsonObject.put(JSON_MOVIE_ID, movieTemplate.ID);
            jsonObject.put(JSON_COMPLETE, isComplete);
            jsonObject.put(JSON_FOLDER_NAME, projectFolderName);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;
        }

    }

    public boolean delete() {

        final File file = new File(absoluteFolderPath);

        if (Helpers.nukeDirectory(file)) {
            MyApplication.getInstance().getProjects().remove(this);
            return true;
        }

        return false;
    }

    public int getMaxProgress(){
        return movieTemplate.totalMediaComponents - 1;
    }

    public int calculateProgress(){

        noofTellScenesComplete = 0;
        noofShowScenesComplete = 0;

        for (Scene scene : movieTemplate.scenes) {

            if (scene.type == Scene.SELFIE) {
                if (scene.allVideoClipsPresent()) {
                    noofTellScenesComplete++;
                }
            } else {
                if (scene.allVideoClipsPresent()) {
                    noofShowScenesComplete++;
                }
                if (scene.audioClipPresent()) {
                    noofTellScenesComplete++;
                }
            }

        }

        return noofShowScenesComplete + noofTellScenesComplete;

    }

    public void saveProjectJson() {

        Helpers.writeToJSON(getJsonToSaveString(), new File(absoluteFolderPath + "/project.json"));

    }

    public static Project extractFromJson(final String jsonString) throws JSONException {

        if(jsonString.equals("NA")) return null;

        final JSONObject    jsonObject    = new JSONObject(jsonString);
        final int           id            = jsonObject.getInt(JSON_ID);
        final String        name          = jsonObject.getString(JSON_NAME);
        final int           movie_id      = jsonObject.getInt(JSON_MOVIE_ID);
        final boolean       isComplete    = jsonObject.getBoolean(JSON_COMPLETE);
        final String        projectFolder = jsonObject.getString(JSON_FOLDER_NAME);
        final MovieTemplate movieTemplate = MyApplication.getInstance().getMovieTemplates().get(movie_id).getCopy();

        return new Project(id, projectFolder, name, movieTemplate, isComplete);

    }

    @Override
    public String toString() {
        String string = "";

        string += "Name: " + name + "\n";
        string += "MovieTemplate: " + movieTemplate.title + "\n";
        for (Scene c : movieTemplate.scenes) {
            string += "\t: " + c.title + "\n";
        }

        return string;
    }

    /**
     * checks for videos files in a scene if they are present or not and update videosArrays of scenes accordingly
     * checks for audio file in a scene and if present add it to the category
     **/
    @Deprecated
    public void updateMovieScenes() {

        final File projectFolder = new File(absoluteFolderPath);
        if (projectFolder.exists()) {

            final String[] projectFiles = projectFolder.list();

            // if no video files found in the project folder, clear videoClips array in Category
            if (projectFiles.length == 0) {
                movieTemplate.clearCategoryVideoFiles();
            } else {

                // if only audio files, still clear videoArrays in Categories in MovieTemplate
                boolean noVideoFound = true;
                for (String file : projectFiles) {

                    if (file.equals("project.json")) {
                        continue;
                    }

                    // file = c3_v1.mp4
                    final String cat   = file.split("_")[0];// c3
                    final String media = file.split("_")[1];// v1.mp4

                    // get the the category id
                    final int   scene_id = Integer.parseInt(cat.substring(1));
                    final Scene scene       = movieTemplate.scenes.get(scene_id);

                    final String media_type = String.valueOf(media.charAt(0));// v1

                    // for video
                    if (media_type.toLowerCase().equals("v")) {
                        if (noVideoFound) {
                            noVideoFound = false;
                        }
                    }

                    // for audio
                    if (media_type.toLowerCase().equals("a")) {
                        scene.setAudio(absoluteFolderPath + "/" + file);
                    }

                }


                if (noVideoFound) {
                    movieTemplate.clearCategoryVideoFiles();
                }

            }

        }

    }

    /**
     * Fills up scene's video array and sets the audio file
     */
    private void setupMovieScenes() {

        final File projectFolder = new File(absoluteFolderPath);

        if (projectFolder.exists()) {


            // fill up video & audio arrays in scenes
            final String[] projectFiles = projectFolder.list();

            for (final String file : projectFiles) {

                if (file.equals("project.json")) {
                    continue;
                }

                // file = c3_v1.mp4
                final String cat   = file.split("_")[0];// c3
                final String media = file.split("_")[1];// v1.mp4

                // get the the category id
                final int   scene_id = Integer.parseInt(cat.substring(1));
                final Scene scene    = movieTemplate.scenes.get(scene_id);

                final String media_type = String.valueOf(media.charAt(0));// v1

                // audio or video?
                if (media_type.toLowerCase().equals("v")) {
                    scene.addVideo(absoluteFolderPath + "/" + file);
                }

                if (media_type.toLowerCase().equals("a")) {
                    scene.setAudio(absoluteFolderPath + "/" + file);
                }

                scene.updateCompletionStatus();

            }

        } else {

            if(projectFolder.mkdirs()) {
                saveProjectJson();
            }

        }

    }

    public void updateCompleteStatus() {

        this.isComplete = true;
        for (Scene scene : movieTemplate.scenes) {

            if (scene.type == Scene.SELFIE) {

                if (!scene.allVideoClipsPresent()) {
                    this.isComplete = false;
                    break;
                }

            } else {

                if (!scene.allVideoClipsPresent()) {
                    this.isComplete = false;
                    break;
                }

                if (!scene.audioClipPresent()) {
                    this.isComplete = false;
                    break;
                }

            }

        }

        saveProjectJson();

    }

    public void updateProjectName(String name) {

        this.name = name;
        saveProjectJson();

    }

    public static Project createNewProject(MovieTemplate selectedMovieTemplate) {

        final MyApplication myApp = MyApplication.getInstance();

        final String project_name = "P" + (myApp.getProjects().size()) + " " + selectedMovieTemplate.title;
        final long    project_id   = Helpers.getTimeStamp();
        final String project_folder       = project_name + "_" + project_id;

        final Project newProject = new Project(project_id, project_folder, project_name, selectedMovieTemplate, false);
        myApp.getProjects().add(newProject);

        newProject.saveProjectJson();

        return newProject;
    }

}