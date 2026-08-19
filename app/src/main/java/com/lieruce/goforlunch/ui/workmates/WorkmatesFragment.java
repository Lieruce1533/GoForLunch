package com.lieruce.goforlunch.ui.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.FragmentWorkmatesBinding;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;
import com.lieruce.goforlunch.viewmodel.WorkmatesViewModel;

/**
 * Fragment displaying the global list of coworkers and their current lunch choices.
 * Supports reactive searching and direct navigation to coworkers' chosen restaurants.
 */
public class WorkmatesFragment extends Fragment implements WorkmateAdapter.OnWorkmateClickListener {

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
        viewModel = new ViewModelProvider(requireActivity(), factory).get(WorkmatesViewModel.class);
    }

    private void setupRecyclerView() {
        String currentUserId = AuthRepository.getInstance().getCurrentUser() != null 
                ? AuthRepository.getInstance().getCurrentUser().getUid() 
                : null;
        adapter = new WorkmateAdapter(currentUserId, this);
        binding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workmatesRecyclerView.setAdapter(adapter);
    }

    private void observeWorkmates() {
        viewModel.getWorkmates().observe(getViewLifecycleOwner(), workmates -> {
            if (workmates != null) {
                adapter.setWorkmates(workmates);
                if (workmates.isEmpty()) {
                    binding.workmatesEmptyView.setVisibility(View.VISIBLE);
                    binding.workmatesRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.workmatesEmptyView.setVisibility(View.GONE);
                    binding.workmatesRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onWorkmateClick(User user) {
        if (user.getChosenRestaurantId() != null) {
            Bundle args = new Bundle();
            args.putString("restaurantId", user.getChosenRestaurantId());
            Navigation.findNavController(requireView()).navigate(R.id.restaurantDetailFragment, args);
        } else {
            String currentUserId = AuthRepository.getInstance().getCurrentUser() != null
                    ? AuthRepository.getInstance().getCurrentUser().getUid()
                    : "";
            
            String message;
            if (user.getUid() != null && user.getUid().equals(currentUserId)) {
                message = getString(R.string.current_user_status_undecided);
            } else {
                String name = user.getUsername() != null ? user.getUsername() : getString(R.string.workmates_image); // Fallback string
                message = getString(R.string.workmate_status_undecided, name);
            }
            
            Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
