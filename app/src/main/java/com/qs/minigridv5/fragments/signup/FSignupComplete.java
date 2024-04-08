package com.qs.minigridv5.fragments.signup;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.qs.minigridv5.R;

public class FSignupComplete extends MySignupFragment {


    public FSignupComplete() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_signup_complete, container, false);

        final Button nextBtn = view.findViewById(R.id.f_signup_complete_next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                skip();

            }
        });

        return view;
    }

}
