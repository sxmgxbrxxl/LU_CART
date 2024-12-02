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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        com.advento.lucart.databinding.ActivityBusinessHomeBinding binding = ActivityBusinessHomeBinding.inflate(getLayoutInflater());
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

        setCurrentFragment(businessHomeFragment);

        binding.bnvBusiness.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.miHome) {
                setCurrentFragment(businessHomeFragment);
            } else if (itemId == R.id.miProducts) {
                setCurrentFragment(myProductsFragment);
            } else if (itemId == R.id.miTransactions) {
                setCurrentFragment(myTransactionsFragment);
            } else if (itemId == R.id.miProfile) {
                setCurrentFragment(businessProfileFragment);
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