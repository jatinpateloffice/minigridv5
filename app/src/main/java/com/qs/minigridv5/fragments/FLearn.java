package com.qs.minigridv5.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AVideoPlayer;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;

import java.util.ArrayList;

public class FLearn extends MyFragment {

    private int[]    movieTitles = {
            R.string.overview_title,
            R.string.dikhao_tutorial_title,
            R.string.selfie_tutorial_title,
            R.string.audio_tutorial_title,
            R.string.editing_tutorial_title,
            R.string.publish_tutorial_title
    };
    private Bitmap[] thumbnails;

    public FLearn() {

    }

    @Override
    public void onResume() {
        super.onResume();

        parentActivity.checkForSettings();

        parentActivity.setToolbarText(R.string.title_learn);
        parentActivity.navigationView.getMenu().getItem(C.NAV_LEARN_IDX).setChecked(true);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (thumbnails == null)
            thumbnails = new Bitmap[C.tutorialVideoNames.length];

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_learn, container, false);

        final RecyclerView learnMovieList = view.findViewById(R.id.f_learn_movies_list);
        learnMovieList.setLayoutManager(new LinearLayoutManager(getContext()));
        learnMovieList.setAdapter(new MyAdapter());

        return view;
    }

    @Override
    public String getName() {
        return "FLearn";
    }

    class MyAdapter extends RecyclerView.Adapter<MovieViewHolder> {


        MyAdapter() {
        }


        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.x_learn_movie_item, parent, false);
            return new MovieViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MovieViewHolder vh, final int i) {

            final int position = vh.getAdapterPosition();

//            final String movieFileName = C.TUTORIALS_DIR + "/" + C.tutorialVideoNames[position];
            final String movieFileName = Helpers.getAssetString(getContext(), C.tutorialVideoNames[position]);


            vh.title.setText(movieTitles[position]);

            new MovieDataLoader(movieFileName, vh.thumbnail, position).execute();

            vh.playBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getContext(), AVideoPlayer.class);
                    intent.putExtra(AVideoPlayer.KEY_FILE_NAME, movieFileName);
                    intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
                    intent.putExtra(AVideoPlayer.KEY_CAN_EDIT, false);
                    intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
                    intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
                    startActivity(intent);

                }
            });

            vh.shareBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("video/*");
                    shareIntent.putExtra(Intent.EXTRA_TITLE, movieFileName);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(movieFileName));
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(Intent.createChooser(shareIntent, "Share video"));

                }
            });

        }

        @Override
        public int getItemCount() {
            return C.tutorialVideoNames.length;
        }
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView   thumbnail;
        FrameLayout playBtn;
        ImageView   shareBtn;
        TextView    title;
        TextView    desc;

        MovieViewHolder(View v) {
            super(v);

            thumbnail = v.findViewById(R.id.x_learn_movie_item_thumbnail);
            playBtn = v.findViewById(R.id.x_learn_movie_item_play_btn);
            shareBtn = v.findViewById(R.id.x_learn_movie_item_share_btn);
            title = v.findViewById(R.id.x_learn_movie_item_title);
            desc = v.findViewById(R.id.x_learn_movie_item_desc);
            desc.setVisibility(View.GONE);

        }
    }

    class MovieDataLoader extends AsyncTask<Void, Void, Bitmap> {

        String    videoFile;
        ImageView imageView;
        int       position;


        MovieDataLoader(String videoFile, ImageView imageView, int position) {
            this.videoFile = videoFile;
            this.imageView = imageView;
            this.position = position;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            if (thumbnails[position] == null) {
                final Bitmap bitmap = Helpers.createThumbnailAtTimeFromAsset(getContext(), videoFile, 15);
                thumbnails[position] = bitmap;
            }

            return thumbnails[position];

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }

        }
    }

}
