package com.advento.lucart;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ViewStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_statistics);

        // Set up the toolbar with a title and back button
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Statistics Overview"); // Customizable title
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Adds the back button
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Finish the activity to return to AdminDashboard
        return true;
    }
}
