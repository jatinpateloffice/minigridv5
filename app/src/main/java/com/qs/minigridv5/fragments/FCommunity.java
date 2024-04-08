package com.qs.minigridv5.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;
import com.qs.minigridv5.misc.Helpers;

import java.util.ArrayList;

public class FCommunity extends MyFragment {

    private FrameLayout noInternetLayout;

    public FCommunity() {

    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onResume() {

        super.onResume();

        parentActivity.checkForSettings();
        parentActivity.setToolbarText(R.string.title_community);
        parentActivity.navigationView.getMenu().getItem(C.NAV_COMMUNITY_IDX).setChecked(true);

        if (Helpers.isNetworkAvailable(getContext())) {
            noInternetLayout.setVisibility(View.GONE);
            //loadingContent.setVisibility(View.VISIBLE);
        } else {
            noInternetLayout.setVisibility(View.VISIBLE);
            //loadingContent.setVisibility(View.GONE);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View      view      = inflater.inflate(R.layout.f_community, container, false);
        final TabLayout tabLayout = view.findViewById(R.id.f_webvideos_tabs);
        final ViewPager viewPager = view.findViewById(R.id.f_community_webvideos_viewpager);

        noInternetLayout = view.findViewById(R.id.f_community_no_internet);


        final FWebVideos featuredYouTubeVideos = new FWebVideos();
        Bundle           bundle                = new Bundle();
        bundle.putString(FWebVideos.KEY_WEB_VIDEOS, C.sp_featured_videos_response);
        featuredYouTubeVideos.setArguments(bundle);

        final FWebVideos userYouTubeVideos = new FWebVideos();
        bundle = new Bundle();
        bundle.putString(FWebVideos.KEY_WEB_VIDEOS, C.sp_user_videos_response);
        userYouTubeVideos.setArguments(bundle);


        final WebVideoViewPagerAdapter vpa = new WebVideoViewPagerAdapter(this.getChildFragmentManager());
        vpa.addFrag(featuredYouTubeVideos, "Featured Videos");
        vpa.addFrag(userYouTubeVideos, "User Videos");
        viewPager.setAdapter(vpa);
        tabLayout.setupWithViewPager(viewPager);

        final TextView tryAgainBtn = view.findViewById(R.id.f_community_try_again);

        tryAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        return view;
    }

    @Override
    public String getName() {
        return "FCommunity";
    }

    private class WebVideoViewPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> youTubeFragments      = new ArrayList<>();
        ArrayList<String>   youTubeFragmentTitles = new ArrayList<>();

        public WebVideoViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return youTubeFragments.get(position);
        }

        @Override
        public int getCount() {
            return youTubeFragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return youTubeFragmentTitles.get(position);
        }

        public void addFrag(Fragment fragment, String title) {
            youTubeFragments.add(fragment);
            youTubeFragmentTitles.add(title);
        }

        public void addFrag(Fragment fragment, int title) {
            youTubeFragments.add(fragment);
            youTubeFragmentTitles.add(getResources().getString(title));
        }

    }
}
