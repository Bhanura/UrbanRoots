package com.example.urbanroots;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {

    private List<Crop> cropList;
    private boolean isAdmin;
    private OnCropActionListener listener;

    public CropAdapter(List<Crop> cropList) {
        this(cropList, false, null);
    }

    public CropAdapter(List<Crop> cropList, boolean isAdmin, OnCropActionListener listener) {
        this.cropList = cropList;
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = cropList.get(position);
        holder.cropNameTextView.setText(crop.getCropName());
        holder.priceTextView.setText(String.format("$%.2f", crop.getPrice()));
        holder.descriptionTextView.setText(crop.getDescription());
        holder.statusTextView.setText(crop.getStatus());

        if (isAdmin) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(crop);
                }
            });
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(crop);
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView, priceTextView, descriptionTextView, statusTextView;
        Button editButton, deleteButton;

        CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface OnCropActionListener {
        void onEdit(Crop crop);
        void onDelete(Crop crop);
    }
}