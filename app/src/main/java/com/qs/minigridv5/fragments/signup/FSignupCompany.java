package com.qs.minigridv5.fragments.signup;


import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.qs.minigridv5.entities.Company;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MyApplication;

public class FSignupCompany extends MySignupFragment {

    RadioGroup radioGroup;

    public FSignupCompany() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (AMain.companies.isEmpty()) {
//
//            skip();
//
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_signup_company, container, false);
        setupSkip(view);

        radioGroup = view.findViewById(R.id.f_signup_company_radioGroup);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        final boolean noCompanyDataFromBackend = MyApplication.getInstance().getCompanies().isEmpty();

        if(noCompanyDataFromBackend) gotoFragment(parentActivity.signupState);

        for (Company company : MyApplication.getInstance().getCompanies()) {
            addRadioButton(company);
        }

        final int selected_company_id = ShrePrefs.readData(getContext(), C.sp_user_company, -1);
        if (selected_company_id > 0) {
            radioGroup.check(selected_company_id);
        }

        final Button nextBtn = view.findViewById(R.id.f_signup_company_next_btn);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selected_company_id = radioGroup.getCheckedRadioButtonId();

                // proceed if company is selected or no company list is not inflated but dont save if no company data
                if (selected_company_id > 0) {

                    ShrePrefs.writeData(getContext(), C.sp_user_company, selected_company_id);

                    gotoFragment(parentActivity.signupState);

                } else if (noCompanyDataFromBackend) {

                    gotoFragment(parentActivity.signupState);

                } else {

                    Toast.makeText(parentActivity, "Select a company to continue", Toast.LENGTH_SHORT).show();

                }


            }
        });

        return view;
    }

    private void addRadioButton(Company company) {

        int   paddingDp    = 24;
        float density      = getContext().getResources().getDisplayMetrics().density;
        int   paddingPixel = (int) (paddingDp * density);

        final RadioButton rb = new RadioButton(getContext());
        rb.setText(company.name);
        rb.setId(company.ID);
        rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        rb.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
        radioGroup.addView(rb);

    }

}
