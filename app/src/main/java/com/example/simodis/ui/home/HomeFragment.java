package com.example.simodis.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.simodis.R;
import com.example.simodis.ui.OnboardingActivity;
import com.example.simodis.data.model.IndikatorStrategis;
import com.example.simodis.data.network.RetrofitClient;
import com.example.simodis.databinding.FragmentHomeBinding;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements IndikatorAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private IndikatorAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        fetchIndikatorData("");
        setupSearchListener();
        setupHelpButtonListener();
    }

    private void setupRecyclerView() {
        binding.rvIndikator.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new IndikatorAdapter(getContext(), new ArrayList<>(), this);
        binding.rvIndikator.setAdapter(adapter);
    }

    private void fetchIndikatorData(String keyword) {
        binding.progressBar.setVisibility(View.VISIBLE);

        Call<List<IndikatorStrategis>> call;
        if (keyword.isEmpty()) {
            call = RetrofitClient.getApiService().getIndikatorStrategis();
        } else {
            call = RetrofitClient.getApiService().searchIndikator(keyword);
        }

        call.enqueue(new Callback<List<IndikatorStrategis>>() {
            @Override
            public void onResponse(@NonNull Call<List<IndikatorStrategis>> call, @NonNull Response<List<IndikatorStrategis>> response) {
                if (binding == null) return; // Mencegah crash
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.filterList((ArrayList<IndikatorStrategis>) response.body());
                } else {
                    Toast.makeText(getContext(), "Gagal memuat data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<IndikatorStrategis>> call, @NonNull Throwable t) {
                if (binding == null) return; // Mencegah crash
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchListener() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRunnable = () -> fetchIndikatorData(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        binding.searchInputLayout.setEndIconOnClickListener(v -> {
            String query = binding.searchEditText.getText().toString();
            fetchIndikatorData(query);
        });
    }

    /**
     * @param indikatorId ID dari indikator yang diklik.
     */
    @Override
    public void onItemClicked(int indikatorId) {
        Bundle bundle = new Bundle();
        bundle.putInt("indikator_id", indikatorId);

        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_indikatorDetailFragment, bundle);
    }

    private void setupHelpButtonListener() {
        binding.btnHelp.setOnClickListener(v -> {
            // Membuat Intent untuk memulai OnboardingActivity
            Intent intent = new Intent(getActivity(), OnboardingActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}