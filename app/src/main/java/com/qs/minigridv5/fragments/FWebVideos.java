package com.qs.minigridv5.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AMain;
import com.qs.minigridv5.adapters.YouTubeVideoAdapter;
import com.qs.minigridv5.entities.YouTubeVideo;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Abhis on 26-10-2018.
 */

public class FWebVideos extends MyFragment {


    public static final String KEY_WEB_VIDEOS = "key_web_videos";

    public FWebVideos() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View         view                  = inflater.inflate(R.layout.featured_youtube_videos, container, false);
        final RecyclerView videoListRecyclerView = view.findViewById(R.id.featured_video_recycler);
        videoListRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        videoListRecyclerView.setLayoutManager(layoutManager);

        ArrayList<YouTubeVideo> webVideosToShow = new ArrayList<>();

        final String spWebVideosKey = getArguments().getString(KEY_WEB_VIDEOS);
        final String webVideosJson  = ShrePrefs.readData(getContext(), spWebVideosKey, null);


        try {
            JSONObject responseObject = new JSONObject(webVideosJson);
            JSONArray  dataArray      = responseObject.getJSONArray("data");

            webVideosToShow.clear();
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject jsonObject = dataArray.getJSONObject(i);
                webVideosToShow.add(YouTubeVideo.extractFromJSON(jsonObject));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        final TextView textView = view.findViewById(R.id.no_web_videos_text);

        if(!Helpers.isNetworkAvailable(getContext())){
            textView.setVisibility(View.GONE);
            videoListRecyclerView.setVisibility(View.GONE);
        } else if (webVideosToShow.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            videoListRecyclerView.setVisibility(View.GONE);
        } else {

            YouTubeVideoAdapter youTubeVideoAdapter = new YouTubeVideoAdapter(getActivity(), webVideosToShow);
            videoListRecyclerView.setAdapter(youTubeVideoAdapter);
            videoListRecyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.setToolbarText(R.string.title_community);
    }

    @Override
    public String getName() {
        return "FWebVideos";
    }

}
