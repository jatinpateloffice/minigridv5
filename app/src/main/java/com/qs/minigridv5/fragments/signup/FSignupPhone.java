package com.qs.minigridv5.fragments.signup;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
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


public class FSignupPhone extends MySignupFragment {

    private ProgressDialog registerProgressDialog;
    private ProgressDialog requestOTPProgressDialog;
    private Button         nextBtn, reqOTPBtn;

    public FSignupPhone() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.f_signup_phone, container, false);
        setupSkip(view);

        final AppCompatEditText phoneNumber  = view.findViewById(R.id.f_signup_phone_edittext);
        final String            phone_number = ShrePrefs.readData(getContext(), C.sp_user_phone, "");
        phoneNumber.setText(phone_number);

        registerProgressDialog = new ProgressDialog(parentActivity);
        registerProgressDialog.setMessage("Registering User");
        registerProgressDialog.setCancelable(false);
        requestOTPProgressDialog = new ProgressDialog(parentActivity);
        requestOTPProgressDialog.setMessage("Requesting OTP");
        requestOTPProgressDialog.setCancelable(false);

        nextBtn = view.findViewById(R.id.f_signup_phone_next_btn);
        reqOTPBtn = view.findViewById(R.id.f_signup_phone_request_otp_btn);

        if (parentActivity.doLogin) {
            nextBtn.setVisibility(View.GONE);
            reqOTPBtn.setVisibility(View.VISIBLE);
            // hide progressbar, progress text and skip
            view.findViewById(R.id.f_signup_phone_progressbar).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.f_signup_skip_btn).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.f_signup_phone_progress_text).setVisibility(View.INVISIBLE);
        }


        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String number = phoneNumber.getText().toString().trim();

                if (number.equals("") || number.isEmpty()) {
                    phoneNumber.setError(getString(R.string.phone_no_cannot_be_empty));
                    phoneNumber.requestFocus();
                } else if (number.contains(String.valueOf(C.SEPARATOR))) {
                    phoneNumber.setError(getString(R.string.invalid_phone_number));
                    phoneNumber.requestFocus();
                }

                if (number.equals("") || number.isEmpty()) {
                    Toast.makeText(getContext(), R.string.phone_no_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else if (number.contains(String.valueOf(C.SEPARATOR))) {
                    Toast.makeText(getContext(), R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
                } else {

                    ShrePrefs.writeData(getContext(), C.sp_user_phone, number);

                    doRegistration();

                }

            }
        });

        reqOTPBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                requestOTP();
            }
        });

        return view;
    }

    private void doRegistration() {

        final String username       = ShrePrefs.readData(parentActivity, C.sp_user_name, null);
        final String phone          = ShrePrefs.readData(parentActivity, C.sp_user_phone, null);
        final int    company_id     = ShrePrefs.readData(getContext(), C.sp_user_company, -1);
        final int    state_id       = ShrePrefs.readData(getContext(), C.sp_user_state, -1);
        final String preferred_lang = "English";

        if (username != null && phone != null && company_id > 0 && state_id > 0) {

            final String url = new StringBuilder().append(C.API_REGISTER_DEMO)
                    .append("user_name=").append(username).append("&")
                    .append("mobile=").append(phone).append("&")
                    .append("company_id=").append(company_id).append("&")
                    .append("state_id=").append(state_id).append("&")
                    .append("country_id=").append(1).append("&")
                    .append("preferred_language=").append(preferred_lang)
                    .toString();

            Log.i(C.T, "register url: " + url);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            registerProgressDialog.hide();

                            try {
                                JSONObject   responseObject = new JSONObject(response);
                                final String responseCode   = responseObject.getString("responses");

                                switch (responseCode) {

                                    case "200":// successfully registered

                                        new AlertDialog.Builder(parentActivity)
                                                .setTitle("Successfully Registered")
                                                .setMessage("Please request for OTP")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        dialogInterface.dismiss();
                                                        requestOTP();

                                                    }
                                                })
                                                .setCancelable(true)
                                                .create()
                                                .show();

                                        // uncomment if auto request for otp
                                        nextBtn.setVisibility(View.GONE);
                                        reqOTPBtn.setVisibility(View.VISIBLE);

                                        break;
                                    case "204":// user already exists
                                        new AlertDialog.Builder(parentActivity)
                                                .setTitle("Already Registered")
                                                .setMessage("Please enter different details to register. Request for OTP to login")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        nextBtn.setVisibility(View.GONE);
                                                        reqOTPBtn.setVisibility(View.VISIBLE);
                                                        dialogInterface.dismiss();

                                                    }
                                                })
                                                .setCancelable(true)
                                                .create()
                                                .show();
                                        break;
                                    default:


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

                            registerProgressDialog.hide();

                        }
                    });

            MyApplication.getInstance().addToRequestQueue(stringRequest);
            registerProgressDialog.show();

        } else {

            new AlertDialog.Builder(parentActivity)
                    .setTitle("Incomplete Details")
                    .setMessage("Please check and enter all the requ")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();

                        }
                    })
                    .setCancelable(true)
                    .create()
                    .show();

        }

    }

    private void requestOTP() {

        final String phoneNo = ShrePrefs.readData(getContext(), C.sp_user_phone, null);

        final String url = C.API_OTP_REQUEST_DEMO + phoneNo;

        Log.i(C.T, "request OTP: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.e(C.T, "response: " + response);

                        requestOTPProgressDialog.hide();

                        try {
                            JSONObject responseObject = new JSONObject(response);
                            String     responseCode   = responseObject.getString("responses");

                            switch (responseCode) {

                                case "200":// otp successfully sent
                                    gotoFragment(parentActivity.signupVerify);
                                    break;

                                case "204":

                                    new AlertDialog.Builder(parentActivity)
                                            .setTitle("Something went wrong :(")
                                            .setMessage("Error sending OTP. Please try again.")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    dialogInterface.dismiss();
                                                    requestOTP();

                                                }
                                            })
                                            .setCancelable(true)
                                            .create()
                                            .show();

                                    break;

                                default:

                            }

                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO log this properly

                        Log.e(C.T, "error: " + error.getLocalizedMessage());

                        requestOTPProgressDialog.hide();
                    }
                });

        // TODO temp change

        gotoFragment(parentActivity.signupVerify);
//        MyApplication.getInstance().addToRequestQueue(stringRequest);
//        requestOTPProgressDialog.show();

    }

}
