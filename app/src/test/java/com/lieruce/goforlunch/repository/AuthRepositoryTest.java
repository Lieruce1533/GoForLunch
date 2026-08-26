package com.lieruce.goforlunch.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

public class AuthRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AuthRepository authRepository;
    private MockedStatic<FirebaseAuth> mockedFirebaseAuth;
    private FirebaseAuth mockAuth;

    @Before
    public void setUp() {
        mockAuth = mock(FirebaseAuth.class);
        mockedFirebaseAuth = mockStatic(FirebaseAuth.class);
        mockedFirebaseAuth.when(FirebaseAuth::getInstance).thenReturn(mockAuth);

        authRepository = AuthRepository.getInstance();
    }

    @After
    public void tearDown() {
        mockedFirebaseAuth.close();
        // Reset singleton for next tests
        try {
            java.lang.reflect.Field instance = AuthRepository.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCurrentUser_shouldReturnFirebaseUser() {
        // Arrange
        FirebaseUser mockUser = mock(FirebaseUser.class);
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        // Act
        FirebaseUser result = authRepository.getCurrentUser();

        // Assert
        assertEquals(mockUser, result);
    }
}
