package com.qs.minigridv5.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.qs.minigridv5.R;
import com.qs.minigridv5.entities.Project;
import com.qs.minigridv5.misc.C;

import java.util.ArrayList;

public class FMake extends MyFragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;

    public FCurrentProjects fCurrentProjects;
    public FFinishedProjects fFinishedProjects;

    public FMake() {

    }

    @Override
    public void onResume() {
        super.onResume();
        parentActivity.setToolbarText(R.string.title_make);
        parentActivity.navigationView.getMenu().getItem(C.NAV_MAKE_IDX).setChecked(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(C.T, getClass().getSimpleName() + " onCreateView");
        final View view = inflater.inflate(R.layout.f_make, container, false);

        tabLayout = view.findViewById(R.id.f_project_tabs);
        viewPager = view.findViewById(R.id.f_project_viewpager);

        ViewPagerAdapter vpa = new ViewPagerAdapter(this.getChildFragmentManager());

        fCurrentProjects = new FCurrentProjects();
        fFinishedProjects = new FFinishedProjects();

        vpa.addFrag(fCurrentProjects, R.string.tab_title_current_projects);
        vpa.addFrag(fFinishedProjects, R.string.tab_title_finished_projects);

        viewPager.setAdapter(vpa);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public String getName() {
        return "FMake";
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
