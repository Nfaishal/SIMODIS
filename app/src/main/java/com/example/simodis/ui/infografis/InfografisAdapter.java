package com.example.simodis.ui.infografis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.simodis.R;
import com.example.simodis.data.model.Infografis;
import com.example.simodis.databinding.ItemInfografisBinding;
import java.util.List;

public class InfografisAdapter extends RecyclerView.Adapter<InfografisAdapter.InfografisViewHolder> {

    private final Context context;
    private List<Infografis> infografisList;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClicked(Infografis infografis);
    }

    public InfografisAdapter(Context context, List<Infografis> infografisList, OnImageClickListener listener) {
        this.context = context;
        this.infografisList = infografisList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InfografisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInfografisBinding binding = ItemInfografisBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new InfografisViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull InfografisViewHolder holder, int position) {
        Infografis infografis = infografisList.get(position);
        holder.bind(infografis, listener);
    }

    @Override
    public int getItemCount() {
        return infografisList.size();
    }

    public void updateList(List<Infografis> newList) {
        this.infografisList = newList;
        notifyDataSetChanged();
    }

    class InfografisViewHolder extends RecyclerView.ViewHolder {
        private final ItemInfografisBinding binding;

        public InfografisViewHolder(ItemInfografisBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Infografis infografis, final OnImageClickListener listener) {
            Glide.with(context)
                    .load(infografis.getImageUrl())
                    .placeholder(R.color.grey)
                    .into(binding.ivInfografis);

            itemView.setOnClickListener(v -> listener.onImageClicked(infografis));
        }
    }
}
