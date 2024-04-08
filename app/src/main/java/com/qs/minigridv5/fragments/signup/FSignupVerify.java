package com.qs.minigridv5.fragments.signup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MyApplication;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Abhis on 17-10-2018.
 */

public class FSignupVerify extends MySignupFragment {

    private EditText       otpInputText;
    private ProgressDialog verifyingOTPProgressDialog;

    private final int MAX_TIMER_SECONDS_LIMIT = 120;

    public FSignupVerify() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_signup_verify, container, false);
        setupSkip(view);

        verifyingOTPProgressDialog = new ProgressDialog(parentActivity);
        verifyingOTPProgressDialog.setMessage("Verifying OTP. Please wait.");
        verifyingOTPProgressDialog.setCancelable(false);

        otpInputText = view.findViewById(R.id.f_signup_verify_otp_edittext);
        final TextView countDownText = view.findViewById(R.id.f_signup_verify_timer);
        final Button   verifyButton  = view.findViewById(R.id.f_signup_verify_verifyOTP);
        final Button   resendButton  = view.findViewById(R.id.f_signup_verify_resend);

        if(parentActivity.doLogin){

            view.findViewById(R.id.f_signup_verify_progressbar).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.f_signup_skip_btn).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.f_signup_verify_progress_text).setVisibility(View.INVISIBLE);

        }

        new CountDownTimer(MAX_TIMER_SECONDS_LIMIT * 1000, 1000) {

            public void onTick(long millisUntilFinished) {

                final long elapsedSeconds = millisUntilFinished / 1000;

                final int    min    = (int) elapsedSeconds / 60;
                final int    sec    = (int) elapsedSeconds % 60;
                final String minStr = min > 0 ? min + ":" : "";
                final String secStr = (sec > 9 ? sec + "" : "0" + sec) + "s";


                countDownText.setText("time remaining: " + minStr + secStr);
            }

            public void onFinish() {

                countDownText.setText("Time out\nPlease request OTP again");
                resendButton.setVisibility(View.VISIBLE);

            }
        }.start();

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //parentActivity.loadFragment(parentActivity.signupComplete,true,false);
                verifyOTP();
            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goToPreviousFragment();
            }
        });


        return view;
    }

    private void verifyOTP() {


        final String otpValue = otpInputText.getText().toString();
        final String phoneNo  = ShrePrefs.readData(getContext(), C.sp_user_phone, null);

        if (phoneNo == null) {

            new AlertDialog.Builder(parentActivity)
                    .setMessage("Invalid Phone number. Please enter again.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            gotoFragment(parentActivity.signupPhone);
                        }
                    })
                    .setCancelable(true)
                    .create()
                    .show();

        } else if (otpValue.isEmpty() || otpValue.length() == 0) {

            new AlertDialog.Builder(parentActivity)
                    .setMessage("No OTP value entered. Please enter OTP.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .create()
                    .show();

        } else {

            final String url = C.API_OTP_VERIFY_DEMO + "mobile=" + phoneNo + "&otp=" + otpValue;

            Log.i(C.T, "otp verify url: " + url);

            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            verifyingOTPProgressDialog.dismiss();

                            try {
                                JSONObject responseObject = new JSONObject(response);
                                final int  responseCode   = responseObject.getInt("responses");

                                switch (responseCode) {
                                    case 200:

                                        JSONObject jsonObjectdata = responseObject.getJSONObject("data");
                                        String authKey = jsonObjectdata.getString("authorisation_key");
                                        Log.e(C.T, "auth key: " + authKey);
                                        ShrePrefs.writeData(getContext(), C.sp_authorisation_key, authKey);
                                        gotoFragment(parentActivity.signupComplete);
                                        break;

                                    case 204:

                                        new AlertDialog.Builder(parentActivity)
                                                .setTitle("Incorrect OTP")
                                                .setMessage("You have entered incorrect OTP.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        dialogInterface.dismiss();
                                                        otpInputText.getText().clear();
                                                    }
                                                })
                                                .setCancelable(true)
                                                .create()
                                                .show();
                                        break;
                                    default:
                                        new AlertDialog.Builder(parentActivity)
                                                .setMessage("Something went wrong from our side. Please try again.")
                                                .setTitle("Oops..")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        dialogInterface.dismiss();

                                                    }
                                                })
                                                .setCancelable(true)
                                                .create()
                                                .show();


                                        break;

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            verifyingOTPProgressDialog.dismiss();
                            Crashlytics.logException(error.getCause());

                        }
                    });
            MyApplication.getInstance().addToRequestQueue(stringRequest);
            verifyingOTPProgressDialog.show();

        }


    }


    @Override
    public void onResume() {
        super.onResume();


    }


}
