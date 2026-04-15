package com.lieruce.goforlunch.ui.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lieruce.goforlunch.databinding.FragmentWorkmatesBinding;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;
import com.lieruce.goforlunch.viewmodel.WorkmatesViewModel;

public class WorkmatesFragment extends Fragment {

    private FragmentWorkmatesBinding binding;
    private WorkmatesViewModel viewModel;
    private WorkmateAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkmatesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        observeWorkmates();
    }

    private void setupViewModel() {
        ViewModelFactory factory = ViewModelFactory.getInstance(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(WorkmatesViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new WorkmateAdapter();
        binding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workmatesRecyclerView.setAdapter(adapter);
    }

    private void observeWorkmates() {
        viewModel.getWorkmates().observe(getViewLifecycleOwner(), workmates -> {
            if (workmates != null) {
                adapter.setWorkmates(workmates);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
