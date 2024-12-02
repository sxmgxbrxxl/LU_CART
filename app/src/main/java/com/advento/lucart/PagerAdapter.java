package com.advento.lucart;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class PagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList;

    public PagerAdapter(@NonNull FragmentActivity fragmentActivity, @NonNull List<Fragment> fragments) {
        super(fragmentActivity);
        this.fragmentList = fragments; // Initialize the list of fragments
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the fragment corresponding to the position
        if (position >= 0 && position < fragmentList.size()) {
            return fragmentList.get(position);
        }
        throw new IllegalArgumentException("Invalid position: " + position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size(); // Return the total number of fragments
    }
}
