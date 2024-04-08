package com.qs.minigridv5.fragments.signup;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AMain;
import com.qs.minigridv5.activities.ASignUp;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.ShrePrefs;

public class MySignupFragment extends Fragment {

    protected ASignUp parentActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        parentActivity = (ASignUp) context;

    }

    protected void skip(){

        ShrePrefs.writeData(parentActivity, C.sp_load_signup, false);

        startActivity(new Intent(parentActivity, AMain.class));

    }

    protected void setupSkip(View view){

        final LinearLayout skipBtn = view.findViewById(R.id.f_signup_skip_btn);
        if(parentActivity.hideSkip){
            skipBtn.setVisibility(View.GONE);
        } else {
            skipBtn.setVisibility(View.VISIBLE);
            skipBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    skip();
                }
            });
        }

    }

    public String getName(){
        return getClass().getSimpleName();
    }

    protected void gotoFragment(MySignupFragment nextFragment) {
        parentActivity.loadFragment(nextFragment, true, false);
    }

    protected void goToPreviousFragment() {

        parentActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        parentActivity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

    }

}
