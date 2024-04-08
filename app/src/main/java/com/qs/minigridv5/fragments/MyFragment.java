package com.qs.minigridv5.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import com.qs.minigridv5.activities.AMain;
import com.qs.minigridv5.utilities.MyApplication;

public abstract class MyFragment extends Fragment {

    AMain parentActivity;
    MyApplication myApp = MyApplication.getInstance();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity = (AMain) context;
        parentActivity.checkForSettings();
    }



    public abstract String getName();

}
