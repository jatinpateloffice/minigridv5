package com.qs.minigridv5.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.fragments.FProjectTellScenes;
import com.qs.minigridv5.fragments.FProjectShowScenes;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;

import java.util.ArrayList;
import java.util.HashMap;

public class AProjectEdit extends MyActivity implements View.OnClickListener{

    public static final String KEY_PROJECT_ID = "proj_id";
    public static final String KEY_LOAD_PAGE = "load_page";

    public static final int PROJECT_SHOW_PAGE = 0;
    public static final int PROJECT_TELL_PAGE = 1;

    private Project   project;
    private Toolbar   toolbar;
    public TabLayout tabLayout;
    private ViewPager viewPager;

    public FrameLayout intro_overlay;
//    public FrameLayout new_movie_overlay;
    public FrameLayout dikhao_overlay;
    public FrameLayout batao_overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_project_edit);

        final long project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        final int loadPage = getIntent().getIntExtra(KEY_LOAD_PAGE, PROJECT_SHOW_PAGE);

        project = myApp.getProjectForID(project_id);

        project.updateCompleteStatus();

        if (!project.isComplete) {
            finish();
        }

        // setup toolbar
        toolbar = findViewById(R.id.a_project_edit_toolbar);
        toolbar.setTitle(project.movieTemplate.title);
        setSupportActionBar(toolbar);


        tabLayout = findViewById(R.id.a_project_edit_tabs);
        viewPager = findViewById(R.id.a_project_edit_viewpager);

        ViewPagerAdapter vpa = new ViewPagerAdapter(getSupportFragmentManager());


        final FProjectShowScenes fProjectShowScenes = new FProjectShowScenes();
        final FProjectTellScenes fProjectTellScenes = new FProjectTellScenes();


        vpa.addFrag(fProjectShowScenes, R.string.dikhao);
        vpa.addFrag(fProjectTellScenes, R.string.batao);

        viewPager.setAdapter(vpa);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setCurrentItem(loadPage);


        tabLayout.setupWithViewPager(viewPager);

        intro_overlay = findViewById(R.id.a_project_edit_overlay_intro);
//        new_movie_overlay = findViewById(R.id.a_project_edit_overlay_new_movie);
        dikhao_overlay = findViewById(R.id.a_project_edit_overlay_dikhao);
        batao_overlay = findViewById(R.id.a_project_edit_overlay_batao);

        intro_overlay.setOnClickListener(this);
//        new_movie_overlay.setOnClickListener(this);
        dikhao_overlay.setOnClickListener(this);
        batao_overlay.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.toolbar_menu_profile) {

            startActivity(new Intent(this, AProfile.class));

            return true;
        }

        if (id == R.id.toolbar_menu_help) {

            activateOverlays();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    String getName() {
        return "AProjectEdit";
    }

    public Project getProject(){
        return project;
    }

    public void startMuxing(View view) {

        Intent intent = new Intent(this, AMuxer.class);
        intent.putExtra("proj_id", project.ID);
        startActivity(intent);

    }

    private void activateOverlays() {

        intro_overlay.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View view) {

        view.setVisibility(View.GONE);
//        if (view == intro_overlay) {
//            new_movie_overlay.setVisibility(View.VISIBLE);
//        }

        if (view == intro_overlay) {

            final int currentPosition = tabLayout.getSelectedTabPosition();
            if (currentPosition == 0) {

                dikhao_overlay.setVisibility(View.VISIBLE);

            }

            if (currentPosition == 1) {

                batao_overlay.setVisibility(View.VISIBLE);

            }

        }

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments      = new ArrayList<>();
        ArrayList<String>   fragmentTitles = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFrag(Fragment fragment, int title) {
            fragments.add(fragment);
            fragmentTitles.add(getResources().getString(title));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

    }

    public static class ThumbnailLoader extends AsyncTask<Void, Void, Bitmap> {


        private VideoView               videoView;
        private HashMap<String, Bitmap> thumbnails;


        public ThumbnailLoader(VideoView videoView, HashMap<String, Bitmap> thumbnails) {

            this.videoView = videoView;
            this.thumbnails = thumbnails;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            final String videoFile = (String) videoView.getTag();
            if (!thumbnails.containsKey(videoFile)) {
                final Bitmap bitmap = Helpers.createThumbnailAtTime(videoFile, 100);
                thumbnails.put(videoFile, bitmap);
            }


            return thumbnails.get(videoFile);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null) {
                videoView.setBackground(new BitmapDrawable(bitmap));
            }

            videoView = null;

        }
    }

}
