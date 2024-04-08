package com.qs.minigridv5.fragments.signup;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.qs.minigridv5.R;

public class FSignupIntro extends MySignupFragment {


    public FSignupIntro() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_signup_intro, container, false);
        setupSkip(view);

        final Button nextBtn = view.findViewById(R.id.f_signup_intro_next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    gotoFragment(parentActivity.signupName);

            }
        });

        return view;
    }

}
