package com.lieruce.goforlunch.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class RestaurantDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private RestaurantDetailViewModel viewModel;
    private RestaurantRepository restaurantRepository;
    private UserRepository userRepository;
    private AuthRepository authRepository;

    @Before
    public void setUp() {
        restaurantRepository = mock(RestaurantRepository.class);
        userRepository = mock(UserRepository.class);
        authRepository = mock(AuthRepository.class);

        // Mock Firestore Query returned by getUsersEatingAt
        Query mockQuery = mock(Query.class);
        when(userRepository.getUsersEatingAt(anyString())).thenReturn(mockQuery);

        viewModel = new RestaurantDetailViewModel(restaurantRepository, userRepository, authRepository);
    }

    @Test
    public void setRestaurantId_shouldFetchDetailsAndWorkmates() {
        // Arrange
        String rid = "test_id";
        Restaurant mockRestaurant = new Restaurant(rid, "Test", "Addr", 4.0, null, 0, 0, null, null, null, null);
        Task<Restaurant> mockTask = mock(Task.class);
        when(restaurantRepository.getRestaurantDetails(rid)).thenReturn(mockTask);

        // Act
        viewModel.setRestaurantId(rid);

        // Assert
        verify(restaurantRepository).getRestaurantDetails(rid);
        verify(userRepository).getUsersEatingAt(rid);
    }
}
