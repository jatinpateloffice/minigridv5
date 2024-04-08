package com.qs.minigridv5.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AProjectEdit;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.misc.C;

import java.util.ArrayList;

public class FFinishedProjects extends MyFragment {

    private LinearLayout noProjectContainer;
    private ListView     projectsListView;

    public ArrayList<Project> completeProjects = new ArrayList<>();

    public FFinishedProjects() {

    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.setToolbarText(R.string.title_make);
        parentActivity.navigationView.getMenu().getItem(C.NAV_MAKE_IDX).setChecked(true);

        updateProjectListView();

        if (completeProjects.size() <= 0) {
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

        Log.d(C.T, getClass().getSimpleName() + " onCreateView");

        final View view = inflater.inflate(R.layout.f_finished_projects, container, false);

        noProjectContainer = view.findViewById(R.id.f_no_finished_projects_container);
        projectsListView = view.findViewById(R.id.f_finished_projects_list);

        return view;
    }

    private void updateProjectListView() {

        completeProjects.clear();

        for (Project project : myApp.getProjects()) {

            project.updateCompleteStatus();
            if (project.isComplete) {
                completeProjects.add(project);
            }

        }

        final int noofCompletedProjects = completeProjects.size();


        if (noofCompletedProjects <= 0) {
            noProjectContainer.setVisibility(View.VISIBLE);
            projectsListView.setVisibility(View.GONE);
        } else {
            noProjectContainer.setVisibility(View.GONE);
            projectsListView.setVisibility(View.VISIBLE);

            final String[] strings = new String[noofCompletedProjects];

            int i = 0;
            for (Project project : completeProjects) {
                strings[i++] = project.name;
            }
            projectsListView.setAdapter(new MyListAdapter(getContext(), completeProjects));

        }

    }

    @Override
    public String getName() {
        return "FFinishedProjects";
    }

    class MyListAdapter extends ArrayAdapter<Project> {

        public MyListAdapter(@NonNull Context context, ArrayList projects) {
            super(context, -1, projects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

            final View view = parentActivity.getLayoutInflater().inflate(R.layout.x_project_item, null);

            final Project project = getItem(position);

            final TextView title = view.findViewById(R.id.x_project_item_title);
            final TextView subTitle = view.findViewById(R.id.x_project_item_sub_title);
            final TextView enctext = view.findViewById(R.id.x_project_item_encouragement_text);
            final ImageView deleteBtn = view.findViewById(R.id.x_project_item_delete_btn);
            final ImageView thumbnail = view.findViewById(R.id.x_project_item_thumbnail);
            final CircularProgressBar progressBar = view.findViewById(R.id.x_project_item_progress_bar);
            progressBar.setVisibility(View.GONE);
            final TextView progressText = view.findViewById(R.id.x_project_item_progress_text);
            progressText.setVisibility(View.GONE);
            final TextView supportText = view.findViewById(R.id.x_project_item_progress_support_text);
            supportText.setVisibility(View.GONE);
            final Button continueBtn = view.findViewById(R.id.x_project_item_continue_btn);
            continueBtn.setText(R.string.re_record);
            final FrameLayout item_progress_container = view.findViewById(R.id.x_project_item_progress_container);
            item_progress_container.setVisibility(View.GONE);
            final ImageView editBtn = view.findViewById(R.id.x_project_rename_btn);
            editBtn.setVisibility(View.VISIBLE);


//            title.setText(project.name);
            title.setText(project.name);
            subTitle.setText(project.movieTemplate.title);
            enctext.setVisibility(View.GONE);

            thumbnail.setImageBitmap(project.thumbnail);
            progressBar.setIndeterminate(false);


            continueBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    final Intent intent = new Intent(parentActivity, AProjectEdit.class);
                    intent.putExtra("proj_id", project.ID);
                    startActivity(intent);

                }
            });


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

            editBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    final View dialogView = getLayoutInflater().inflate(R.layout.x_rename_project_dialog, null);
                    final EditText newNameEditText = dialogView.findViewById(R.id.x_rename_project_edittext);

                    final AlertDialog dialog = new AlertDialog
                            .Builder(getContext())
                            .setView(dialogView)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    final String newName = newNameEditText.getEditableText().toString();

                                    if (newName.isEmpty() || newName.contains("_")) {

                                        Toast.makeText(getActivity(), "Invalid Project Name", Toast.LENGTH_SHORT).show();

                                    } else {

                                        project.updateProjectName(newName);
                                        title.setText(newName);
                                        dialogInterface.dismiss();

                                    }

                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    dialogInterface.dismiss();

                                }
                            })
                            .create();
                    dialog.show();


                }
            });


            return view;
        }
    }


}
