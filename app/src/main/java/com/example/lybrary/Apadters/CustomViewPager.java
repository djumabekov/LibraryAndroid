package com.example.lybrary.Apadters;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


// Адаптер управления фрагментами
public class CustomViewPager extends FragmentPagerAdapter {

    private List<Fragment> fragmentList;
    private List<String> nameList;

    public CustomViewPager(@NonNull FragmentManager fm) {
        super(fm);
        fragmentList = new ArrayList<>();
        nameList = new ArrayList<>();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment, String name){
        fragmentList.add(fragment);
        nameList.add(name);
    }

    //названия фрагментов для ТабЛайаут
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return nameList.get(position);
    }
}
