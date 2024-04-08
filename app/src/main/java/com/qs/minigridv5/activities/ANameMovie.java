package com.qs.minigridv5.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Movie;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class ANameMovie extends MyActivity implements View.OnClickListener, TextWatcher {

    public static final String KEY_PROJECT_ID = "proj_id";
    public static final String KEY_MOVIE_ID   = "movie_id";

    long    project_id;
    Project project;

    LinearLayout skipBtn;
    Button       nextBtn;
    EditText     text;


    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_name_movie);

        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        final long movie_id = getIntent().getLongExtra(KEY_MOVIE_ID, -1);

        project = myApp.getProjectForID(project_id);
        movie = myApp.getMovieForID(movie_id);


        text = findViewById(R.id.a_name_movie_edittext);
        text.addTextChangedListener(this);


        final TextView suggestionText = findViewById(R.id.a_name_movie_suggestions_text);
        suggestionText.setText(Helpers.getMovieNameSuggestion(this, project));

        nextBtn = findViewById(R.id.a_name_movie_next_btn);
        nextBtn.setBackground(getResources().getDrawable(R.drawable.round_button_disabled));
        nextBtn.setOnClickListener(this);

        skipBtn = findViewById(R.id.a_name_movie_skip_btn);
        skipBtn.setOnClickListener(this);

    }

    @Override
    String getName() {
        return "ANameMovie";
    }

    @Override
    public void onClick(View view) {

        if (view == skipBtn) {

            gotoMain();

        }

        if (view == nextBtn) {

            final String newMovieName = text.getText().toString();


            if (newMovieName.equals("") || newMovieName.isEmpty()) {
                Toast.makeText(this, R.string.movie_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            } else if (newMovieName.contains(String.valueOf(C.SEPARATOR)) || newMovieName.contains("_")) {
                Toast.makeText(this, R.string.invalid_movie_name, Toast.LENGTH_SHORT).show();
            } else {

                final File oldMovieFile = movie.getMovieFile();
                movie.setName(newMovieName);
                oldMovieFile.renameTo(new File(movie.getMovieFilePath()));
                project.updateProjectName(newMovieName);

                gotoMain();

            }

        }

    }

    private void gotoMain(){

        project.updateCompleteStatus();

        // save new movie data into json
        movie.saveIntoJson();


        final Intent intent = new Intent(this, AMain.class);
        intent.putExtra("load_space", AMain.LIBRARY);
        startActivity(intent);
        finishAffinity();

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence s, int i, int i1, int i2) {

        if (s.length() > 0) {
            nextBtn.setBackground(getResources().getDrawable(R.drawable.round_button));
        } else {
            nextBtn.setBackground(getResources().getDrawable(R.drawable.round_button_disabled));
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

        if (s.length() > 0) {
            nextBtn.setBackground(getResources().getDrawable(R.drawable.round_button));
        } else {
            nextBtn.setBackground(getResources().getDrawable(R.drawable.round_button_disabled));
        }

    }
}
