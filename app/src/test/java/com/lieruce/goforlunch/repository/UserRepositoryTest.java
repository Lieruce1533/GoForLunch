package com.lieruce.goforlunch.repository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

@RunWith(JUnit4.class)
public class UserRepositoryTest {

    private UserRepository userRepository;

    @Mock
    private CollectionReference mockCollection;
    
    @Mock
    private DocumentReference mockDocument;
    
    @Mock
    private FirebaseUser mockFirebaseUser;

    @Before
    public void setUp() {
        // Initialize mocks annotated with @Mock
        MockitoAnnotations.openMocks(this);
        
        // Arrange: tell the mock collection to return a mock document when document() is called
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        
        // Initialize our repository with the mock collection
        userRepository = new UserRepository(mockCollection);
    }

    @Test
    public void createUser_shouldCallFirestoreSetWithCorrectData() {
        // 1. Arrange (Setup the specific data for this test)
        String uid = "test_uid";
        String name = "Fabien Flint";
        Uri photoUri = Uri.parse("https://example.com/photo.jpg");

        when(mockFirebaseUser.getUid()).thenReturn(uid);
        when(mockFirebaseUser.getDisplayName()).thenReturn(name);
        when(mockFirebaseUser.getPhotoUrl()).thenReturn(photoUri);

        // 2. Act (Call the method we want to test)
        userRepository.createUser(mockFirebaseUser);

        // 3. Assert (Verify that Firestore was called correctly)
        // We verify that document(uid) was called on our collection
        verify(mockCollection).document(uid);
        
        // We verify that set() was called on that document with a Map containing our data
        // and using the merge() option.
        verify(mockDocument).set(anyMap(), eq(SetOptions.merge()));
    }

    @Test
    public void createUser_withNoName_shouldUseEmailPrefix() {
        // 1. Arrange
        String uid = "test_uid_2";
        String email = "fabien@test.com";
        
        when(mockFirebaseUser.getUid()).thenReturn(uid);
        when(mockFirebaseUser.getDisplayName()).thenReturn(null); // No display name
        when(mockFirebaseUser.getEmail()).thenReturn(email);

        // 2. Act
        userRepository.createUser(mockFirebaseUser);

        // 3. Assert
        // The repository should extract "fabien" from the email
        verify(mockDocument).set(org.mockito.ArgumentMatchers.argThat(map -> 
            "fabien".equals(map.get("username"))
        ), eq(SetOptions.merge()));
    }
}
