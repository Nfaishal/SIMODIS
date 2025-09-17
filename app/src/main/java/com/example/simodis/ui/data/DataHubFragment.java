package com.example.simodis.ui.data;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.simodis.databinding.FragmentDataHubBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class DataHubFragment extends Fragment {
    private FragmentDataHubBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDataHubBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DataPagerAdapter pagerAdapter = new DataPagerAdapter(requireActivity());
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Data BPS");
                            break;
                        case 1:
                            tab.setText("Data Sektoral");
                            break;
                        case 2:
                            tab.setText("Infografis");
                            break;
                    }
                }
        ).attach();
    }
}
