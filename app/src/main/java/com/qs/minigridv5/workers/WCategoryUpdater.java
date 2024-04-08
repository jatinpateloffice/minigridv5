package com.qs.minigridv5.workers;

import android.os.AsyncTask;
import com.qs.minigridv5.entities.MovieTemplate;
import com.qs.minigridv5.entities.Scene;


public class WCategoryUpdater extends AsyncTask<Void, Void, Void> {

    final MovieTemplate movieTemplate;
    UpdateCompleteListener updateCompleteListener;

    public WCategoryUpdater(MovieTemplate movieTemplate) {
        this.movieTemplate = movieTemplate;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        synchronized (movieTemplate) {
            for (Scene scene : movieTemplate.scenes) {
                scene.update();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (updateCompleteListener != null) {
            updateCompleteListener.onCategoryUpdateComplete();
        }

    }

    public interface UpdateCompleteListener {
        void onCategoryUpdateComplete();
    }

}
