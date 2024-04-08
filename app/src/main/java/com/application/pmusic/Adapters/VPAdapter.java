package com.application.pmusic.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.application.pmusic.Fragments.Albums;
import com.application.pmusic.Fragments.Favorites;
import com.application.pmusic.Fragments.Recent;

import java.util.ArrayList;

public class VPAdapter extends FragmentStateAdapter {
    public String[] titles = {"Recent", "Albums", "Favorites"};
    private final ArrayList<Fragment> fragments = new ArrayList<>();
    private int currentFragmentPosition = 0;

    public VPAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position){
            case 1:
                fragment = new Albums();
                break;
            case 2:
                fragment = new Favorites();
                break;
            default:
                fragment = new Recent();
                break;
        }
        fragments.add(fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public Fragment getFragment(int position){
        if (position >= 0 && position < fragments.size()) {
            return fragments.get(position);
        } else {
            return null;
        }
    }

    public void setCurrentFragmentPosition(int position) {
        currentFragmentPosition = position;
    }

    public int getCurrentFragmentPosition() {
        return currentFragmentPosition;
    }
}