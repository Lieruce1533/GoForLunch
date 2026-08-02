package com.lieruce.goforlunch.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.ChatRepository;
import com.lieruce.goforlunch.repository.GooglePlacesRepository;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.repository.MockRestaurantRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private static volatile ViewModelFactory instance;
    private final AuthRepository authRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;

    public static ViewModelFactory getInstance(Context context) {
        if (instance == null) {
            synchronized (ViewModelFactory.class) {
                if (instance == null) {
                    instance = new ViewModelFactory(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private ViewModelFactory(Context context) {
        this.authRepository = AuthRepository.getInstance();
        this.userRepository = UserRepository.getInstance();
        this.locationRepository = LocationRepository.getInstance(context);
        
        SharedPreferences prefs = context.getSharedPreferences("go4lunch_prefs", Context.MODE_PRIVATE);
        boolean useMock = prefs.getBoolean("presentation_mode", true);
        
        if (useMock) {
            this.restaurantRepository = new MockRestaurantRepository();
        } else {
            this.restaurantRepository = GooglePlacesRepository.getInstance(context);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(authRepository, userRepository);
        }
        if (modelClass.isAssignableFrom(MapsViewModel.class)) {
            return (T) new MapsViewModel(locationRepository, restaurantRepository, userRepository);
        }
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            return (T) new RestaurantDetailViewModel(restaurantRepository, userRepository, authRepository);
        }
        if (modelClass.isAssignableFrom(WorkmatesViewModel.class)) {
            return (T) new WorkmatesViewModel(userRepository);
        }
        if (modelClass.isAssignableFrom(ChatViewModel.class)) {
            return (T) new ChatViewModel(ChatRepository.getInstance(), authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
