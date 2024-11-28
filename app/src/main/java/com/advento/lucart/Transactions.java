package com.advento.lucart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.advento.lucart.databinding.ActivityTransactionsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class Transactions extends AppCompatActivity {

    private ActivityTransactionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.tbTransactions);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up ViewPager2 and TabLayout
        setupViewPagerAndTabs();
    }

    private void setupViewPagerAndTabs() {
        // Create a list of fragments for the tabs
        List<androidx.fragment.app.Fragment> fragments = new ArrayList<>();
        fragments.add(new fragment_to_ship());
        fragments.add(new fragment_to_receive());
        fragments.add(new fragment_completed());
        fragments.add(new fragment_cancelled());

        // Use your custom PagerAdapter
        PagerAdapter adapter = new PagerAdapter(this, fragments);
        binding.viewPager.setAdapter(adapter);

        // Attach TabLayout with ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {

                case 0:
                    tab.setText("To Ship");
                    break;
                case 1:
                    tab.setText("To Receive");
                    break;
                case 2:
                    tab.setText("Completed");
                    break;
                case 3:
                    tab.setText("Cancelled");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
