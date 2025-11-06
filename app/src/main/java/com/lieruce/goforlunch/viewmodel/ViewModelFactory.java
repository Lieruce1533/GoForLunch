package com.lieruce.goforlunch.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lieruce.goforlunch.AuthRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private static volatile ViewModelFactory instance;
    private final AuthRepository authRepository;

    public static ViewModelFactory getInstance() {
        if (instance == null) {
            synchronized (ViewModelFactory.class) {
                if (instance == null) {
                    instance = new ViewModelFactory();
                }
            }
        }
        return instance;
    }

    private ViewModelFactory() {
        this.authRepository = AuthRepository.getInstance();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(authRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
