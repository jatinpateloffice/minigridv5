package com.qs.minigridv5.workers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.C;

public class WFileMover extends AsyncTask<Void, Void, Boolean> {

//    ProgressDialog pd;
    final String inFile, outFile;
    FileMoveListener fileMoveListener;
    boolean move;

    public WFileMover(final String inFile, final String outFile, boolean move, FileMoveListener fml) {
//        pd = new ProgressDialog(context);
//        pd.setTitle("Saving...");
        this.inFile = inFile;
        this.outFile = outFile;
        this.fileMoveListener = fml;
        this.move = move;// if move = true, file is moved else it is copied
    }

    public WFileMover(final String inFile, final String outFile, FileMoveListener fml) {

        this(inFile, outFile, true, fml);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        boolean saveSuccessFull = false;

        final File fin = new File(inFile);
        final File dest = new File(outFile);
        FileInputStream in = null;
        FileOutputStream out = null;


        try {

            in = new FileInputStream(fin);

            out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            Log.e(C.T, "sratratrtaysiy gmving");
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            if(move) {
                if (fin.exists()) {
                    fin.delete();
                }
            }
            saveSuccessFull = true;

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {


            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }

        }

        return saveSuccessFull;
    }

    @Override
    protected void onPostExecute(Boolean saveSuccessfull) {

//        pd.dismiss();

        if (fileMoveListener != null)
            fileMoveListener.onMovingComplete(saveSuccessfull);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (fileMoveListener != null)
            fileMoveListener.onMovingComplete(false);

    }

    public interface FileMoveListener {
        void onMovingComplete(boolean success);
    }

}
