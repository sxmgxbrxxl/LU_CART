package com.advento.lucart;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.advento.lucart.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private int lastSelectedItemOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        Fragment homeFragment = new HomeFragment();
        Fragment myCartFragment = new MyCartFragment();
        Fragment favoritesFragment = new FavoritesFragment();
        Fragment profileFragment = new ProfileFragment();

        setCurrentFragment(homeFragment, 0);

        binding.bnvUser.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.miHome) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.miCart) {
                selectedFragment = myCartFragment;
            } else if (itemId == R.id.miFavorites) {
                selectedFragment = favoritesFragment;
            } else if (itemId == R.id.miProfile) {
                selectedFragment = profileFragment;
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
        } else if (menuItemId == R.id.miCart) {
            return 1;
        } else if (menuItemId == R.id.miFavorites) {
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
