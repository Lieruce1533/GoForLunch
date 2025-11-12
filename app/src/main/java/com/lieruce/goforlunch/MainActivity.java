package com.lieruce.goforlunch;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this, ViewModelFactory.getInstance()).get(MainViewModel.class);

        // Observe the user's authentication state
        viewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                // User is signed in, setup the main UI
                setupNavigation();
            } else {
                // No user is signed in, launch the sign-in flow
                launchSignInFlow();
            }
        });
    }

    private void setupNavigation() {
        // Find the NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // Define top-level destinations
        Set<Integer> topLevelDestinations = new HashSet<>();
        topLevelDestinations.add(R.id.navigation_map);
        topLevelDestinations.add(R.id.navigation_restaurants);
        topLevelDestinations.add(R.id.navigation_workmates);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(topLevelDestinations).build();

        // Link the NavController to the Toolbar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Link the BottomNavigationView to the NavController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        // Set the menu for the BottomNavigationView
        binding.bottomNavView.getMenu().clear(); // Clear existing menu
        binding.bottomNavView.inflateMenu(R.menu.main_menu);

        // Set the navigation graph
        navController.setGraph(R.navigation.nav_graph);
    }

    private void launchSignInFlow() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in, tell the ViewModel to refresh its state
            viewModel.refreshUser(); // The observer will handle the UI update

            // Always attempt to create/update the user in Firestore upon successful login
            viewModel.createUser().addOnSuccessListener(aVoid -> {
                showSnackBar("User data synced with Firestore!");
            }).addOnFailureListener(e -> {
                showSnackBar("Error syncing user data with Firestore.");
            });
        } else {
            // Sign in failed
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
