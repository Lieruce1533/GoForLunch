package com.lieruce.goforlunch.viewmodel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.auth.FirebaseUser;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class MainViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MainViewModel mainViewModel;
    private AuthRepository authRepository;
    private UserRepository userRepository;

    @Before
    public void setUp() {
        authRepository = mock(AuthRepository.class);
        userRepository = mock(UserRepository.class);
        mainViewModel = new MainViewModel(authRepository, userRepository);
    }

    @Test
    public void createUser_shouldCallUserRepository() {
        // Arrange
        FirebaseUser mockUser = mock(FirebaseUser.class);
        when(authRepository.getCurrentUser()).thenReturn(mockUser);

        // Act
        mainViewModel.createUser();

        // Assert
        verify(userRepository).createUser(mockUser);
    }
}
