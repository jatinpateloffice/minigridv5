package com.qs.minigridv5.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.State;
import com.qs.minigridv5.fragments.*;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MultipartPOSTRequester;
import com.qs.minigridv5.utilities.VideoUploader;
import io.fabric.sdk.android.Fabric;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class AMain extends MyActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {


    public static final int LEARN   = 1;
    public static final int SEE     = 2;
    public static final int MAKE    = 3;
    public static final int LIBRARY = 4;

    // Runtime DB
    public static ArrayList<State>   states;

    private Toolbar toolbar;

    public  FMake         fMake;
    private FLearn        fLearn;
    private FCommunity    fCommunity;
    private FLibrary      fLibrary;
    private FTemplateList fTemplateList;


    private Fragment currentFragment;

    public BottomNavigationView navigationView;

    private TextView intro_overlay_header, intro_overlay_text;
    private FrameLayout intro_overlay;
    private FrameLayout help_overlay;
    private FrameLayout make_new_movie_overlay;
    private FrameLayout make_current_projects_overlay;
    private FrameLayout make_complete_projects_overlay;

    private LinearLayout newMovieBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        navigationView = findViewById(R.id.a_main_bottom_nav_bar);
        navigationView.setOnNavigationItemSelectedListener(this);
        disableShiftMode(navigationView);

        // setup toolbar
        toolbar = findViewById(R.id.a_main_toolbar);
        toolbar.setTitle(R.string.title_make);
        setSupportActionBar(toolbar);

        newMovieBtn = findViewById(R.id.a_make_new_movie_btn);

        // increatement the page counts
        int noof_page_land_counts = ShrePrefs.readData(this, C.sp_amain_page_land_counts, 0);
        ShrePrefs.writeData(this, C.sp_amain_page_land_counts, ++noof_page_land_counts);
        ShrePrefs.writeData(this, C.sp_first_time_open, false);


        intro_overlay = findViewById(R.id.a_main_overlay_intro);
        intro_overlay_header = findViewById(R.id.a_main_intro_overlay_header);
        intro_overlay_text = findViewById(R.id.a_main_intro_overlay_text);
        make_new_movie_overlay = findViewById(R.id.a_main_overlay_new_movie);
        make_current_projects_overlay = findViewById(R.id.a_main_overlay_current_project);
        make_complete_projects_overlay = findViewById(R.id.a_main_overlay_complete_project);
        help_overlay = findViewById(R.id.a_main_overlay_help);

        intro_overlay.setOnClickListener(this);
        make_new_movie_overlay.setOnClickListener(this);
        make_current_projects_overlay.setOnClickListener(this);
        make_complete_projects_overlay.setOnClickListener(this);
        help_overlay.setOnClickListener(this);

        boolean showHelpOverlay = ShrePrefs.readData(this, C.sp_show_help_overlay, true);
        if (showHelpOverlay) {
            help_overlay.setVisibility(View.VISIBLE);
            ShrePrefs.writeData(this, C.sp_show_help_overlay, false);
        }

        fTemplateList = new FTemplateList();

        fMake = new FMake();
        fLearn = new FLearn();
        fCommunity = new FCommunity();
        fLibrary = new FLibrary();

        final int load_space = getIntent().getIntExtra("load_space", -1);

        switch (load_space) {

            case LEARN:
                loadFragment(fLearn, false, true);
                break;
            case SEE:
                loadFragment(fCommunity, false, true);
                break;
            case MAKE:
                loadFragment(fMake, false, true);
                break;
            case LIBRARY:
                loadFragment(fLibrary, false, true);
                break;
            default:
                if (Helpers.isNetworkAvailable(this)) {
                    loadFragment(fCommunity, false, true);
                } else {
                    loadFragment(fCommunity, false, true);
                }
                break;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }

        // check for settings
        checkForSettings();
        // this loads the projects in case no projects found
        loadProjects();
//        for (Project project : myApp.getProjects()) {
//            project.updateMovieScenes();
//        }
    }

    public void startNewMovieProject(View view) {

        loadFragment(fTemplateList, true, false);

    }

    @Override
    String getName() {
        return "AMain";
    }

    @Override
    public void onBackPressed() {


        if (currentFragment == fTemplateList) {
            loadFragment(fMake, false, true);
        } else {

            showExitComfirmDialog();
        }
    }

    private void showExitComfirmDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                exitApp();

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle(R.string.exit_app);
        builder.show();


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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_learn:
                toolbar.setTitle(R.string.title_learn);
                loadFragment(fLearn, false, true);
                return true;
            case R.id.nav_community:
                toolbar.setTitle(R.string.title_community);
                loadFragment(fCommunity, false, true);
                return true;
            case R.id.nav_make:
                toolbar.setTitle(R.string.title_make);
                loadFragment(fMake, false, true);
                return true;
            case R.id.nav_library:
                toolbar.setTitle(R.string.title_library);
                loadFragment(fLibrary, false, true);
                return true;
        }
        return false;
    }

    public void loadFragment(Fragment fragment, boolean putOnStack, boolean emptyStack) {

        if (fragment == fTemplateList) {
            newMovieBtn.setVisibility(View.GONE);
        } else {
            newMovieBtn.setVisibility(View.VISIBLE);
        }

        final FragmentManager fragMan     = getSupportFragmentManager();
        FragmentTransaction   transaction = fragMan.beginTransaction();
        transaction.replace(R.id.a_main_content_frame, fragment);
        if (emptyStack) {
            fragMan.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (putOnStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        currentFragment = fragment;

    }


    @SuppressLint("RestrictedApi")
    private void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            if (menuView.getChildCount() < 6) {
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    //noinspection RestrictedApi
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            }
        } catch (NoSuchFieldException e) {
            Log.e("BNVHelper", "Unable to get shift mode field", e);
            Crashlytics.logException(e);
        } catch (IllegalAccessException e) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
            Crashlytics.logException(e);
        }
    }


    // TODO shift this in MyApplication
    public static State getState(int id) {

        for (State state : states) {
            if (state.ID == id) {
                return state;
            }
        }

        return null;

    }

    public void setToolbarText(int stringID) {
        toolbar.setTitle(stringID);
    }

    public void activateOverlays() {

        intro_overlay.setVisibility(View.VISIBLE);
        intro_overlay_header.setVisibility(View.VISIBLE);

        if (currentFragment == fLearn) {
            intro_overlay_header.setText(R.string.title_learn);
            intro_overlay_text.setText(R.string.learn_intro_overlay_text);
        }

        if (currentFragment == fCommunity) {
            intro_overlay_header.setText(R.string.title_community);
            intro_overlay_text.setText(R.string.community_intro_overlay_text);
        }

        if (currentFragment == fMake) {
            intro_overlay_header.setText(R.string.title_make);
            intro_overlay_text.setText(R.string.make_intro_overlay_text);
        }

        if (currentFragment == fLibrary) {
            intro_overlay_header.setText(R.string.title_library);
            intro_overlay_text.setText(R.string.library_intro_overlay_text);
        }

        if (currentFragment == fTemplateList) {

            intro_overlay_header.setVisibility(View.GONE);
            intro_overlay_text.setText(R.string.theme_select_overlay_text);

        }

    }

    @Override
    public void onClick(View view) {

        view.setVisibility(View.GONE);

        // if you are in Make Space
        if (currentFragment == fMake) {

            if (view == intro_overlay) {
                make_new_movie_overlay.setVisibility(View.VISIBLE);
            }

            // if you tapped on new movieTemplate overlay
            if (view == make_new_movie_overlay) {

                final int currentPosition = fMake.tabLayout.getSelectedTabPosition();

                // if you are on current projects tab
                if (currentPosition == 0) {

                    if (fMake.fCurrentProjects.inCompleteProjects.size() > 0) {
                        make_current_projects_overlay.setVisibility(View.VISIBLE);
                    }
                }

                // if you are on complete projects tab
                if (currentPosition == 1) {

                    if (fMake.fFinishedProjects.completeProjects.size() > 0) {
                        make_complete_projects_overlay.setVisibility(View.VISIBLE);
                    }
                }


            }
        }


    }


}
