package com.qs.minigridv5.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AYoutubePlayer;
import com.qs.minigridv5.entities.YouTubeVideo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Abhis on 10-10-2018.
 */

public class YouTubeVideoAdapter extends RecyclerView.Adapter<YouTubeVideoAdapter.ViewHolder> {

    private ArrayList<YouTubeVideo> youTubeVideos;
    private final Context context;


    public YouTubeVideoAdapter(Context context, ArrayList<YouTubeVideo> youTubeVideos) {
        this.context = context;
        this.youTubeVideos = youTubeVideos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.x_youtube_video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final YouTubeVideo youTubeVideo = youTubeVideos.get(position);
        holder.tv_video_title.setText(youTubeVideo.video_title);
        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AYoutubePlayer.class);
                intent.putExtra("video_key", youTubeVideo.youtube_id);
                context.startActivity(intent);

            }
        });
        Picasso.with(context)
                .load("https://img.youtube.com/vi/" + youTubeVideo.youtube_id + "/0.jpg")
                .into(holder.youTubeImageView);
    }


    @Override
    public int getItemCount() {
        return youTubeVideos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tv_video_title;
        ImageView   youTubeImageView;
        FrameLayout playButton;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_video_title = itemView.findViewById(R.id.tv_video_title);
            youTubeImageView = itemView.findViewById(R.id.youTubeImageView);
            playButton = itemView.findViewById(R.id.playButton);
        }
    }

}
