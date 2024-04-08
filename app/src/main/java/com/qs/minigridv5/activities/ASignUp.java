package com.qs.minigridv5.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.qs.minigridv5.R;
import com.qs.minigridv5.fragments.signup.*;

public class ASignUp extends MyActivity {

    public static final String KEY_DO_LOGIN = "do_login";
    public static final String KEY_SKIP_INTRO = "skip_intro";

    public FSignupIntro    signupIntro;
    public FSignupName     signupName;
    public FSignupPhone    signupPhone;
    public FSignupCompany  signupCompany;
    public FSignupState    signupState;
    public FSignupDistrict signupDistrict;
    public FSignupComplete signupComplete;
    public FSignupVerify   signupVerify;
    public boolean         skipCompleteScreen = false;
    public boolean         hideSkip           = false;
    public boolean doLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_sign_up);

        signupIntro = new FSignupIntro();
        signupName = new FSignupName();
        signupPhone = new FSignupPhone();
        signupCompany = new FSignupCompany();
        signupState = new FSignupState();
        signupDistrict = new FSignupDistrict();
        signupComplete = new FSignupComplete();
        signupVerify = new FSignupVerify();

        final boolean skipIntroFragment = getIntent().getBooleanExtra(KEY_SKIP_INTRO, true);
        skipCompleteScreen = getIntent().getBooleanExtra("skip_complete", false);
        hideSkip = getIntent().getBooleanExtra("hide_skip", false);
        doLogin = getIntent().getBooleanExtra(KEY_DO_LOGIN, false);

        if (skipIntroFragment) {

            if (doLogin) {
                loadFragment(signupPhone, false, true);
            }

            loadFragment(signupName, false, true);
        } else {
            loadFragment(signupIntro, false, true);
        }

    }

    @Override
    String getName() {
        return "ASignup";
    }

    public void loadFragment(Fragment fragment, boolean putOnStack, boolean emptyStack) {


        final FragmentManager fragMan     = getSupportFragmentManager();
        FragmentTransaction   transaction = fragMan.beginTransaction();
        transaction.replace(R.id.a_signup_pages_container, fragment);
        if (emptyStack) {
            fragMan.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        if (putOnStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

    }

}
