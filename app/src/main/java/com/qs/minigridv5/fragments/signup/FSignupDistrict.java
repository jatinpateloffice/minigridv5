package com.qs.minigridv5.fragments.signup;


import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.qs.minigridv5.R;
import com.qs.minigridv5.activities.AMain;
import com.qs.minigridv5.entities.State;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.ShrePrefs;


public class FSignupDistrict extends MySignupFragment {

    RadioGroup radioGroup;

    public FSignupDistrict() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_signup_district, container, false);
        setupSkip(view);

        radioGroup = view.findViewById(R.id.f_signup_district_radioGroup);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        for (State state : AMain.states) {
            addRadioButton(state);
        }

        final int selected_company = ShrePrefs.readData(getContext(), C.sp_user_district, -3);
        if (selected_company > 0) {
            radioGroup.check(selected_company);
        }

        final Button nextBtn = view.findViewById(R.id.f_signup_district_next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int state_id = radioGroup.getCheckedRadioButtonId();

                if (state_id > 0) {

                    ShrePrefs.writeData(getContext(), C.sp_user_district, state_id);

                    if(parentActivity.skipCompleteScreen){
                        parentActivity.finish();
                    } else {
                        parentActivity.loadFragment(parentActivity.signupComplete, true, false);
                    }

                } else {

                    Toast.makeText(parentActivity, "Select a District to continue", Toast.LENGTH_SHORT).show();

                }

            }
        });


        return view;
    }

    private void addRadioButton(State state) {

        int paddingDp = 24;
        float density = getContext().getResources().getDisplayMetrics().density;
        int paddingPixel = (int) (paddingDp * density);

        final RadioButton rb = new RadioButton(getContext());
        rb.setText(state.name);
        rb.setId(state.ID);
        rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        rb.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        radioGroup.addView(rb);

    }

}
