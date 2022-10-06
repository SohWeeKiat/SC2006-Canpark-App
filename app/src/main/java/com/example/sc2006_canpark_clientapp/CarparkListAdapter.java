package com.example.sc2006_canpark_clientapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CarparkListAdapter extends RecyclerView.Adapter<CarparkListAdapter.ViewHolder> {
    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.carpark_result_item, parent, false);
        return new CarparkListAdapter.ViewHolder(view, this.mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tBPlaceName;
        private OnItemClickListener mOnItemClickListener;

        public ViewHolder(View view, OnItemClickListener mOnItemClickListener) {
            super(view);
            this.mOnItemClickListener = mOnItemClickListener;
            view.setOnClickListener(this);
            tBPlaceName = (TextView) view.findViewById(R.id.tBPlaceName);
        }

        public TextView getPlace() {
            return tBPlaceName;
        }

        @Override
        public void onClick(View view) {
            if (this.mOnItemClickListener != null)
                this.mOnItemClickListener.onItemClick(view, getLayoutPosition());
        }
    }
}
