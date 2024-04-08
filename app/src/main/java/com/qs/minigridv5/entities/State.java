package com.qs.minigridv5.entities;

import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.net.URLDecoder;


public class State {

    public final int    ID;
    public final String name;

    public State(int ID, String name) {
        this.ID = ID;
        this.name = name;
    }

    @Override
    public String toString() {
        return ID + " -> " + name;
    }

    public static State extractFromJson(JSONObject jsonObject) {

        try {
            final int id = jsonObject.getInt("state_id");
            final String name = URLDecoder.decode(jsonObject.getString("state_name"), "UTF-8");
            return new State(id, name);

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return null;
        }


    }

}
