package com.example.urbanroots;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CareTaskAdapter extends RecyclerView.Adapter<CareTaskAdapter.CareTaskViewHolder> {

    private List<CareTask> careTaskList;
    private OnCareTaskClickListener listener;

    public interface OnCareTaskClickListener {
        void onMarkAsDone(CareTask careTask);
    }

    public CareTaskAdapter(List<CareTask> careTaskList, OnCareTaskClickListener listener) {
        this.careTaskList = careTaskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CareTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_care_task, parent, false);
        return new CareTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CareTaskViewHolder holder, int position) {
        CareTask careTask = careTaskList.get(position);
        holder.taskTypeTextView.setText(careTask.getType());
        holder.statusTextView.setText(careTask.getCurrentStatus());
        holder.dueDateTextView.setText(careTask.getDueDate());

        // Color-code status
        switch (careTask.getCurrentStatus()) {
            case "Done":
                holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Pending":
                holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                break;
            default:
                holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        // Mark as done button
        holder.markAsDoneButton.setOnClickListener(v -> listener.onMarkAsDone(careTask));
    }

    @Override
    public int getItemCount() {
        return careTaskList.size();
    }

    static class CareTaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTypeTextView;
        TextView statusTextView;
        TextView dueDateTextView;
        com.google.android.material.button.MaterialButton markAsDoneButton;

        CareTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTypeTextView = itemView.findViewById(R.id.taskTypeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            dueDateTextView = itemView.findViewById(R.id.dueDateTextView);
            markAsDoneButton = itemView.findViewById(R.id.markAsDoneButton);
        }
    }
}