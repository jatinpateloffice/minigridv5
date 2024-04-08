package com.qs.minigridv5.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.ShrePrefs;

public class ALanguageSelect extends AppCompatActivity {

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_language_select);

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.check(R.id.a_lang_hindi);


    }

    public void next(View view) {

        int checked = radioGroup.getCheckedRadioButtonId();

        switch (checked) {

            case R.id.a_lang_english:
                ShrePrefs.writeData(this, C.sp_lang, C.LANG_ENGLISH);
                break;

            case R.id.a_lang_hindi:
                ShrePrefs.writeData(this, C.sp_lang, C.LANG_HINDI);
                break;

            default:
                Toast.makeText(this, R.string.select_app_language, Toast.LENGTH_SHORT).show();
                break;
        }


        ShrePrefs.writeData(this, C.sp_load_lang_select, false);
        final Intent intent = new Intent(this, AIntroCarousel.class);
        startActivity(intent);


//        final Intent intent = new Intent(this, AVideoPlayer.class);
//        intent.putExtra(AVideoPlayer.KEY_FILE_NAME, Helpers.getAssetString(this, C.OVERVIEW_VIDEO_NAME));
//        intent.putExtra(AVideoPlayer.KEY_CAN_EDIT, false);
//        intent.putExtra(AVideoPlayer.KEY_KEEP_ASPECT_RATIO, false);
//        intent.putExtra(AVideoPlayer.KEY_CONTINUE_BTN_TEXT_ID, R.string._continue);
//        intent.putExtra(AVideoPlayer.KEY_SHOW_TEXT, false);
//        intent.putExtra(AVideoPlayer.KEY_FROM_ASSETS, true);
//        startActivity(intent);
//
//        AVideoPlayer.setContinueClickListener(new AVideoPlayer.ContinueClickListener() {
//            @Override
//            public void onContinueClicked(Activity activity) {
//
//                final Intent intent = new Intent(activity, ASignUp.class);
//                intent.putExtra(ASignUp.KEY_SKIP_INTRO, false);
//                startActivity(intent);
//
//            }
//        });


    }

}
