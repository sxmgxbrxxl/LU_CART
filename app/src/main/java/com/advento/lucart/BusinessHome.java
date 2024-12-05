package com.advento.lucart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.advento.lucart.databinding.ActivityBusinessHomeBinding;

public class BusinessHome extends AppCompatActivity {

    private ActivityBusinessHomeBinding binding;
    private int lastSelectedItemOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityBusinessHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        Fragment businessHomeFragment = new BusinessHomeFragment();
        Fragment myProductsFragment = new MyProductsFragment();
        Fragment myTransactionsFragment = new TransactionsFragment();
        Fragment businessProfileFragment = new BusinessProfileFragment();

        setCurrentFragment(businessHomeFragment, 0);

        binding.bnvBusiness.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.miHome) {
                selectedFragment = businessHomeFragment;
            } else if (itemId == R.id.miProducts) {
                selectedFragment = myProductsFragment;
            } else if (itemId == R.id.miTransactions) {
                selectedFragment = myTransactionsFragment;
            } else if (itemId == R.id.miProfile) {
                selectedFragment = businessProfileFragment;
            }

            if (selectedFragment != null) {
                setCurrentFragment(selectedFragment, itemId);
            }

            return true;
        });
    }

    private int getMenuOrder(int menuItemId) {
        if (menuItemId == R.id.miHome) {
            return 0;
        } else if (menuItemId == R.id.miProducts) {
            return 1;
        } else if (menuItemId == R.id.miTransactions) {
            return 2;
        } else if (menuItemId == R.id.miProfile) {
            return 3;
        } else {
            return -1;
        }
    }

    private void setCurrentFragment(Fragment fragment, int menuItemId) {
        int currentOrder = getMenuOrder(menuItemId);

        if (currentOrder != lastSelectedItemOrder) {
            int enterAnim, exitAnim, popEnterAnim, popExitAnim;

            if (currentOrder > lastSelectedItemOrder) {
                enterAnim = R.anim.slide_in_right;
                exitAnim = R.anim.slide_out_left;
                popEnterAnim = R.anim.slide_in_left;
                popExitAnim = R.anim.slide_out_right;
            } else {
                enterAnim = R.anim.slide_in_left;
                exitAnim = R.anim.slide_out_right;
                popEnterAnim = R.anim.slide_in_right;
                popExitAnim = R.anim.slide_out_left;
            }

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
                    .replace(binding.flFragment.getId(), fragment)
                    .commit();

            lastSelectedItemOrder = currentOrder;
        }
    }
}