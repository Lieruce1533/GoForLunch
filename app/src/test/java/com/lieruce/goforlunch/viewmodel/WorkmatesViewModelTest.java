package com.lieruce.goforlunch.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class WorkmatesViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private WorkmatesViewModel viewModel;
    private UserRepository userRepository;
    private AuthRepository authRepository;

    @Before
    public void setUp() {
        userRepository = mock(UserRepository.class);
        authRepository = mock(AuthRepository.class);
        MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

        when(authRepository.getUserLiveData()).thenReturn(userLiveData);
        
        // Mock Firestore Query
        CollectionReference mockCollection = mock(CollectionReference.class);
        when(userRepository.getAllUsers()).thenReturn(mockCollection);

        viewModel = new WorkmatesViewModel(userRepository, authRepository);
        
        // Observe to trigger MediatorLiveData
        viewModel.getWorkmates().observeForever(workmates -> {});
    }

    @Test
    public void searchFilter_shouldReturnOnlyWorkmatesEatingAtMatchingRestaurant() throws Exception {
        // Arrange
        List<User> allUsers = new ArrayList<>();
        User u1 = new User("1", "John", null);
        u1.setChosenRestaurantName("Pizza Palace");
        
        User u2 = new User("2", "Jane", null);
        u2.setChosenRestaurantName("Burger King");
        
        User u3 = new User("3", "Bob", null);
        u3.setChosenRestaurantName("Pizza Hut");
        
        allUsers.add(u1);
        allUsers.add(u2);
        allUsers.add(u3);

        // Inject data into the private rawWorkmates LiveData using reflection for testing purposes
        Field rawField = WorkmatesViewModel.class.getDeclaredField("rawWorkmates");
        rawField.setAccessible(true);
        MutableLiveData<List<User>> rawWorkmates = (MutableLiveData<List<User>>) rawField.get(viewModel);
        rawWorkmates.setValue(allUsers);

        // Act
        viewModel.setSearchQuery("Pizza");

        // Assert
        List<User> filtered = viewModel.getWorkmates().getValue();
        assertNotNull(filtered);
        assertEquals(2, filtered.size());
        assertEquals("John", filtered.get(0).getUsername());
        assertEquals("Bob", filtered.get(1).getUsername());
    }
}
