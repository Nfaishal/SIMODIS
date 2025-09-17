package com.example.simodis.ui.datasektoral;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simodis.data.model.DataSektoral;
import com.example.simodis.databinding.ItemDataSektoralBinding;
import java.util.ArrayList;
import java.util.List;

public class DataSektoralAdapter extends RecyclerView.Adapter<DataSektoralAdapter.DataSektoralViewHolder> {

    private List<DataSektoral> dataSektoralList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onLihatClicked(DataSektoral data);
        void onUnduhClicked(DataSektoral data);
    }

    public DataSektoralAdapter(List<DataSektoral> dataSektoralList, OnItemClickListener listener) {
        this.dataSektoralList = dataSektoralList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DataSektoralViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDataSektoralBinding binding = ItemDataSektoralBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DataSektoralViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DataSektoralViewHolder holder, int position) {
        holder.bind(dataSektoralList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return dataSektoralList.size();
    }

    public void filterList(ArrayList<DataSektoral> filteredList) {
        this.dataSektoralList = filteredList;
        notifyDataSetChanged();
    }

    class DataSektoralViewHolder extends RecyclerView.ViewHolder {
        private final ItemDataSektoralBinding binding;

        public DataSektoralViewHolder(ItemDataSektoralBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final DataSektoral data, final OnItemClickListener listener) {
            binding.tvJudulTabel.setText(data.getJudulTabel());
            binding.tvSumberInstansi.setText(data.getSumberInstansi());
            binding.btnLihat.setOnClickListener(v -> listener.onLihatClicked(data));
            binding.btnUnduh.setOnClickListener(v -> listener.onUnduhClicked(data));
        }
    }
}
