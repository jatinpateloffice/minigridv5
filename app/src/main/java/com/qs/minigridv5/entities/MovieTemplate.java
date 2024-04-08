package com.qs.minigridv5.entities;

import android.content.Context;
import com.qs.minigridv5.misc.C;

import java.util.ArrayList;

public class MovieTemplate {

    public int              ID;
    public String           title;
    public ArrayList<Scene> scenes;
    public int totalMediaComponents = 0;

    public String egVideoFilePath;
    public int    thumbnailImgID;

    public  Project      project;
    private MoviePackage moviePackage;

    public MovieTemplate(MoviePackage mp) {
        this.moviePackage = mp;
        this.ID = mp.ID;
        this.title = mp.name;
        this.scenes = new ArrayList<>();
        this.egVideoFilePath = mp.egVideoFilePath;
        this.thumbnailImgID = mp.thumbnailImgIdentifier;

        final ArrayList<Scene.ScenePackage> scenePackages = moviePackage.scenePackages;

        scenes = new ArrayList<>();

        for (int i = 0; i < scenePackages.size(); i++) {

            final Scene.ScenePackage cp = scenePackages.get(i);

            final Scene scene = new Scene(i, this, cp);
            totalMediaComponents += (scene.type == Scene.SELFIE? 1 : 2);

            scenes.add(scene);

        }


    }

    public void assignedProject(Project project) {

        this.project = project;


    }

    public MovieTemplate getCopy() {

        return new MovieTemplate(this.moviePackage);

    }

    @Deprecated
    public void clearCategoryVideoFiles() {

        for (Scene scene : scenes) {
            scene.videoClips.clear();
        }

    }

    /**
     * returns non selfie scenes
     * @return
     */
    public ArrayList<Scene> getNonSelfieScenes() {
        ArrayList<Scene> scenes = new ArrayList<>();
        for (Scene scene : this.scenes) {
            if (!scene.isSelfieScene()) {
                scenes.add(scene);
            }
        }
        return scenes;
    }

    public Scene getSceneWithID(int ID){
        for (Scene c : scenes) {
            if(c.ID == ID){
                return c;
            }
        }
        return null;
    }

    public static class MoviePackage {

        public int                           ID;
        public String                        name;
        public ArrayList<Scene.ScenePackage> scenePackages;

        public String egVideoFilePath;
        public int    thumbnailImgIdentifier;
        public boolean forCustomer;

        public MoviePackage(Context context, int ID, String name, boolean forCustomer, ArrayList<Scene.ScenePackage> scenePackages) {
            this.ID = ID;
            this.name = name;
            this.scenePackages = scenePackages;
            this.forCustomer = forCustomer;

            this.egVideoFilePath = C.MOVIES_EXAMPLES_DIR + "/" + C.MOVIE_EG_FILE_NAME + this.ID + ".mp4";
            thumbnailImgIdentifier = context.getResources().getIdentifier("m" + this.ID, "drawable", context.getPackageName());

        }
    }

}
