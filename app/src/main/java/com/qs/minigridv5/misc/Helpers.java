package com.qs.minigridv5.misc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.entities.Project;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

public class Helpers {

    public static void setupToolbar(Context context) {

    }

    public static boolean nukeDirectory(File directory) {

        boolean nukeSuccess = true;

        if (directory.exists()) {

            // try deleting the directory, if is not empty it won't delete
            if (!directory.delete()) {

                final File[] listFiles = directory.listFiles();

                if (listFiles != null) {
                    for (File childFile : directory.listFiles()) {

                        if (childFile.isDirectory()) {
                            boolean childNukeSuccess = nukeDirectory(childFile);
                            if (nukeSuccess) {// only update nuke success value if nuke success is true, hence if nuke success if false it will remain false
                                nukeSuccess = childNukeSuccess;
                            }
                        } else {
                            boolean deleteSuccess = childFile.delete();
                            if (nukeSuccess) {
                                nukeSuccess = deleteSuccess;
                            }
                        }

                    }
                }
            }

        }

        if (directory.exists()) {
            directory.delete();
        }

        return nukeSuccess;

    }

    public static String loadJSONFromAsset(Context context, String jsonFileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(jsonFileName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, StandardCharsets.UTF_8);


        } catch (IOException ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
            return null;
        }
        return json;

    }

    public static long calculateClipLength(final String absFileName) {

        final File file = new File(absFileName);
        if (!file.exists()) return 0;

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(absFileName);
            String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);// in milliseconds
            return Long.parseLong(time);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(C.T + "->", file.length() + " <- " + absFileName);
            Crashlytics.logException(e);
            // delete the corrupt file
            file.delete();
            return 0;
        }

    }

    public static void copyFile(Context context, String filename, String dstFilePath) throws Exception {
        AssetManager assetManager = context.getAssets();

        InputStream  in;
        OutputStream out;

        in = assetManager.open(filename);
        out = new FileOutputStream(dstFilePath);

        byte[] buffer = new byte[1024];
        int    read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();


    }

    public static Bitmap createThumbnailAtTime(String filePath, int timeInSeconds) {

        final File file = new File(filePath);
        if (!file.exists()) return null;

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        try {
            mMMR.setDataSource(filePath);
            //api time unit is microseconds
            final Bitmap bitmap = mMMR.getFrameAtTime(timeInSeconds * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            final int bWidth = bitmap.getWidth();
            final int bHeight = bitmap.getHeight();
            final int scaleFactor = 2;
            return Bitmap.createScaledBitmap(bitmap, bWidth/scaleFactor, bHeight/scaleFactor, false);
//            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(C.T + "->", file.length() + " <- " + filePath);
            Crashlytics.logException(e);

            return null;
        }
    }

    public static Bitmap createThumbnailAtTimeFromAsset(final Context context, String filePath, int timeInSeconds, final int scaleFactor) {

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        try {
            mMMR.setDataSource(context, Uri.parse(filePath));
            //api time unit is microseconds
            final Bitmap bitmap = mMMR.getFrameAtTime(timeInSeconds * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            final int bWidth = bitmap.getWidth();
            final int bHeight = bitmap.getHeight();
            return Bitmap.createScaledBitmap(bitmap, bWidth/scaleFactor, bHeight/scaleFactor, false);
//            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(C.T + "->", " <- " + filePath);
            Crashlytics.logException(e);

            return null;
        }
    }

    public static Bitmap createThumbnailAtTimeFromAsset(final Context context, String filePath, int timeInSeconds) {

        MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
        try {
            mMMR.setDataSource(context, Uri.parse(filePath));
            //api time unit is microseconds
            return mMMR.getFrameAtTime(timeInSeconds * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(C.T + "->", " <- " + filePath);
            Crashlytics.logException(e);

            return null;
        }
    }

    public static long getTimeStamp() {
        return new Timestamp(System.currentTimeMillis()).getTime();
    }

    public static void clearTempFiles() {

        nukeDirectory(new File(C.TEMP_DIR));

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getMovieNameSuggestion(Context context, Project project) {
        final String suggestion;
        final String username = ShrePrefs.readData(context, C.sp_user_name, null);
        if (username == null) {
            suggestion = project.movieTemplate.title;
        } else {
            suggestion = username + " - " + project.movieTemplate.title;
        }
        return suggestion;
    }

    public static void showQuickAlert(Context context, final String message) {

        new android.support.v7.app.AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setCancelable(true)
                .create()
                .show();

    }

    public static void showAlert(Context context, final boolean cancelable, final String message, final DialogInterface.OnClickListener clickListener) {

        new android.support.v7.app.AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", clickListener)
                .setCancelable(cancelable)
                .create()
                .show();

    }

    public static void changeImageViewColor(Context context, ImageView imgview, int color) {
        imgview.setColorFilter(context.getResources().getColor(color));
    }

    public static String getTimeInSecs(final long timeInMicroSecs) {

        return String.format("%.3fs", (timeInMicroSecs * 0.000001));

    }

    public static String viewFileStructure(final String dir) {

        final StringBuilder builder = new StringBuilder();
        builder.append(" ");
        browseDirectory(0, builder, dir);
        return builder.toString();
    }

    private static void browseDirectory(final int depth, final StringBuilder builder, final String dir) {

        final File d = new File(dir);
        builder.append("\n");
        for (int i = 0; i < depth; i++) {
            builder.append("|");
            builder.append("\t");
        }
        if (d.isDirectory()) {

            builder.append(" /");
            builder.append(d.getName());
            for (String f : d.list()) {
                browseDirectory(depth + 1, builder, d + "/" + f);
            }

        } else {
            builder.append("|_")
                    .append(d.getName())
                    .append(" ")
                    .append(String.format("%.3f mb", (((float) d.length() / (1024 * 1024)))));
        }

    }

    public static final String getAssetString(final Context context, final String assetName) {
        return "android.resource://" + context.getPackageName() + "/raw/" + assetName;
    }

    public static void writeToJSON(JSONObject jsonObject, File jsonFile) {

        writeToJSON(jsonObject.toString(), jsonFile);

    }

    public static void writeToJSON(String jsonString, File jsonFile) {

        FileWriter writer = null;
        try {
            writer = new FileWriter(jsonFile);
            if (jsonString != null) {
                writer.write(jsonString);
            } else {
                writer.write("NA");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {

            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }

    }

    public static String readFromJSON(final String jsonFilePath) {

        final File jsonFile = new File(jsonFilePath);

        if (!jsonFile.exists()) {
            return "no file";
        }

        try {

            try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
                StringBuilder sb   = new StringBuilder();
                String        line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    line = br.readLine();
                }
                return sb.toString();

            }

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;

        }


    }

}
