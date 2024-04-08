package com.qs.minigridv5.entities;

import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.net.URLDecoder;

import com.crashlytics.android.Crashlytics;
import org.json.JSONException;
import org.json.JSONObject;

public class Company {

    public final int    ID;
    public final        String name;

    private Company(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    @Override
    public String toString() {
        return ID + " -> " + name;
    }

    public static Company extractFromJson(JSONObject jsonObject) {

        try {
            final int id = jsonObject.getInt("compnay_id");
            final String name = new String(jsonObject.getString("compnay_name").getBytes("ISO-8859-1"),"UTF-8");
            final String string=jsonObject.getString("compnay_name");
            final String nametwo= URLDecoder.decode(string, "UTF-8");

            return new Company(id, nametwo);

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;
        }


    }

}
