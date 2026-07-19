package com.lieruce.goforlunch.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.location.Location;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

public class MapsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MapsViewModel mapsViewModel;
    private LocationRepository locationRepository;
    private RestaurantRepository restaurantRepository;
    private UserRepository userRepository;

    private MutableLiveData<Location> locationLiveData;
    private MutableLiveData<List<Restaurant>> restaurantsLiveData;
    
    private MockedStatic<Log> mockedLog;
    private MockedStatic<Location> mockedLocation;

    @Before
    public void setUp() {
        locationRepository = mock(LocationRepository.class);
        restaurantRepository = mock(RestaurantRepository.class);
        userRepository = mock(UserRepository.class);

        locationLiveData = new MutableLiveData<>();
        restaurantsLiveData = new MutableLiveData<>();

        when(locationRepository.getLocationLiveData()).thenReturn(locationLiveData);
        when(restaurantRepository.getNearbyRestaurantsLiveData()).thenReturn(restaurantsLiveData);
        
        // Mock Firestore Query returned by getAllUsers
        CollectionReference mockCollection = mock(CollectionReference.class);
        when(userRepository.getAllUsers()).thenReturn(mockCollection);

        // Mock Android Log
        mockedLog = mockStatic(Log.class);
        
        mapsViewModel = new MapsViewModel(locationRepository, restaurantRepository, userRepository);
        
        // Observe to trigger MediatorLiveData
        mapsViewModel.getNearbyRestaurants().observeForever(restaurants -> {});
        mapsViewModel.getLocationToUse().observeForever(location -> {});
    }

    @After
    public void tearDown() {
        mockedLog.close();
    }

    @Test
    public void locationUpdate_shouldTriggerRestaurantFetch() {
        // Arrange
        Location mockLocation = mock(Location.class);
        when(mockLocation.getLatitude()).thenReturn(48.8);
        when(mockLocation.getLongitude()).thenReturn(2.3);

        // Act
        locationLiveData.setValue(mockLocation);

        // Assert
        verify(restaurantRepository).fetchNearbyRestaurants(mockLocation);
    }

    @Test
    public void searchFilter_shouldReturnOnlyMatchingRestaurants() {
        // Arrange
        List<Restaurant> allRestaurants = new ArrayList<>();
        allRestaurants.add(new Restaurant("1", "Pizza Palace", "Address 1", 4.5, null, 0, 0, null, null, null, null));
        allRestaurants.add(new Restaurant("2", "Burger King", "Address 2", 4.0, null, 0, 0, null, null, null, null));
        allRestaurants.add(new Restaurant("3", "Pizza Hut", "Address 3", 3.5, null, 0, 0, null, null, null, null));
        
        restaurantsLiveData.setValue(allRestaurants);

        // Act
        mapsViewModel.setSearchQuery("Pizza");

        // Assert
        List<Restaurant> filtered = mapsViewModel.getNearbyRestaurants().getValue();
        assertNotNull(filtered);
        assertEquals(2, filtered.size());
        assertEquals("Pizza Palace", filtered.get(0).getName());
        assertEquals("Pizza Hut", filtered.get(1).getName());
    }
}
