package com.example.simodis.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.simodis.R;
import com.example.simodis.data.model.IndikatorStrategis;
import com.example.simodis.databinding.ItemIndikatorCardBinding;
import java.util.ArrayList;
import java.util.List;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;

public class IndikatorAdapter extends RecyclerView.Adapter<IndikatorAdapter.IndikatorViewHolder> {

    private final Context context;
    private List<IndikatorStrategis> indikatorList;
    private final OnItemClickListener listener; // Listener untuk menangani klik

    /**
     * Interface untuk mengirimkan event klik dari adapter ke Fragment.
     */
    public interface OnItemClickListener {
        void onItemClicked(int indikatorId);
    }

    /**
     * Constructor utama yang akan kita gunakan.
     * @param context Konteks dari Fragment.
     * @param indikatorList Daftar data yang akan ditampilkan.
     * @param listener Implementasi dari interface OnItemClickListener (biasanya Fragment itu sendiri).
     */
    public IndikatorAdapter(Context context, List<IndikatorStrategis> indikatorList, OnItemClickListener listener) {
        this.context = context;
        this.indikatorList = indikatorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IndikatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIndikatorCardBinding binding = ItemIndikatorCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new IndikatorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IndikatorViewHolder holder, int position) {
        IndikatorStrategis indikator = indikatorList.get(position);
        holder.bind(indikator, listener);
    }

    @Override
    public int getItemCount() {
        return indikatorList.size();
    }

    public void filterList(ArrayList<IndikatorStrategis> filteredList) {
        this.indikatorList = filteredList;
        notifyDataSetChanged();
    }

    class IndikatorViewHolder extends RecyclerView.ViewHolder {
        private final ItemIndikatorCardBinding binding;

        public IndikatorViewHolder(ItemIndikatorCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Metode bind sekarang menerima listener sebagai parameter.
         * @param indikator Data untuk item saat ini.
         * @param listener Listener untuk menangani klik.
         */
        public void bind(final IndikatorStrategis indikator, final OnItemClickListener listener) {
            binding.tvNamaIndikator.setText(indikator.getNamaIndikator());
            binding.tvNilai.setText(indikator.getNilai());
            binding.tvSatuan.setText(indikator.getSatuan());
            binding.tvTahun.setText(String.valueOf(indikator.getTahun()));

            int imageResId = context.getResources().getIdentifier(
                    indikator.getImageName(), "drawable", context.getPackageName());

            Glide.with(context)
                    .load(imageResId)
                    .into(binding.ivIlustrasi);

            itemView.setOnClickListener(v -> {
                listener.onItemClicked(indikator.getIdIndikator());
            });

            String theme = indikator.getColorTheme();
            int backgroundResId;
            int textColorResId;

            binding.getRoot().setCardBackgroundColor(context.getResources().getColor(android.R.color.white, null));

            if (theme != null) {
                switch (theme) {
                    case "blue":
                        backgroundResId = R.drawable.gradient_bg_blue;
                        textColorResId = R.color.blue;
                        break;
                    case "green":
                        backgroundResId = R.drawable.gradient_bg_green;
                        textColorResId = R.color.green;
                        break;
                    case "orange":
                        backgroundResId = R.drawable.gradient_bg_orange;
                        textColorResId = R.color.orange;
                        break;
                    default:
                        backgroundResId = android.R.color.transparent;
                        textColorResId = R.color.colorPrimary;
                        break;
                }
            } else {
                backgroundResId = android.R.color.transparent;
                textColorResId = R.color.colorPrimary;
            }

            binding.getRoot().findViewById(R.id.card_content_layout).setBackgroundResource(backgroundResId);

            int color = ContextCompat.getColor(context, textColorResId);
            binding.tvNilai.setTextColor(color);
            binding.tvTahun.setTextColor(color);
        }
    }
}