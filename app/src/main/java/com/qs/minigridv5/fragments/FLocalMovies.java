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
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AMain;
import com.qs.minigridv5.activities.ASignUp;
import com.qs.minigridv5.activities.AVideoPlayer;
import com.qs.minigridv5.activities.MyActivity;
import com.qs.minigridv5.entities.Company;
import com.qs.minigridv5.entities.Movie;
import com.qs.minigridv5.entities.State;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MyApplication;
import com.qs.minigridv5.utilities.VideoUploader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FLocalMovies extends MyFragment {

    private LinearLayout noMoviesContainer;
    private RecyclerView moviesList;
    private MyAdapter adapter;


    public FLocalMovies() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(adapter == null) {

            adapter = new MyAdapter();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_local_movies, container, false);

        noMoviesContainer = view.findViewById(R.id.f_no_local_movies_container);
        moviesList = view.findViewById(R.id.f_local_movies_list);

        if (myApp.getMovies().size() > 0) {
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
        return "FLibrary";
    }

    class MyAdapter extends RecyclerView.Adapter<MovieViewHolder> {




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

            final Movie movie = myApp.getMovies().get(position);

            int          uploadStatus  = movie.movieStatus;
            final String movieName     = movie.getName();
            final String templateTitle = movie.getTemplateName();
            final String movieFilePath = movie.getMovieFilePath();


            vh.movieName.setText(movieName);
            vh.movieTitle.setText(templateTitle);


            switch (uploadStatus) {

                case C.MOVIE_STATUS_LOCAL:
                    vh.uploadStatusContainer.setVisibility(View.GONE);
                    vh.publishButton.setVisibility(View.VISIBLE);
                    break;

                case C.MOVIE_STATUS_UPLOADED:
                    vh.progressStuffContainer.setVisibility(View.GONE);
                    vh.cancelUploadButton.setVisibility(View.GONE);
                    vh.uploadStatusText.setVisibility(View.VISIBLE);
                    vh.uploadStatusText.setText(R.string.awating_approval);
                    vh.uploadStatusContainer.setVisibility(View.VISIBLE);
                    vh.uploadStatusContainer.setBackgroundColor(getResources().getColor(R.color.orange));
                    vh.publishButton.setVisibility(View.GONE);
                    break;

                case C.MOVIE_STATUS_UNPUBLISHED:
                    vh.progressStuffContainer.setVisibility(View.GONE);
                    vh.cancelUploadButton.setVisibility(View.GONE);
                    vh.uploadStatusText.setVisibility(View.VISIBLE);
                    vh.uploadStatusText.setText(R.string.film_unpublished);
                    vh.uploadStatusContainer.setVisibility(View.VISIBLE);
                    vh.cancelUploadButton.setVisibility(View.GONE);
                    vh.seeWhyBtn.setVisibility(View.VISIBLE);
                    vh.uploadStatusContainer.setBackgroundColor(getResources().getColor(R.color.red));
                    vh.publishButton.setVisibility(View.VISIBLE);
                    break;
            }

            if(movie.thumbnail != null) {
                vh.movieThumbnail.setImageBitmap(movie.thumbnail);
            }

            vh.playBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getContext(), AVideoPlayer.class);
                    intent.putExtra("file_name", movieFilePath);
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
                    shareIntent.putExtra(Intent.EXTRA_TITLE, movieFilePath);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(movieFilePath));
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

                            final File file = new File(movieFilePath);

                            if (file.delete()) {

                                movie.getMovieJsonFile().delete();
                                myApp.getMovies().remove(movie);
                                adapter.notifyItemRemoved(position);

                                if (myApp.getMovies().size() <= 0) {
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


            // setup video uploading details
            final String auth_key   = ShrePrefs.readData(getContext(), C.sp_authorisation_key, null);
            final String lang       = C.LANG_API;
            final String title      = movieName;
            final String video_desc = "Video Description";
            final String video_lang = "hi";
            final int    company_id = ShrePrefs.readData(getContext(), C.sp_user_company, -1);
            final int    state_id   = ShrePrefs.readData(getContext(), C.sp_user_state, -1);

            final HashMap<String, String> formFields = new HashMap<>();
            formFields.put("hash_key", C.VIDEO_UPLOAD_HASH_KEY);
            formFields.put("language", lang);
            formFields.put("title", title);
            formFields.put("description", video_desc);
            formFields.put("video_language", video_lang);
            formFields.put("company_id", String.valueOf(company_id));
            formFields.put("state_id", String.valueOf(state_id));
            formFields.put("country_id", "1");

            final VideoUploader[] videoUploader = new VideoUploader[1];
            videoUploader[0] = new VideoUploader(
                    (MyActivity) getActivity(),
                    formFields,
                    vh,
                    movie);

            vh.publishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (auth_key == null) {

                        Helpers.showQuickAlert(getContext(), "User not verified. Please verify your phone number.");

                    } else if (company_id < 0 || state_id < 0) {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                final Intent intent = new Intent(getContext(), ASignUp.class);
                                intent.putExtra("skip_complete", true);
                                intent.putExtra("hide_skip", true);
                                intent.putExtra("hide_progress", true);
                                startActivity(intent);

                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setTitle(R.string.incomplete_profile_title);
                        builder.setMessage(R.string.incomplete_profile_dialog_message);
                        builder.show();

                    } else {

                        if (Helpers.isNetworkAvailable(getContext())) {

                            try {
                                videoUploader[0].execute();
                            } catch (IllegalStateException e) {
                                videoUploader[0] = new VideoUploader(
                                        (MyActivity) getActivity(),
                                        formFields,
                                        vh,
                                        movie);
                            }

                        } else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setCancelable(true);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();

                                }
                            });
                            builder.setTitle(R.string.no_internet_connection);
                            builder.show();
                        }
                    }
                }
            });

            vh.seeWhyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();

                        }
                    });
//                    builder.setTitle(movieData != null ? movieData.get(6) : getString(R.string.film_unpublished));
                    builder.show();

                }
            });


            vh.cancelUploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (videoUploader != null) {
                        videoUploader[0].cancel(true);
                    }
                    vh.uploadStatusContainer.setVisibility(View.GONE);

                }
            });

        }

        @Override
        public int getItemCount() {
            return myApp.getMovies().size();
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

        }
    }

}
