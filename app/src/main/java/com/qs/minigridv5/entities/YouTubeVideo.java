package com.qs.minigridv5.entities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abhis on 26-10-2018.
 */

public class YouTubeVideo {

    public int    id;
    public String video_title;
    public String youtube_id;

    private YouTubeVideo(String video_title, String youtube_id) {
        this.video_title = video_title;
        this.youtube_id = youtube_id;
    }

    public static YouTubeVideo extractFromJSON(JSONObject jsonObject) throws JSONException {

        final String video_title      = jsonObject.getString("video_title");
        final String video_youtube_id = jsonObject.getString("youtube_id");


        return new YouTubeVideo(video_title, video_youtube_id);


    }
}
