package com.qs.minigridv5.entities;

import android.graphics.Bitmap;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Movie {

    public static final String  JSON_ID            = "id";
    private static final String JSON_NAME          = "name";
    private static final String JSON_TEMPLATE_NAME = "template_name";
    private static final String JSON_VIDEO_ID      = "video_id";
    private static final String JSON_MOVIE_STATUS  = "movie_status";
    private static final String JSON_YOUTUBE_ID    = "youtube_id";
    private static final String JSON_ADMIN_COMMENTS    = "admin_comments";

    public  long   ID;
    private String name;// name set by user, default => templateName
    private String templateName;
    private int    video_id;
    public int     movieStatus;
    private String youtube_id;
    private String adminComments;
    public Bitmap thumbnail;

    public Movie(Project project){

        this.ID = Helpers.getTimeStamp();
        this.templateName = project.movieTemplate.title;
        this.name = templateName;
        this.movieStatus = C.MOVIE_STATUS_LOCAL;
        this.youtube_id = "NA";
        this.adminComments = "";

    }

    private Movie(long ID, String name, String templateName, int video_id, int movieStatus, String youtube_id, String adminComments) {
        this.ID = ID;
        this.name = name;
        this.templateName = templateName;
        this.video_id = video_id;
        this.movieStatus = movieStatus;
        this.youtube_id = youtube_id;
        this.adminComments = adminComments;

        thumbnail = Helpers.createThumbnailAtTime(getMovieFilePath(), 1);

    }

    public String getMovieFileName(){

        return ID + "." + C.VIDEO_EXTENSION;

    }

    public String getMovieFilePath(){
        return C.MOVIES_DIR + "/" + getMovieFileName();
    }

    public File getMovieJsonFile() {
        return new File(C.MOVIES_DIR + "/" + ID + ".json");
    }

    public File getMovieFile(){
        return new File(getMovieFilePath());
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getTemplateName(){
        return templateName;
    }

    public void setVideoID(int id) {
        this.video_id = id;
    }

    public int getMovieStatus(){
        return movieStatus;
    }

    public void setYoutubeLink(final String youtube_link){
        this.youtube_id = youtube_link;
    }

    public static Movie extractMovieFromJson(final JSONObject jsonObject) throws JSONException {

        final long ID = jsonObject.getLong(JSON_ID);
        final String name = jsonObject.getString(JSON_NAME);
        final String templateName = jsonObject.getString(JSON_TEMPLATE_NAME);
        final int video_id = jsonObject.getInt(JSON_VIDEO_ID);
        int movie_status = 0;
        try {
            movie_status = jsonObject.getInt(JSON_MOVIE_STATUS);
        } catch (Exception e) {

        }

        String youtube_id = "NA";

        try {
            youtube_id = jsonObject.getString(JSON_YOUTUBE_ID);
        } catch (Exception e) {

        }

        String adminComments = "";
        try {
            adminComments = jsonObject.getString(JSON_ADMIN_COMMENTS);
        } catch (Exception e) {

        }

        return new Movie(ID, name, templateName, video_id, movie_status, youtube_id, adminComments);

    }

//    public static Movie tempExtractFromFileName(final String fileName) {
//
//        String name = fileName.split("_")[0];
//        String templateName = fileName.split("_")[1];
//        long ID;
//        try {
//            ID = Long.parseLong(fileName.split("_")[2]);
//        } catch (Exception e) {
//            Log.e(C.T, "yup, outof bounds");
//            ID = Long.parseLong(templateName);
//        }
//
//        return new Movie(ID, name, templateName, -1, 0, "NA");
//
//    }

    public JSONObject getJsonObject() throws JSONException{

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_ID, ID);
        jsonObject.put(JSON_NAME, name);
        jsonObject.put(JSON_TEMPLATE_NAME, templateName);
        jsonObject.put(JSON_VIDEO_ID, video_id);
        jsonObject.put(JSON_MOVIE_STATUS, movieStatus);
        jsonObject.put(JSON_YOUTUBE_ID, youtube_id);

        return jsonObject;
    }

    // creates new json file if not existing or overwrites
    public void saveIntoJson() {

        try {
            Helpers.writeToJSON(getJsonObject(), getMovieJsonFile());
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }


    }

    public int getVideoId() {
        return video_id;
    }

    public void setMovieStatus(int movieStatus) {
        this.movieStatus = movieStatus;
    }

    public void setYoutube_id(String youtube_id) {
        this.youtube_id = youtube_id;
    }

    public String getAdminComments() {
        return adminComments;
    }

    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
    }
}
