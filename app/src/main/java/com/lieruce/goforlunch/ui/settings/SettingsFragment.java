package com.lieruce.goforlunch.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import com.lieruce.goforlunch.databinding.FragmentSettingsBinding;
import com.lieruce.goforlunch.worker.WorkManagerHelper;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private static final String PREFS_NAME = "go4lunch_prefs";
    private static final String KEY_NOTIFICATIONS = "enable_notifications";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        binding.switchNotifications.setChecked(isEnabled);

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            if (isChecked) {
                WorkManagerHelper.scheduleLunchReminder(requireContext());
            } else {
                WorkManagerHelper.cancelLunchReminder(requireContext());
            }
        });

        // --- LANGUAGE SWITCH ---
        // 1. Get current app locale
        LocaleListCompat currentAppLocales = AppCompatDelegate.getApplicationLocales();
        boolean isFrench = !currentAppLocales.isEmpty() && "fr".equals(currentAppLocales.get(0).getLanguage());
        
        // 2. Set switch state without triggering listener initially
        binding.switchLanguage.setChecked(isFrench);

        // 3. Handle language change
        binding.switchLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String languageTag = isChecked ? "fr" : "en";
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
