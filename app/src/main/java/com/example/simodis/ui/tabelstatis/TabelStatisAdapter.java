package com.example.simodis.ui.tabelstatis;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.simodis.data.model.TabelStatis;
import com.example.simodis.databinding.ItemTabelStatisBinding;
import java.util.ArrayList;
import java.util.List;

public class TabelStatisAdapter extends RecyclerView.Adapter<TabelStatisAdapter.ViewHolder> {

    private List<TabelStatis> listData;
    private final OnItemClickListener listener;

    /**
     * Interface untuk menangani klik pada tombol di dalam item.
     * Ini akan diimplementasikan oleh Fragment.
     */
    public interface OnItemClickListener {
        void onUnduhClicked(TabelStatis data);
    }

    public TabelStatisAdapter(List<TabelStatis> listData, OnItemClickListener listener) {
        this.listData = listData;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTabelStatisBinding binding = ItemTabelStatisBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listData.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    /**
     * Metode untuk memperbarui daftar di adapter dengan hasil filter.
     * @param filteredList Daftar hasil filter yang baru.
     */
    public void filterList(ArrayList<TabelStatis> filteredList) {
        this.listData = filteredList;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTabelStatisBinding binding;

        public ViewHolder(ItemTabelStatisBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final TabelStatis data, final OnItemClickListener listener) {
            binding.tvJudulTabel.setText(data.getTitle());
            String subjekInfo = data.getSubject() + " • " + data.getLastUpdate();
            binding.tvSubjekInfo.setText(subjekInfo);
            binding.btnUnduh.setOnClickListener(v -> listener.onUnduhClicked(data));
        }
    }
}
