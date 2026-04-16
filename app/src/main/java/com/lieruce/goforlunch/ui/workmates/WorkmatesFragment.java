package com.lieruce.goforlunch.ui.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.FragmentWorkmatesBinding;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;
import com.lieruce.goforlunch.viewmodel.WorkmatesViewModel;

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
        viewModel = new ViewModelProvider(this, factory).get(WorkmatesViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new WorkmateAdapter(this);
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
    public void onWorkmateClick(User user) {
        if (user.getChosenRestaurantId() != null) {
            Bundle args = new Bundle();
            args.putString("restaurantId", user.getChosenRestaurantId());
            Navigation.findNavController(requireView()).navigate(R.id.restaurantDetailFragment, args);
        } else {
            Toast.makeText(requireContext(), user.getUsername() + " hasn't decided yet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
