package com.lieruce.goforlunch.viewmodel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.ChatRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

public class ChatViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ChatViewModel chatViewModel;
    private ChatRepository chatRepository;
    private AuthRepository authRepository;
    private MockedStatic<Log> mockedLog;

    @Before
    public void setUp() {
        chatRepository = mock(ChatRepository.class);
        authRepository = mock(AuthRepository.class);

        CollectionReference mockCollection = mock(CollectionReference.class);
        when(chatRepository.getChatMessages()).thenReturn(mockCollection);

        mockedLog = mockStatic(Log.class);

        chatViewModel = new ChatViewModel(chatRepository, authRepository);
    }

    @After
    public void tearDown() {
        mockedLog.close();
    }

    @Test
    public void sendMessage_shouldCallChatRepositoryWhenUserValid() {
        // Arrange
        FirebaseUser mockUser = mock(FirebaseUser.class);
        when(mockUser.getUid()).thenReturn("user123");
        when(mockUser.getDisplayName()).thenReturn("Test User");
        Uri mockUri = mock(Uri.class);
        when(mockUri.toString()).thenReturn("https://example.com/photo.jpg");
        when(mockUser.getPhotoUrl()).thenReturn(mockUri);

        when(authRepository.getCurrentUser()).thenReturn(mockUser);

        // Act
        chatViewModel.sendMessage("Hello World!");

        // Assert
        verify(chatRepository).sendMessage(any());
    }
}
