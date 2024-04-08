package com.qs.minigridv5.utilities;

import android.content.Context;
import android.content.Intent;
import com.qs.minigridv5.activities.AAudioRecorder;
import com.qs.minigridv5.activities.ACam;
import com.qs.minigridv5.activities.AMuxer;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.entities.Scene;

public class FlowManager {

    private static FlowManager flowManager;

    private FlowManager() {
    }

    public void gotoNextActivity(Context context, final long project_id) {

        final Project project = MyApplication.getInstance().getProjectForID(project_id);

        // first check for all the videos if present
        for (Scene scene : project.movieTemplate.getNonSelfieScenes()) {
            if (!scene.allVideoClipsPresent()) {

                final Intent intent = new Intent(context, ACam.class);
                intent.putExtra(ACam.KEY_PROJECT_ID, project_id);
                intent.putExtra(ACam.KEY_SCENE_ID, scene.ID);
                context.startActivity(intent);
                return;
            }
        }

        for (Scene scene : project.movieTemplate.scenes) {

            // if not a selfie scene, check for audio recording complete or not
            if (scene.type != Scene.SELFIE) {

                if (!scene.audioClipPresent()) {
                    final Intent intent = new Intent(context, AAudioRecorder.class);
                    intent.putExtra(AAudioRecorder.KEY_PROJECT_ID, project_id);
                    intent.putExtra(AAudioRecorder.KEY_SCENE_ID, scene.ID);
                    context.startActivity(intent);
                    return;
                }

            } else {// else check if selfie video is present

                if (!scene.allVideoClipsPresent()) {
                    final Intent intent = new Intent(context, ACam.class);
                    intent.putExtra(ACam.KEY_PROJECT_ID, project_id);
                    intent.putExtra(ACam.KEY_SCENE_ID, scene.ID);
                    context.startActivity(intent);
                    return;
                }

            }

        }

        // goto to Muxer of all clips and recordings are completed
        project.updateCompleteStatus();
        Intent intent = new Intent(context, AMuxer.class);
        intent.putExtra("proj_id", project_id);
        context.startActivity(intent);
        return;


    }

    public static FlowManager getInstance() {

        if (flowManager == null) {
            flowManager = new FlowManager();
        }

        return flowManager;

    }

}
