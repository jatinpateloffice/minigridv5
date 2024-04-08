package com.qs.minigridv5.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.utilities.MyApplication;

public class AProcessCarousel extends MyActivity implements ViewPager.OnPageChangeListener {

    public static final String KEY_PROJECT_ID = "project_id";


    private ViewPager viewPager;

    private Button      finishButton;
    private ImageView   nextButton;
    private ImageView[] dots;

    private final int noofPages = 4;

    private long project_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_process_carousel);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        finishButton = findViewById(R.id.a_process_btn_finish);
        nextButton = findViewById(R.id.a_process_btn_next);


        // setting up dots
        LinearLayout dots_container = findViewById(R.id.a_process_carousel_dots_container);
        dots = new ImageView[noofPages];
        final int dotDimension = 8;
        int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dotDimension, getResources().getDisplayMetrics());

        for (int i = 0; i < noofPages; i++) {

            final ImageView dotImg = new ImageView(this);
            dotImg.setImageResource(R.drawable.ic_radio_button_unchecked_white_24dp);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimensionInDp, dimensionInDp);
            layoutParams.setMargins(4,4,4,4);
            dotImg.setLayoutParams(layoutParams);

            dots[i] = dotImg;

            dots_container.addView(dotImg);

        }

        viewPager = findViewById(R.id.a_process_carousel_viewpager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(this);


        project_id = getIntent().getLongExtra(KEY_PROJECT_ID, -1);

    }

    public void gotoNext(View view) {

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

    }

    public void start(View view) {

        MyApplication.getInstance().flowManager.gotoNextActivity(this, project_id);

    }

    @Override
    public void onPageScrolled(int position, float v, int i1) {

        final int last = noofPages - 1;
        nextButton.setVisibility(position == last ? View.GONE : View.VISIBLE);
        finishButton.setVisibility(position == last ? View.VISIBLE : View.GONE);
        updateIndicators(position);
    }

    @Override
    public void onPageSelected(int position) {

        final int last = noofPages - 1;
        nextButton.setVisibility(position == last ? View.GONE : View.VISIBLE);
        finishButton.setVisibility(position == last ? View.VISIBLE : View.GONE);
        updateIndicators(position);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void updateIndicators(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(
                    i == position ?
                            R.drawable.indicator_selected :
                            R.drawable.ic_radio_button_unchecked_white_24dp
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideStatusBar();
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        private int position;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle                             args     = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            position = getArguments().getInt(ARG_SECTION_NUMBER);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.f_carousel, container, false);

            String headerText = "Page " + position;
            String bodyText = "Body text for page " + position;
            final int imgRes;

            switch (position) {

                case 1:
                    imgRes = R.drawable.step_shoot;
                    headerText = "";
                    bodyText = "Record a video";
                    break;
                case 2:
                    imgRes = R.drawable.step_selfie;
                    headerText = "";
                    bodyText = "Shoot a Selfie";
                    break;
                case 3:
                    imgRes = R.drawable.step_talking;
                    headerText = "";
                    bodyText = "Record Audio";
                    break;
                case 4:
                    imgRes = R.drawable.step_congrats;
                    headerText = "";
                    bodyText = "Congratulations";
                    break;
                default:
                    imgRes = R.drawable.step_shoot;
                    headerText = "";
                    bodyText = "Record a video";
                    break;

            }

            final TextView headerTextView = rootView.findViewById(R.id.f_intro_header);
            headerTextView.setText(headerText);
            final TextView bodyTextView = rootView.findViewById(R.id.f_intro_text);
            bodyTextView.setText(bodyText);
            final ImageView img = rootView.findViewById(R.id.f_intro_image);
            Glide.with(this).load(imgRes).into(img);

            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {

            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return noofPages;
        }
    }

}
