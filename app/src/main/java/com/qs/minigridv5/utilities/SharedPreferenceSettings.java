package com.qs.minigridv5.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Abhis on 15-10-2018.
 */

public class SharedPreferenceSettings {
    Context context;
    SharedPreferences username,mobile,preference_language,country_id,state_id;
    SharedPreferences.Editor usernameEditor, mobileEditor,preference_languageEditor,country_idEditor,
    state_idEditor;

    public SharedPreferenceSettings() {
    }

    public SharedPreferenceSettings(Context context) {
        this.context = context;
        username=context.getSharedPreferences("username",Context.MODE_PRIVATE);
        usernameEditor=username.edit();
        mobile=context.getSharedPreferences("mobileno",Context.MODE_PRIVATE);
        mobileEditor=mobile.edit();
        preference_language=context.getSharedPreferences("language",Context.MODE_PRIVATE);
        preference_languageEditor=preference_language.edit();
        country_id=context.getSharedPreferences("countryid",context.MODE_PRIVATE);
        country_idEditor=country_id.edit();
        state_id=context.getSharedPreferences("state_id",Context.MODE_PRIVATE);
        state_idEditor=state_id.edit();
    }
    public void setUsernamename(String usernamename){
        usernameEditor.putString("username",usernamename).commit();
    }
    public String getUsername(){
        return username.getString("username",null);
    }
    public void setMobile(String mobile){
        mobileEditor.putString("mobileno",mobile).commit();
    }
    public String getMobile(){
        return  mobile.getString("mobileno",null);
    }
    public void setPreference_language(String language){
        preference_languageEditor.putString("language",language).commit();
    }
    public String getPreference_language(){
        return preference_language.getString("language",null);
    }
    public void setCountry_id(String country_id){
        country_idEditor.putString("countryid",country_id).commit();
    }
    public  String getCountry_id(){
        return  country_id.getString("countryid",null);
    }
    public void setState_id(String id){
        state_idEditor.putString("state_id",id).commit();
    }
    public String getState_id(){
        return state_id.getString("state_id",null);
    }
}
