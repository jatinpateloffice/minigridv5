package com.qs.minigridv5.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;

import java.util.ArrayList;

public class AOverview extends MyActivity {

    long     project_id;
    int     scene_id = -1;
    boolean dikhao;

    Class gotoClass;// ACam or ARecorder

    RecyclerView overviewItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_overview);

        project_id = getIntent().getLongExtra("proj_id", -1);

        // check if all dikhao videos are present
        boolean dikhaoDone = true;
        final Project project = myApp.getProjectForID(project_id);

        for (Scene scene : project.movieTemplate.scenes) {

            if (scene.type != Scene.SELFIE) {

                if (!scene.allVideoClipsPresent()) {
                    scene_id = scene.ID;
                    dikhao = true;
                    dikhaoDone = false;
                    gotoClass = ACam.class;
                    break;
                }

            }
        }

        // if all dekhaos are complete
        if (dikhaoDone) {

            boolean sunaoDone = true;
            dikhao = false;

            // check for sunao clips
            for (Scene scene : project.movieTemplate.scenes) {

                if (scene.type == Scene.SELFIE) {

                    // if selfie scene has no video
                    if (scene.videoClips.size() <= 0) {

                        scene_id = scene.ID;
                        gotoClass = ACam.class;
                        sunaoDone = false;
                        break;

                    }

                } else {

                    if (!scene.audioClipPresent()) {
                        scene_id = scene.ID;
                        gotoClass = AAudioRecorder.class;
                        sunaoDone = false;
                        break;
                    }

                }

            }
            if (sunaoDone) {

                project.updateCompleteStatus();
                Intent intent = new Intent(this, AMuxer.class);
                intent.putExtra("proj_id", project_id);
                startActivity(intent);

            }
        }

        ArrayList<Scene> listScenes = new ArrayList<>();

        String text;
        int noofCompleteCategories = 0;
        int totalCategories;
        if (!dikhao) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            text = getString(R.string.sunao);
            totalCategories = project.movieTemplate.scenes.size();

            int i = 0;
            for (Scene scene : project.movieTemplate.scenes) {

                scene.updateCompletionStatus();
                if (scene.complete) {
                    noofCompleteCategories++;
                }

                listScenes.add(scene);
                if (i < project.movieTemplate.scenes.size() - 1) {
                    listScenes.add(null);
                }
                i++;

            }
        } else {

            text = getString(R.string.dikhao);
            totalCategories = project.movieTemplate.getNonSelfieScenes().size();

            int i = 0;
            for (Scene scene : project.movieTemplate.getNonSelfieScenes()) {

                if (scene.allVideoClipsPresent()) {
                    noofCompleteCategories++;
                }

                listScenes.add(scene);
                if (i < project.movieTemplate.getNonSelfieScenes().size() - 1) {
                    listScenes.add(null);
                }
                i++;

            }

        }

        final TextView headerTextView = findViewById(R.id.a_overview_header);
        headerTextView.setText(text);

        final TextView noofScenesText = findViewById(R.id.a_overview_no_of_scenes);
        noofScenesText.setText(noofCompleteCategories + " / " + totalCategories + " " + getString(R.string.scenes_over));

        overviewItemsList = findViewById(R.id.a_overview_items_list);
        overviewItemsList.setLayoutManager(new LinearLayoutManager(this, dikhao ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false));
        overviewItemsList.setAdapter(new MyAdapter(listScenes));


    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    String getName() {
        return "AOverview";
    }

    public void onContinueClick(View view) {

        if (gotoClass == null) return;

        final Intent intent = new Intent(this, gotoClass);
        intent.putExtra("proj_id", project_id);
        intent.putExtra("cat_id", scene_id);
        startActivity(intent);

    }

    public class MyAdapter extends RecyclerView.Adapter<AOverview.MyAdapter.ViewHolder> {

        ArrayList<Scene> categories;
        int              no = 1;

        public MyAdapter(ArrayList<Scene> categories) {
            this.categories = categories;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView img;
            public ImageView line;
            public TextView  numText;
            public TextView  sceneNameText;

            public ViewHolder(View v) {
                super(v);
                img = v.findViewById(R.id.x_overview_img);
                line = v.findViewById(R.id.x_overview_line);
                numText = v.findViewById(R.id.x_overview_scene_no);
                sceneNameText = v.findViewById(R.id.x_overview_scene_text);
            }
        }

        @Override
        public AOverview.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(dikhao ? R.layout.x_overview_item_verti : R.layout.x_overview_item_hori, parent, false);
            return new AOverview.MyAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final AOverview.MyAdapter.ViewHolder holder, final int position) {

            final Scene scene = categories.get(position);

            if (scene == null) {

                // draw a bar
                holder.line.setVisibility(View.VISIBLE);
                holder.img.setVisibility(View.INVISIBLE);
                holder.numText.setVisibility(View.INVISIBLE);
                holder.sceneNameText.setVisibility(dikhao? View.GONE : View.INVISIBLE);

            } else {

                holder.line.setVisibility(View.GONE);
                holder.img.setVisibility(View.VISIBLE);
                holder.numText.setVisibility(View.VISIBLE);
                holder.numText.setText((no++) + "");
                holder.sceneNameText.setVisibility(View.VISIBLE);
                holder.sceneNameText.setText(scene.title);

                if (scene.ID == scene_id) {

                    holder.img.setImageResource(R.drawable.in_progress_sun);

                } else if (scene.ID < scene_id) {

                    holder.img.setImageResource(R.drawable.complete_sun);


                } else {

                    holder.img.setImageResource(R.drawable.incomplete_sun);

                }

            }

        }

        @Override
        public int getItemCount() {

            return categories.size();
        }


    }

}
