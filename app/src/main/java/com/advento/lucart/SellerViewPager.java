package com.advento.lucart;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SellerViewPager extends FragmentStateAdapter {

    // Constructor to pass the FragmentActivity
    public SellerViewPager(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment based on position
        switch (position) {
            case 0:
                return new fragment_seller_toship();  // To Ship fragment
            case 1:
                return new fragment_seller_toreceive();  // To Receive fragment
            case 2:
                return new fragment_seller_completed();  // Completed fragment
            default:
                return new fragment_seller_toship();  // Default to To Ship
        }
    }

    @Override
    public int getItemCount() {
        return 3;  // We have 3 fragments: To Ship, To Receive, Completed
    }
}
