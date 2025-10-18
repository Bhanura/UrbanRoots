package com.example.urbanroots;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Toolbar, BottomNavigationView, DrawerLayout, and NavigationView
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set Toolbar as ActionBar
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(null); // Ensure no left-side navigation icon
        }

        // Set up Navigation Controller
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        } else {
            throw new IllegalStateException("NavHostFragment not found");
        }

        // Configure AppBar with top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment, R.id.notificationsFragment, R.id.settingsFragment)
                .setOpenableLayout(drawerLayout) // Enable drawer with AppBar
                .build();

        // Set up BottomNavigationView with NavController
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Set up NavigationView (Drawer) menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                navController.navigate(R.id.profileFragment);
            } else if (id == R.id.nav_edit_profile) {
                navController.navigate(R.id.editProfileFragment);
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                navController.navigate(R.id.loginFragment);
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        // Update Toolbar title, visibility, and drawer state based on destination
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String label = destination.getLabel() != null ? destination.getLabel().toString() : "UrbanRoots";
            if (toolbar != null) {
                toolbar.setTitle(label);
                // No back button for any destination (as per your requirement)
                toolbar.setNavigationIcon(null);
            }

            int destinationId = destination.getId();
            if (destinationId == R.id.splashFragment || destinationId == R.id.loginFragment) {
                setToolbarVisibility(false);
                setBottomNavigationVisibility(false);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else if (destinationId == R.id.registerFragment) {
                setToolbarVisibility(true);
                setBottomNavigationVisibility(false);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                setToolbarVisibility(true);
                setBottomNavigationVisibility(true);
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });
    }

    // Inflate the toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Handle Toolbar menu item clicks (right-side menu icon)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_menu) {
            drawerLayout.openDrawer(GravityCompat.END); // Open drawer from right
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle Up button navigation
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    // Method to show/hide Toolbar
    private void setToolbarVisibility(boolean visible) {
        if (toolbar != null) {
            toolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // Method to show/hide BottomNavigationView
    private void setBottomNavigationVisibility(boolean visible) {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}