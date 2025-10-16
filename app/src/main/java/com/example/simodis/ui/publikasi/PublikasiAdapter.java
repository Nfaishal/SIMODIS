package com.example.simodis.ui.publikasi; // Pastikan package ini sesuai dengan proyek Anda

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.simodis.R; // Pastikan R diimpor dengan benar
import com.example.simodis.data.model.Publikasi;
import com.example.simodis.databinding.ItemPublikasiBinding;
import java.util.ArrayList;
import java.util.List;

public class PublikasiAdapter extends RecyclerView.Adapter<PublikasiAdapter.PublikasiViewHolder> {

    private final Context context;
    private List<Publikasi> publikasiList;
    private final OnItemClickListener listener;

    /**
     * Interface untuk menangani klik pada tombol di dalam item.
     * Ini akan diimplementasikan oleh Fragment.
     */
    public interface OnItemClickListener {
        void onLihatClicked(Publikasi publikasi);
        void onUnduhClicked(Publikasi publikasi);
        void onItemRootClicked(Publikasi publikasi);
    }

    public PublikasiAdapter(Context context, List<Publikasi> publikasiList, OnItemClickListener listener) {
        this.context = context;
        this.publikasiList = publikasiList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PublikasiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPublikasiBinding binding = ItemPublikasiBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PublikasiViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PublikasiViewHolder holder, int position) {
        Publikasi publikasi = publikasiList.get(position);
        holder.bind(publikasi, listener);
    }

    @Override
    public int getItemCount() {
        return publikasiList.size();
    }

    public void updateData(ArrayList<Publikasi> newList) {
        // Kosongkan list yang ada saat ini
        this.publikasiList.clear();
        // Tambahkan semua data baru ke dalam list
        this.publikasiList.addAll(newList);
        // Beri tahu RecyclerView bahwa datanya telah berubah dan perlu di-refresh
        notifyDataSetChanged();
    }

    class PublikasiViewHolder extends RecyclerView.ViewHolder {
        private final ItemPublikasiBinding binding;

        public PublikasiViewHolder(ItemPublikasiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Publikasi publikasi, final OnItemClickListener listener) {
            // Mengisi data ke dalam view
            binding.tvTitle.setText(publikasi.getTitle());
            binding.tvReleaseDate.setText(publikasi.getReleaseDate());

            // Memuat gambar sampul menggunakan Glide
            Glide.with(context)
                    .load(publikasi.getCoverUrl()) // URL gambar dari API
                    .placeholder(R.color.grey) // Gambar sementara saat loading
                    .error(R.drawable.ic_broken_image) // Gambar jika gagal memuat
                    .into(binding.ivCover);

            itemView.setOnClickListener(v -> listener.onItemRootClicked(publikasi));
            binding.btnLihat.setOnClickListener(v -> listener.onLihatClicked(publikasi));
            binding.btnUnduh.setOnClickListener(v -> listener.onUnduhClicked(publikasi));
        }
    }
}