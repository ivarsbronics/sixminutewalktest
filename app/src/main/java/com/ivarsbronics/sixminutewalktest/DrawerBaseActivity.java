package com.ivarsbronics.sixminutewalktest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /*authentication variable*/
    private FirebaseAuth mAuth;
    /*current user variable*/
    private FirebaseUser currentUser;
    /*variable for drawer menu*/
    DrawerLayout drawerLayout;

    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base,null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Toolbar toolbar = drawerLayout.findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView userEmail = (TextView) header.findViewById(R.id.userEmail);
        userEmail.setText(currentUser.getEmail());
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_drawer_open, R.string.menu_drawer_closed);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.navStartTest:
                startActivity(new Intent(this, SixMWTActivity.class));
                overridePendingTransition(0,0);
                break;

            case R.id.navDashboard:
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0,0);
                break;

            case R.id.navHome:
                startActivity(new Intent(DrawerBaseActivity.this, HomeActivity.class));
                overridePendingTransition(0,0);
                break;

            /*case R.id.navFourth:
                startActivity(new Intent(this, DashboardActivity.class));
                overridePendingTransition(0,0);
                break;*/

            case R.id.navParameters:
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0,0);
                break;

            case R.id.navLogout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                overridePendingTransition(0,0);
                break;

            case R.id.navCloseApp:
                finish();
                System.exit(0);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}