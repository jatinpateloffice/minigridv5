package com.qs.minigridv5.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;

import java.util.ArrayList;

public class AOverallOverview extends MyActivity {

    public static final String KEY_PROJECT_ID = "proj_id";
    public static final String KEY_SELECTED_IDX = "idx";

    public static final int NONE   = 0;
    public static final int DIKHAO = 1;
    public static final int BATAO  = 2;

    Project project;
    long     project_id;
    int     next_scene_id = -1;

    private Class gotoClass;

    int selectedIdx = 0;

    RelativeLayout dikhaoCompleteCongo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_overall_overview);

        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);
        selectedIdx = getIntent().getIntExtra(KEY_SELECTED_IDX, NONE);

        project = myApp.getProjectForID(project_id);

        switch (selectedIdx) {

            case DIKHAO:
                next_scene_id = project.movieTemplate.getNonSelfieScenes().get(0).ID;
                gotoClass = ACam.class;
                break;

            case BATAO:
                boolean gotoMux = true;
                for (int i = 0; i < project.movieTemplate.scenes.size(); i++) {

                    final Scene nextScene = project.movieTemplate.scenes.get(i);

                    if (nextScene.type == Scene.SELFIE) {

                        if (!nextScene.allVideoClipsPresent()) {
                            next_scene_id = nextScene.ID;
                            gotoClass = ACam.class;
                            gotoMux = false;
                            break;
                        }

                    } else {

                        if (!nextScene.audioClipPresent()) {
                            gotoClass = AAudioRecorder.class;
                            next_scene_id = nextScene.ID;
                            gotoMux = false;
                            break;
                        }

                    }
                }

                if (gotoMux) {
                    project.updateCompleteStatus();
                    Intent intent = new Intent(this, AMuxer.class);
                    intent.putExtra("proj_id", project_id);
                    startActivity(intent);
                }
                break;

            default:
                next_scene_id = project.movieTemplate.getNonSelfieScenes().get(0).ID;
                gotoClass = AOverallOverview.class;
        }

        final TextView titleText = findViewById(R.id.a_overall_overview_header);
        titleText.setText(project.movieTemplate.title);

        final int noofDikhaoScenes = project.movieTemplate.getNonSelfieScenes().size();
        final int noofSunaoScenes = project.movieTemplate.scenes.size();

        final String noofDikhaoString = noofDikhaoScenes + " " + getString(R.string.scenes);
        final String noofSunaoString = noofSunaoScenes + " " + getString(R.string.scenes);
        final String watchFilms = getString(R.string.watch_your_film);

        final ArrayList<OverviewItem> overviewItems = new ArrayList<>();
        overviewItems.add(new OverviewItem(1, R.string.dikhao, noofDikhaoString));
        overviewItems.add(null);
        overviewItems.add(new OverviewItem(2, R.string.sunao, noofSunaoString));
        overviewItems.add(null);
        overviewItems.add(new OverviewItem(3, R.string.all_done, watchFilms));

        RecyclerView overviewItemsList = findViewById(R.id.a_overall_overview_items_list);
        overviewItemsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        overviewItemsList.setAdapter(new AOverallOverview.MyAdapter(overviewItems));

        dikhaoCompleteCongo = findViewById(R.id.a_dikhao_complete_congo);
        boolean showDikhaoCongo = getIntent().getBooleanExtra("show_dikhao_congo", false);
        if (showDikhaoCongo) {
            dikhaoCompleteCongo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dikhaoCompleteCongo.setVisibility(View.GONE);
                    ShrePrefs.writeData(AOverallOverview.this, C.sp_show_dikhao_complete_congo, false);
                }
            });
            dikhaoCompleteCongo.setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    @Override
    String getName() {
        return "AOverallOverview";
    }

    public void onContinueClick(View view) {

        if (selectedIdx == NONE) {

            final Intent intent = new Intent(this, gotoClass);
            intent.putExtra("proj_id", project_id);
            intent.putExtra("idx", DIKHAO);
            intent.putExtra("cat_id", next_scene_id);
            startActivity(intent);

        } else {

            final boolean show_dekhao_tuto = ShrePrefs.readData(this, C.sp_show_dekho_tuto, true);
            final boolean show_sunao_tuto = ShrePrefs.readData(this, C.sp_show_sunao_tuto, true);

            if (show_dekhao_tuto) {

                final String dikhoTutorialVideo = Helpers.getAssetString(this, C.DIKHAO_TUTORIAL_VIDEO_NAME);

                final Intent intent = new Intent(this, AVideoPlayer.class);
                intent.putExtra(AVideoPlayer.KEY_FILE_NAME, dikhoTutorialVideo);
                intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
                intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
                intent.putExtra(AVideoPlayer.KEY_KEEP_ASPECT_RATIO, false);
                intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
                startActivity(intent);
                ShrePrefs.writeData(this, C.sp_show_dekho_tuto, false);

                AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                    @Override
                    public void onContinueClicked(Activity activity) {

                        final Intent intent = new Intent(activity, gotoClass);
                        intent.putExtra("proj_id", project_id);
                        intent.putExtra("cat_id", next_scene_id);
                        activity.startActivity(intent);

                    }
                });

            } else if (show_sunao_tuto) {

                final Scene nextScene = project.movieTemplate.getSceneWithID(next_scene_id);

                final String tutorialVideo;

                if (nextScene.type == Scene.SELFIE) {

                    // load selfie tutorial video
                    tutorialVideo = Helpers.getAssetString(this, C.SELFIE_TUTORIAL_VIDEO_NAME);

                } else {

                    // load audio recording tutorial video
                    tutorialVideo = Helpers.getAssetString(this, C.DIKHAO_TUTORIAL_VIDEO_NAME);

                }

                final Intent intent = new Intent(this, AVideoPlayer.class);
                intent.putExtra(AVideoPlayer.KEY_FILE_NAME, tutorialVideo);
                intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
                intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
                intent.putExtra(AVideoPlayer.KEY_KEEP_ASPECT_RATIO, false);
                intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
                startActivity(intent);

                ShrePrefs.writeData(this, C.sp_show_sunao_tuto, false);

                AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
                    @Override
                    public void onContinueClicked(Activity activity) {

                        final Intent intent = new Intent(activity, gotoClass);
                        intent.putExtra("proj_id", project_id);
                        intent.putExtra("cat_id", next_scene_id);
                        activity.startActivity(intent);

                    }
                });

            } else {

                final Intent intent = new Intent(this, gotoClass);
                intent.putExtra("proj_id", project_id);
                if (selectedIdx == NONE) {
                    intent.putExtra("idx", DIKHAO);
                }
                intent.putExtra("cat_id", next_scene_id);
                startActivity(intent);

            }

        }

    }

    public class MyAdapter extends RecyclerView.Adapter<AOverallOverview.MyAdapter.ViewHolder> {

        ArrayList<OverviewItem> overviewItems;

        public MyAdapter(ArrayList<OverviewItem> overviewItems) {
            this.overviewItems = overviewItems;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView img;
            public ImageView line;
            public TextView  numText;
            public TextView  titleText;
            public TextView  descText;

            public ViewHolder(View v) {
                super(v);

                img = v.findViewById(R.id.x_overall_overview_img);
                line = v.findViewById(R.id.x_overall_overview_line);
                numText = v.findViewById(R.id.x_overall_overview_no);
                titleText = v.findViewById(R.id.x_overall_overview_title_text);
                descText = v.findViewById(R.id.x_overall_overview_desc_text);

            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.x_overall_overview_item, parent, false);
            return new AOverallOverview.MyAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            final OverviewItem oi = overviewItems.get(position);

            if (oi == null) {

                // draw a bar
                holder.line.setVisibility(View.VISIBLE);
                holder.img.setVisibility(View.INVISIBLE);
                holder.numText.setVisibility(View.INVISIBLE);
                holder.titleText.setVisibility(View.GONE);
                holder.descText.setVisibility(View.GONE);

            } else {

                holder.line.setVisibility(View.GONE);
                holder.img.setVisibility(View.VISIBLE);
                holder.numText.setVisibility(View.VISIBLE);
                holder.numText.setText(oi.idx + "");
                holder.titleText.setVisibility(View.VISIBLE);
                holder.titleText.setText(oi.title);
                holder.descText.setVisibility(View.VISIBLE);
                holder.descText.setText(oi.desc);


                if (selectedIdx == oi.idx) {

                    holder.img.setImageResource(R.drawable.in_progress_sun);

                } else if (selectedIdx > oi.idx) {

                    holder.img.setImageResource(R.drawable.complete_sun);

                } else {

                    holder.img.setImageResource(R.drawable.incomplete_sun);

                }

            }


        }

        @Override
        public int getItemCount() {
            return overviewItems.size();
        }


    }

    class OverviewItem {

        int idx;
        public String title;
        public String desc;

        public OverviewItem(int idx, int title, String desc) {
            this.idx = idx;
            this.title = getString(title);
            this.desc = desc;
        }
    }

}
