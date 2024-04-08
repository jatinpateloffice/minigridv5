package com.qs.minigridv5.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.*;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AVideoPlayer;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;

import java.io.File;
import java.util.ArrayList;

public class FPublishedMovies extends MyFragment {

    private LinearLayout noMoviesContainer;
    private     RecyclerView moviesList;

    ArrayList<String> publishedMovieNames = new ArrayList<>();

    MyAdapter adapter;

    public FPublishedMovies() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final File moviesDir = new File(C.MOVIES_DIR);
        final String[] movieFiles = moviesDir.list();

        for (int i = movieFiles.length - 1; i >= 0; i--) {

            final String movieName = movieFiles[i];
            final String[] strs = movieName.split("_");
//            final String timeStamp = strs[strs.length - 2];
//
//            final ArrayList<String> movieData = ShrePrefs.readArrayData(getContext(), C.sp_movie_prefix + timeStamp);
//            int uploadStatus = C.MOVIE_STATUS_LOCAL;
//            if(movieData != null) {
//                uploadStatus = Integer.parseInt(movieData.get(3));
//            }
//
//            if(uploadStatus == C.MOVIE_STATUS_PUBLISHED){
//                publishedMovieNames.add(C.MOVIES_DIR + "/" + movieName);
//            }



        }

        adapter = new MyAdapter(publishedMovieNames);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_published_movies, container, false);


        noMoviesContainer = view.findViewById(R.id.f_no_published_movies_container);
        moviesList = view.findViewById(R.id.f_published_movies_list);

        if (publishedMovieNames.size() > 0) {
            noMoviesContainer.setVisibility(View.GONE);
            moviesList.setVisibility(View.VISIBLE);
            moviesList.setLayoutManager(new LinearLayoutManager(getContext()));
            moviesList.setAdapter(adapter);
        } else {
            noMoviesContainer.setVisibility(View.VISIBLE);
            moviesList.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public String getName() {
        return "FRemoteMovies";
    }

    class MyAdapter extends RecyclerView.Adapter<MovieViewHolder> {

        ArrayList<String> movies;

        public MyAdapter(ArrayList<String> movies) {
            this.movies = movies;
        }


        @NonNull
        @Override
        public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.x_movie_item, parent, false);
            return new MovieViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MovieViewHolder vh, final int i) {

            final int position = vh.getAdapterPosition();

            final String movieFileName = movies.get(position);
            final String[] strs = movieFileName.split("_");
            final String timeStamp = (strs[strs.length - 2]);

            final ArrayList<String> movieData = ShrePrefs.readArrayData(getContext(), C.sp_movie_prefix + timeStamp);

            int uploadStatus = C.MOVIE_STATUS_LOCAL;
            String movieName = "";
            String templateTitle = "";
            int video_id = -1;

            if (movieData != null) {

                movieName = movieData.get(0);
                templateTitle = movieData.get(1);
                video_id = Integer.parseInt(movieData.get(2));

                vh.movieName.setText(movieName);
                vh.movieTitle.setText(templateTitle);

            }


            new MovieDataLoader(movieFileName, vh.movieThumbnail).execute();

            vh.playBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getContext(), AVideoPlayer.class);
                    intent.putExtra("file_name", movieFileName);
                    intent.putExtra("show_text", false);
                    intent.putExtra("can_edit", false);
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

            vh.deleteBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            final File file = new File(movieFileName);

                            if (file.delete()) {

                                final String timeStamp = strs[strs.length - 2];
                                final int idx = publishedMovieNames.indexOf(movieFileName);
                                publishedMovieNames.remove(movieFileName);
                                adapter.notifyItemRemoved(idx);
                                ShrePrefs.removeArrayValue(getContext(), timeStamp);

                                if (publishedMovieNames.size() <= 0) {
                                    noMoviesContainer.setVisibility(View.VISIBLE);
                                    moviesList.setVisibility(View.GONE);
                                } else {
                                    noMoviesContainer.setVisibility(View.GONE);
                                    moviesList.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setTitle(R.string.confirm_movie_delete_dialog_title);
                    builder.show();


                }
            });

            final int finalVideo_id = video_id;

            vh.viewOnWebBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String url = C.VIEW_VIDEO_URL + finalVideo_id;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);

                }
            });

        }

        @Override
        public int getItemCount() {
            return movies.size();
        }
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout uploadStatusContainer;
        public TextView     uploadStatusText;
        public ImageView    cancelUploadButton;
        public TextView     uploadProgressPercentage;
        public ProgressBar  uploadProgressBar;
        public LinearLayout progressStuffContainer;
        public Button       seeWhyBtn;
        public ImageView    movieThumbnail;
        public ImageView    shareBtn;
        public ImageView    deleteBtn;
        public FrameLayout  playBtn;
        public TextView     movieName;
        public TextView     movieTitle;
        public Button       publishButton;
        public Button viewOnWebBtn;

        public MovieViewHolder(View v) {
            super(v);

            uploadStatusContainer = v.findViewById(R.id.x_movie_item_upload_status_container);
            uploadStatusContainer.setVisibility(View.GONE);

            progressStuffContainer = v.findViewById(R.id.x_movie_item_progress_stuff_container);
            uploadProgressBar = v.findViewById(R.id.x_movie_item_upload_progressbar);
            uploadStatusText = v.findViewById(R.id.x_movie_item_upload_status_text);
            cancelUploadButton = v.findViewById(R.id.x_movie_item_upload_cancel_btn);
            uploadProgressPercentage = v.findViewById(R.id.x_movie_item_upload_progress_percentage);
            seeWhyBtn = v.findViewById(R.id.x_movie_item_see_why_btn);
            seeWhyBtn.setVisibility(View.GONE);

            playBtn = v.findViewById(R.id.x_movie_item_play_btn);
            shareBtn = v.findViewById(R.id.x_movie_item_share_btn);
            deleteBtn = v.findViewById(R.id.x_movie_item_delete_btn);

            movieName = v.findViewById(R.id.x_movie_item_name);
            movieTitle = v.findViewById(R.id.x_movie_item_title);
            movieThumbnail = v.findViewById(R.id.x_movie_item_thumbnail);

            publishButton = v.findViewById(R.id.x_movie_item_publish_btn);
            publishButton.setVisibility(View.GONE);

            viewOnWebBtn = v.findViewById(R.id.x_movie_item_view_on_website_btn);
            viewOnWebBtn.setVisibility(View.VISIBLE);

        }
    }

    class MovieDataLoader extends AsyncTask<Void, Void, Bitmap> {

        String    videoFile;
        ImageView imageView;


        public MovieDataLoader(String videoFile, ImageView imageView) {
            this.videoFile = videoFile;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Log.e(C.T, "thumbnail for: " + videoFile);
            return Helpers.createThumbnailAtTime(videoFile, 1);
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
