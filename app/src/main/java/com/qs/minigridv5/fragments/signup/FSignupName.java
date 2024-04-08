package com.qs.minigridv5.fragments.signup;


import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.ShrePrefs;

public class FSignupName extends MySignupFragment {



    public FSignupName() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_signup_name, container, false);
        setupSkip(view);
        final AppCompatEditText nameEditText = view.findViewById(R.id.f_signup_name_edittext);

        final String existingName = ShrePrefs.readData(getContext(), C.sp_user_name, "");
        nameEditText.setText(existingName);

        final Button nextBtn = view.findViewById(R.id.f_signup_name_next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = nameEditText.getText().toString().trim();
                if (name.equals("") || name.isEmpty()){
                    nameEditText.setError(getString(R.string.name_cannot_be_empty));
                    nameEditText.requestFocus();
                } else if (name.contains(String.valueOf(C.SEPARATOR))){
                    nameEditText.setError(getString(R.string.invalid_name));
                    nameEditText.requestFocus();
                } else if (name.length() > C.MAX_USERNAME_LENGTH){
                    nameEditText.setError(getString(R.string.name_too_long));
                    nameEditText.requestFocus();
                }


                if (name.equals("") || name.isEmpty()) {
                    Toast.makeText(getContext(), R.string.name_cannot_be_empty, Toast.LENGTH_LONG).show();
                } else if (name.contains(String.valueOf(C.SEPARATOR))) {
                    Toast.makeText(getContext(), R.string.invalid_name, Toast.LENGTH_LONG).show();
                } else if(name.length() > C.MAX_USERNAME_LENGTH){
                    Toast.makeText(getContext(), R.string.name_too_long, Toast.LENGTH_LONG).show();
                } else {
                    ShrePrefs.writeData(getContext(), C.sp_user_name, name);
                    gotoFragment(parentActivity.signupCompany);
                }

            }
        });

        return view;
    }

}
