package com.lieruce.goforlunch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
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
import com.lieruce.goforlunch.viewmodel.MapsViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private MapsViewModel mapsViewModel;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private MenuProvider currentMenuProvider;

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

        ViewModelFactory factory = ViewModelFactory.getInstance(this);
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        mapsViewModel = new ViewModelProvider(this, factory).get(MapsViewModel.class);

        setupToolbar();
        setupNavigation(); // Initialize navigation once here

        mainViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
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
        if (navController != null) return; // Prevent multiple initializations

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

            // Listen for destination changes to show/hide search bar
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.navigation_map || destination.getId() == R.id.navigation_restaurants) {
                    showSearchMenu();
                } else {
                    hideSearchMenu();
                }
            });

            setupDrawerContent();
        }
    }

    private void showSearchMenu() {
        if (currentMenuProvider == null) {
            currentMenuProvider = new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    menuInflater.inflate(R.menu.options_menu, menu);
                    MenuItem searchItem = menu.findItem(R.id.action_search);
                    SearchView searchView = (SearchView) searchItem.getActionView();
                    if (searchView != null) {
                        searchView.setQueryHint(getString(R.string.search));
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                mapsViewModel.setSearchQuery(query);
                                return true;
                            }
                            @Override
                            public boolean onQueryTextChange(String newText) {
                                mapsViewModel.setSearchQuery(newText);
                                return true;
                            }
                        });
                    }
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    return false;
                }
            };
            addMenuProvider(currentMenuProvider);
        }
    }

    private void hideSearchMenu() {
        if (currentMenuProvider != null) {
            removeMenuProvider(currentMenuProvider);
            currentMenuProvider = null;
            // Clear search query when leaving searchable fragments
            mapsViewModel.setSearchQuery("");
        }
    }

    private void setupDrawerContent() {
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                mainViewModel.signOut(this).addOnSuccessListener(aVoid -> {
                    mainViewModel.refreshUser();
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
        mainViewModel.getCurrentUserData().addOnSuccessListener(documentSnapshot -> {
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

        mainViewModel.getUserLiveData().observe(this, firebaseUser -> {
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
            // Using post to ensure activity is in a valid state to show the permission dialog
            binding.getRoot().post(() -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION));
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
            mainViewModel.refreshUser();
            mainViewModel.createUser().addOnSuccessListener(aVoid -> {
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
