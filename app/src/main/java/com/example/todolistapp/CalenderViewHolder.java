package com.example.todolistapp;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalenderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public final TextView dayOfMonth;
    private final CalenderAdapter.OnItemListener OntimeListener;
    public CalenderViewHolder(@NonNull View itemView, CalenderAdapter.OnItemListener onItemListener) {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.celldaytext);
        this.OntimeListener = onItemListener;
    }

    @Override
    public void onClick(View v) {
        OntimeListener.OnItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
