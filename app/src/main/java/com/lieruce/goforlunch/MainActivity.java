package com.lieruce.goforlunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.snackbar.Snackbar;
import com.lieruce.goforlunch.databinding.ActivityMainBinding;
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

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startLocationUpdates();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied.
                    showSnackBar("Location permission denied. Map features disabled.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MainViewModel.class);

        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                setupNavigation();
                requestLocationPermission();
            } else {
                launchSignInFlow();
            }
        });
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.navigation_map);
        topLevelDestinations.add(R.id.navigation_restaurants);
        topLevelDestinations.add(R.id.navigation_workmates);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);
        binding.bottomNavView.getMenu().clear();
        binding.bottomNavView.inflateMenu(R.menu.main_menu);
        navController.setGraph(R.navigation.nav_graph);
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
}
