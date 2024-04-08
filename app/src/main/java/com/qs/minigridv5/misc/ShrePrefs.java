package com.qs.minigridv5.misc;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

import static com.qs.minigridv5.misc.C.PREF_FILE;

public class ShrePrefs {

    public static void writeData(Context c, String key, String data) {

        getSharePrefs(c).edit().putString(key, data).apply();

    }

    public static void writeData(Context c, String key, int data) {
        getSharePrefs(c).edit().putInt(key, data).apply();
    }

    public static void writeArrayData(Context c, String key, ArrayList<String> data) {

        final SharedPreferences.Editor editor = getSharePrefs(c).edit();
        editor.putInt(key + "_size", data.size());
        int i = 0;
        for (String string : data) {
            editor.putString(key + (i++), string);
        }
        editor.commit();

    }

    public static void writeData(Context c, String key, float data) {
        getSharePrefs(c).edit().putFloat(key, data).apply();
    }

    public static void writeData(Context c, String key, boolean data) {
        getSharePrefs(c).edit().putBoolean(key, data).apply();
    }

    public static String readData(Context c, String key, String def) {

        return getSharePrefs(c).getString(key, def);

    }

    public static ArrayList<String> readArrayData(Context c, String key) {

        final ArrayList<String> data = new ArrayList<>();

        final int dataSize = readData(c, key + "_size", 0);
        for (int i = 0; i < dataSize; i++) {

            data.add(readData(c, key + i, null));

        }

        if(dataSize == 0){
            return null;
        } else {
            return data;
        }

    }


    public static int readData(Context c, String key, int def) {
        return getSharePrefs(c).getInt(key, def);
    }

    public static float readData(Context c, String key, float def) {

        return getSharePrefs(c).getFloat(key, def);
    }

    public static boolean readData(Context c, String key, boolean def) {
        return getSharePrefs(c).getBoolean(key, def);
    }

    public static void removeValue(Context c, String key) {
        getSharePrefs(c).edit().remove(key).apply();
    }

    public static void removeArrayValue(Context c, String key) {

        final SharedPreferences.Editor editor = getSharePrefs(c).edit();
        final int dataSize = readData(c, key + "_size", 0);
        removeValue(c, key + "_key");
        for (int i = 0; i < dataSize; i++) {

            removeValue(c, key + i);

        }
    }

    public static SharedPreferences getSharePrefs(Context context) {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

}
