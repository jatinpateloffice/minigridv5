package com.qs.minigridv5.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Company;
import com.qs.minigridv5.entities.State;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;
import com.qs.minigridv5.misc.ShrePrefs;
import com.qs.minigridv5.utilities.MyApplication;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class AProfile extends MyActivity implements View.OnClickListener {

    ImageView closeBtn;
    Button    editBtn;
    TextView  name_txt, state_txt, company_txt;
    CircleImageView profilePic;
    Uri             imageUri;
    final int    TAKE_PICTURE       = 115;
    final String profilePicFilePath = C.COMMON_DIR + "/profile_pic.png";

    int noofViewa, noofClips, noofVideos;
    TextView viewsText, clapsText, videosText;

    TextView appLangBtn;
    int      lang;
    int      prevLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_profile);

        lang = ShrePrefs.readData(this, C.sp_lang, C.LANG_ENGLISH);

        name_txt = findViewById(R.id.a_profile_name);
        state_txt = findViewById(R.id.a_profile_state);
        company_txt = findViewById(R.id.a_profile_company);

        editBtn = findViewById(R.id.a_profile_edit_btn);
        closeBtn = findViewById(R.id.a_profile_close_btn);

        editBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);

        profilePic = findViewById(R.id.a_profile_pic);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(AProfile.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    imageUri = FileProvider.getUriForFile(AProfile.this, getPackageName() + ".provider", new File(profilePicFilePath));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                    startActivityForResult(intent, TAKE_PICTURE);
                } else {

                    ActivityCompat.requestPermissions(AProfile.this, new String[]{android.Manifest.permission.CAMERA}, 1);

                }

            }
        });

        viewsText = findViewById(R.id.a_profile_views_count);
        clapsText = findViewById(R.id.a_profile_clap_count);
        videosText = findViewById(R.id.a_profile_published_films_count);

        final File profilePicFile = new File(profilePicFilePath);
        if (profilePicFile.exists()) {
            profilePic.setImageURI(Uri.parse(profilePicFilePath));
        }

        final String user_phone = ShrePrefs.readData(this, C.sp_user_phone, null);

        if (user_phone != null) {

            fillupUserAnalyticsFromSharedPrefs();
            if (Helpers.isNetworkAvailable(this)) {

                Log.e(C.T, "api: " + C.API_USER_ANALYTICS_URL + user_phone);
                RequestQueue queue = Volley.newRequestQueue(this);
                StringRequest userAnalyticsRequest = new StringRequest(
                        Request.Method.GET,
                        C.API_USER_ANALYTICS_URL + user_phone,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {

                                    fillupUserAnalytics(response);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Crashlytics.logException(e);
                                    fillupUserAnalyticsFromSharedPrefs();

                                }


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                fillupUserAnalyticsFromSharedPrefs();

                            }
                        });

                queue.add(userAnalyticsRequest);

            }

        }

        appLangBtn = findViewById(R.id.a_profile_lang_btn);
        appLangBtn.setOnClickListener(this);

    }

    @Override
    String getName() {
        return "AProfile";
    }

    private void fillupUserAnalyticsFromSharedPrefs() {

        final String jsonString = ShrePrefs.readData(this, C.sp_user_analytics_json, null);

        if (jsonString != null) {

            try {
                fillupUserAnalytics(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }

        }

    }

    private void fillupUserAnalytics(String jsonString) throws JSONException {

        JSONObject a = new JSONObject(jsonString);

        final String totalViews = a.getString("total_views");
        final String totalClaps = a.getString("total_claps");
        final String totalVideos = a.getString("total_published_videos");

        clapsText.setText(totalClaps);
        viewsText.setText(totalViews);
        videosText.setText(totalVideos);

        ShrePrefs.writeData(this, C.sp_user_analytics_json, jsonString);


    }


    @Override
    protected void onResume() {
        super.onResume();

        final Company company = MyApplication.getInstance().getCompany(ShrePrefs.readData(this, C.sp_user_company, -1));
        final State state = AMain.getState(ShrePrefs.readData(this, C.sp_user_state, -1));
        final State disState = AMain.getState(ShrePrefs.readData(this, C.sp_user_district, -1));

        final String user_name = ShrePrefs.readData(this, C.sp_user_name, "");
        final String user_company = company == null ? "" : company.name;
        final String user_state = state == null ? "" : state.name;
        final String user_district = disState == null ? "" : disState.name;

        name_txt.setText(user_name);
        state_txt.setText(user_state);
        company_txt.setText(user_company);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    profilePic.setImageURI(null);
                    profilePic.setImageURI(imageUri);
                }
        }
    }

    @Override
    public void onClick(View view) {

        if (view == closeBtn) {
            finish();
        }

        if (view == editBtn) {

            final Intent intent = new Intent(this, ASignUp.class);
            intent.putExtra("skip_complete", true);
            intent.putExtra("hide_skip", true);
            intent.putExtra("hide_progress", true);
            startActivity(intent);

        }

        if (view == appLangBtn) {

            showRadioButtonDialog();

        }

    }

    private void showRadioButtonDialog() {
        
        prevLang = lang;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_app_language);
        builder.setSingleChoiceItems(
                new String[]{"English", "हिंदी"},
                lang,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case C.LANG_ENGLISH:
                                lang = C.LANG_ENGLISH;
                                break;
                            case C.LANG_HINDI:
                                lang = C.LANG_HINDI;
                                break;
                        }
                    }
                });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();

                if (prevLang != lang) {


                    final AlertDialog.Builder messageAlertBuilder = new AlertDialog.Builder(AProfile.this);
                    messageAlertBuilder.setCancelable(true);
                    messageAlertBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ShrePrefs.writeData(AProfile.this, C.sp_lang, lang);
                            dialog.dismiss();
                            exitApp();
                            Intent intent = new Intent(AProfile.this, ASplash.class);
                            startActivity(intent);

                        }
                    });
                    messageAlertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            lang = prevLang;
                            dialog.dismiss();

                        }
                    });
                    messageAlertBuilder.setTitle(R.string.restart_to_apply_changes);
                    messageAlertBuilder.show();

                }

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                lang = prevLang;
                dialogInterface.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

    }
}
