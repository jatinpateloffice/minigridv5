package com.qs.minigridv5.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.bumptech.glide.Glide;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.utilities.MyApplication;

import java.util.ArrayList;

public class FCurrentProjects extends MyFragment {

    private LinearLayout noProjectContainer;
    private ListView     projectsListView;

    public ArrayList<Project> inCompleteProjects = new ArrayList<>();

    public FCurrentProjects() {

    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.setToolbarText(R.string.title_make);
        parentActivity.navigationView.getMenu().getItem(C.NAV_MAKE_IDX).setChecked(true);

        updateProjectListView();

        if (inCompleteProjects.size() <= 0) {
            noProjectContainer.setVisibility(View.VISIBLE);
            projectsListView.setVisibility(View.GONE);
        } else {
            noProjectContainer.setVisibility(View.GONE);
            projectsListView.setVisibility(View.VISIBLE);

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_current_projects, container, false);

        Log.d(C.T, getClass().getSimpleName() + " onCreateView");

        noProjectContainer = view.findViewById(R.id.f_no_current_projects_container);
        projectsListView = view.findViewById(R.id.f_current_projects_list);

        return view;
    }

    private void updateProjectListView() {

        inCompleteProjects.clear();

        for (Project project : myApp.getProjects()) {

            project.updateCompleteStatus();
            if (!project.isComplete) {
                inCompleteProjects.add(project);
            }

        }

        final int noofInCompletedProjects = inCompleteProjects.size();


        if (noofInCompletedProjects <= 0) {
            noProjectContainer.setVisibility(View.VISIBLE);
            projectsListView.setVisibility(View.GONE);
        } else {
            noProjectContainer.setVisibility(View.GONE);
            projectsListView.setVisibility(View.VISIBLE);

            final String[] strings = new String[noofInCompletedProjects];

            int i = 0;
            for (Project project : inCompleteProjects) {
                strings[i++] = project.name;
            }
            projectsListView.setAdapter(new MyListAdapter(getContext(), inCompleteProjects));
        }

    }

    @Override
    public String getName() {
        return "FCurrentProjects";
    }

    class MyListAdapter extends ArrayAdapter<Project> {

        public MyListAdapter(@NonNull Context context, ArrayList projects) {
            super(context, -1, projects);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            final View view = parentActivity.getLayoutInflater().inflate(R.layout.x_project_item, null);

            final Project project = getItem(position);

            final TextView title = view.findViewById(R.id.x_project_item_title);
            final TextView subTitle = view.findViewById(R.id.x_project_item_sub_title);
            subTitle.setVisibility(View.GONE);
            final TextView enctext = view.findViewById(R.id.x_project_item_encouragement_text);
            final ImageView deleteBtn = view.findViewById(R.id.x_project_item_delete_btn);
            final ImageView thumbnail = view.findViewById(R.id.x_project_item_thumbnail);
            final ImageView thumbnailMask = view.findViewById(R.id.x_project_item_thumbnail_mask);
            final ImageView completeimg = view.findViewById(R.id.x_project_item_complete_img);
            completeimg.setVisibility(View.GONE);
            final CircularProgressBar progressBar = view.findViewById(R.id.x_project_item_progress_bar);
            final CircularProgressBar emptyProgressBar = view.findViewById(R.id.x_project_item_empty_progress_bar);
            final TextView progressText = view.findViewById(R.id.x_project_item_progress_text);
            final TextView progressSupportText = view.findViewById(R.id.x_project_item_progress_support_text);
            final Button continueBtn = view.findViewById(R.id.x_project_item_continue_btn);

            title.setText(project.movieTemplate.title);

            continueBtn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {

                    MyApplication.getInstance().flowManager.gotoNextActivity(getContext(), project.ID);

                }
            });

            if(project.thumbnail == null){

                thumbnail.setVisibility(View.INVISIBLE);
                thumbnailMask.setVisibility(View.INVISIBLE);
                title.setTextColor(getResources().getColor(R.color.dark_gray));
                progressText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                progressSupportText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                Helpers.changeImageViewColor(getContext(), deleteBtn, R.color.colorPrimaryLight);
                progressBar.setVisibility(View.GONE);
                emptyProgressBar.setVisibility(View.VISIBLE);


            } else {
                thumbnail.setImageBitmap(project.thumbnail);
                progressBar.setVisibility(View.VISIBLE);
                emptyProgressBar.setVisibility(View.GONE);
            }
            progressBar.setIndeterminate(false);


            // calculate project progress
            final int max = project.getMaxProgress() +1 ;
            final int progress = project.calculateProgress();
            progressBar.setMaximum(max);
            progressBar.setProgress(progress);

            final float progressPercentage = ((float)progress / (float)max) * 100;
            progressText.setText((int)progressPercentage + "%");

            final int almostThres = 70;

            if(progressPercentage < 1){
                enctext.setText(R.string.start_this_one);

            } else if(progressPercentage >= 1 && progressPercentage < almostThres){

                enctext.setText(R.string.keep_going);

            } else {

                enctext.setText(R.string.almost_done);

            }


            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (project.delete()) {
                                updateProjectListView();
                            }

                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setTitle(R.string.confirm_delete_project);
                    builder.setMessage(R.string.project_delete_confirm_desc);
                    builder.show();


                }
            });



            return view;
        }

    }

}
