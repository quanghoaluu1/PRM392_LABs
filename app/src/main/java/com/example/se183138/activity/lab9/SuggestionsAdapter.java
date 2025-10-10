package com.example.se183138.activity.lab9;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se183138.R;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
    
    private List<PlacePrediction> predictions = new ArrayList<>();
    private OnSuggestionClickListener listener;
    
    public interface OnSuggestionClickListener {
        void onSuggestionClick(PlacePrediction prediction);
    }
    
    public SuggestionsAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }
    
    public void updateSuggestions(List<PlacePrediction> newPredictions) {
        this.predictions = newPredictions;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggestion, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlacePrediction prediction = predictions.get(position);
        holder.tvMainText.setText(prediction.getMainText());
        holder.tvSecondaryText.setText(prediction.getSecondaryText());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(prediction);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return predictions.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMainText;
        TextView tvSecondaryText;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvMainText = itemView.findViewById(R.id.tvMainText);
            tvSecondaryText = itemView.findViewById(R.id.tvSecondaryText);
        }
    }
}

