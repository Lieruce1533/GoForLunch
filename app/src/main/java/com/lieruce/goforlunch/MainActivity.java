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

import androidx.activity.OnBackPressedCallback;
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
import com.lieruce.goforlunch.viewmodel.WorkmatesViewModel;
import com.lieruce.goforlunch.worker.WorkManagerHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private MapsViewModel mapsViewModel;
    private WorkmatesViewModel workmatesViewModel;
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private MenuProvider currentMenuProvider;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), this::onSignInResult);

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = false;
                if (result.containsKey(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Boolean granted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    fineLocationGranted = granted != null && granted;
                } else {
                    // If not requested now, check current status
                    fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                }

                if (fineLocationGranted) {
                    startLocationUpdates();
                } else {
                    showSnackBar("Location permission denied. Map features disabled.");
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (result.containsKey(Manifest.permission.POST_NOTIFICATIONS)) {
                        Boolean granted = result.get(Manifest.permission.POST_NOTIFICATIONS);
                        if (granted != null && !granted) {
                            showSnackBar("Notifications disabled. You won't receive lunch reminders.");
                        }
                    }
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
        workmatesViewModel = new ViewModelProvider(this, factory).get(WorkmatesViewModel.class);

        setupToolbar();
        setupNavigation(); // Initialize navigation once at the start
        setupBackNavigation();

        WorkManagerHelper.scheduleLunchReminder(this);

        mainViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                updateNavHeader();
                // Request permission only after successful login
                requestPermissions();
                // Sync user data with Firestore on every launch to ensure profile info is up to date
                mainViewModel.createUser();
            } else {
                launchSignInFlow();
            }
        });
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new java.util.ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startLocationUpdates();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionsLauncher.launch(permissionsToRequest.toArray(new String[0]));
        }
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Check if we can navigate up in the navigation stack
                    if (!navController.navigateUp()) {
                        // If not, use the system back behavior (e.g., close app)
                        setEnabled(false); // Temporarily disable this callback
                        getOnBackPressedDispatcher().onBackPressed();
                        setEnabled(true); // Re-enable for next time
                    }
                }
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

            // Force return to list/map/workmates if clicking the tab while in details
            binding.bottomNavigation.setOnItemSelectedListener(item -> {
                int currentId = navController.getCurrentDestination() != null ? navController.getCurrentDestination().getId() : -1;
                if (currentId == R.id.restaurantDetailFragment) {
                    navController.popBackStack();
                }
                return NavigationUI.onNavDestinationSelected(item, navController);
            });

            // Listen for destination changes to show/hide search bar
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.navigation_map || 
                    destination.getId() == R.id.navigation_restaurants ||
                    destination.getId() == R.id.navigation_workmates) {
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
                                workmatesViewModel.setSearchQuery(query);
                                return true;
                            }
                            @Override
                            public boolean onQueryTextChange(String newText) {
                                mapsViewModel.setSearchQuery(newText);
                                workmatesViewModel.setSearchQuery(newText);
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
            workmatesViewModel.setSearchQuery("");
        }
    }

    private void setupDrawerContent() {
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                mainViewModel.signOut(this).addOnSuccessListener(aVoid -> mainViewModel.refreshUser());
            } else if (id == R.id.nav_your_lunch) {
                navigateToYourLunch();
            } else if (id == R.id.nav_settings) {
                navController.navigate(R.id.settingsFragment);
            } else if (id == R.id.chatFragment) {
                navController.navigate(R.id.chatFragment);
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void navigateToYourLunch() {
        mainViewModel.getCurrentUserData().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null && user.getChosenRestaurantId() != null && !user.getChosenRestaurantId().isEmpty()) {
                Bundle args = new Bundle();
                args.putString("restaurantId", user.getChosenRestaurantId());
                navController.navigate(R.id.restaurantDetailFragment, args);
            } else {
                showSnackBar("You haven't chosen a restaurant yet!");
            }
        }).addOnFailureListener(e -> {
            showSnackBar("Error fetching your lunch choice: " + e.getMessage());
        });
    }

    private void updateNavHeader() {
        View headerView = binding.navView.getHeaderView(0);
        ImageView avatarView = headerView.findViewById(R.id.nav_header_avatar);
        TextView nameView = headerView.findViewById(R.id.nav_header_name);
        TextView emailView = headerView.findViewById(R.id.nav_header_email);

        mainViewModel.getUserLiveData().observe(this, firebaseUser -> {
            if (firebaseUser != null) {
                String name = firebaseUser.getDisplayName();
                if ((name == null || name.isEmpty()) && firebaseUser.getEmail() != null) {
                    name = firebaseUser.getEmail().split("@")[0];
                }
                if (name == null || name.isEmpty()) {
                    name = "Anonymous";
                }

                String nameWithMe = name + " (" + getString(R.string.me) + ")";
                nameView.setText(nameWithMe);
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
