package com.qs.minigridv5.utilities;

/**
 * Created by Abhis on 10-10-2018.
 */

import android.app.Application;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.qs.minigridv5.entities.*;

import java.util.ArrayList;

public class MyApplication extends Application {

    private static MyApplication mInstance;
    private        RequestQueue  volleyRequestQueue;
    public         FlowManager   flowManager;

    private ArrayList<MovieTemplate> movieTemplates;
    private ArrayList<Project>       projects;
    private ArrayList<Company>       companies;
    private ArrayList<State>         states;
    private ArrayList<Movie> movies;

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        flowManager = FlowManager.getInstance();

        movieTemplates = new ArrayList<>();
        projects = new ArrayList<>();
        companies = new ArrayList<>();
        states = new ArrayList<>();
        movies = new ArrayList<>();

    }

    public ArrayList<MovieTemplate> getMovieTemplates() {

        return movieTemplates;
    }

    public ArrayList<Project> getProjects() {

        return projects;
    }

    public Project getProjectForID(long project_id) {
        for (Project project : getProjects()) {
            if (project.ID == project_id) {
                return project;
            }
        }

        return null;
    }

    public ArrayList<Company> getCompanies() {
        return companies;
    }

    public Company getCompany(int id) {

        for (Company company : companies) {
            if (company.ID == id) {
                return company;
            }
        }

        return null;

    }

    public ArrayList<State> getStates() {
        return states;
    }

    public ArrayList<Movie> getMovies(){
        return movies;
    }

    public Movie getMovieForID(long id) {
        for (Movie movie : getMovies()) {
            if (movie.ID == id) {
                return movie;
            }
        }
        return null;
    }

    // Volley Stuff
    public RequestQueue getRequestQueue() {
        if (volleyRequestQueue == null) {
            volleyRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return volleyRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}
