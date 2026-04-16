package com.lieruce.goforlunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.snackbar.Snackbar;
import com.lieruce.goforlunch.databinding.ActivityMainBinding;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.viewmodel.MainViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startLocationUpdates();
                } else {
                    showSnackBar("Location permission denied. Map features disabled.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance(this)).get(MainViewModel.class);

        setupToolbar();

        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                setupNavigation();
                updateNavHeader();
                requestLocationPermission();
            } else {
                launchSignInFlow();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            Set<Integer> topLevelDestinations = new HashSet<>();
            topLevelDestinations.add(R.id.navigation_map);
            topLevelDestinations.add(R.id.navigation_restaurants);
            topLevelDestinations.add(R.id.navigation_workmates);

            appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            NavigationUI.setupWithNavController(binding.navView, navController);

            setupDrawerContent();
        }
    }

    private void setupDrawerContent() {
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                viewModel.signOut(this).addOnSuccessListener(aVoid -> {
                    viewModel.refreshUser();
                });
            } else if (id == R.id.nav_your_lunch) {
                navigateToYourLunch();
            } else if (id == R.id.nav_settings) {
                showSnackBar("Settings clicked");
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void navigateToYourLunch() {
        viewModel.getCurrentUserData().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null && user.getChosenRestaurantId() != null) {
                Bundle args = new Bundle();
                args.putString("restaurantId", user.getChosenRestaurantId());
                navController.navigate(R.id.restaurantDetailFragment, args);
            } else {
                showSnackBar("You haven't chosen a restaurant yet!");
            }
        });
    }

    private void updateNavHeader() {
        View headerView = binding.navView.getHeaderView(0);
        ImageView avatarView = headerView.findViewById(R.id.nav_header_avatar);
        TextView nameView = headerView.findViewById(R.id.nav_header_name);
        TextView emailView = headerView.findViewById(R.id.nav_header_email);

        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                nameView.setText(firebaseUser.getDisplayName());
                emailView.setText(firebaseUser.getEmail());
                Glide.with(this)
                        .load(firebaseUser.getPhotoUrl())
                        .circleCrop()
                        .into(avatarView);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void startLocationUpdates() {
        LocationRepository.getInstance(this).startLocationUpdates();
        showSnackBar("Location updates started!");
    }

    private void launchSignInFlow() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            viewModel.refreshUser();
            viewModel.createUser().addOnSuccessListener(aVoid -> {
                showSnackBar("User data synced with Firestore!");
            }).addOnFailureListener(e -> {
                showSnackBar("Error syncing user data with Firestore.");
            });
        } else {
            if (response == null) {
                showSnackBar("Sign in cancelled");
            } else if (response.getError() != null) {
                showSnackBar("Sign in failed: " + response.getError().getErrorCode());
            }
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
