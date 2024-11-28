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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        Fragment homeFragment = new HomeFragment();
        Fragment myCartFragment = new MyCartFragment();
        Fragment favoritesFragment = new FavoritesFragment();
        Fragment profileFragment = new ProfileFragment();

        setCurrentFragment(homeFragment);

        binding.bnvUser.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.miHome) {
                setCurrentFragment(homeFragment);
            } else if (itemId == R.id.miCart) {
                setCurrentFragment(myCartFragment);
            } else if (itemId == R.id.miFavorites) {
                setCurrentFragment(favoritesFragment);
            } else if (itemId == R.id.miProfile) {
                setCurrentFragment(profileFragment);
            }

            return true;
        });
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }
}
