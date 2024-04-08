package com.qs.minigridv5.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.qs.minigridv5.R;
import com.qs.minigridv5.misc.C;

import java.util.ArrayList;

public class FLibrary extends MyFragment {

    private TabLayout tabLayout;
    private  ViewPager viewPager;

    public FLibrary() {

    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.setToolbarText(R.string.title_library);
        parentActivity.navigationView.getMenu().getItem(C.NAV_LIBRARY_IDX).setChecked(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.f_library, container, false);


        tabLayout = view.findViewById(R.id.f_library_tabs);
        viewPager = view.findViewById(R.id.f_library_viewpager);

        ViewPagerAdapter vpa = new ViewPagerAdapter(this.getChildFragmentManager());

        final FLocalMovies fLocalMovies = new FLocalMovies();
        final FPublishedMovies fPublishedMovies = new FPublishedMovies();

        vpa.addFrag(fLocalMovies, R.string.tab_title_local_movies);
        vpa.addFrag(fPublishedMovies, R.string.tab_title_remote_movies);

        viewPager.setAdapter(vpa);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public String getName() {
        return "FLibrary";
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragments      = new ArrayList<>();
        ArrayList<String>   fragmentTitles = new ArrayList<>();


        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFrag(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        public void addFrag(Fragment fragment, int title) {
            fragments.add(fragment);
            fragmentTitles.add(getResources().getString(title));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

    }

}
