package com.example.sc2006_canpark_clientapp.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.Utils.Config;
import com.example.sc2006_canpark_clientapp.Utils.OnItemClickListener;
import com.example.sc2006_canpark_clientapp.R;

import java.util.ArrayList;

public class CarparkListAdapter extends RecyclerView.Adapter<CarparkListAdapter.ViewHolder> {
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<Carpark> Carparks = new ArrayList<>();

    public CarparkListAdapter(OnItemClickListener onItemClickListener)
    {
        this.mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.carpark_result_item, parent, false);
        return new CarparkListAdapter.ViewHolder(view, this.mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Carpark c = this.Carparks.get(position);
        holder.getPlace().setText(c.getAddress());
        holder.getDist().setText(String.format("%.2f KM", c.getDist()));
        if(c.getTotal_lots() != 0) {
            double percentage = (double) c.getLots_available() / c.getTotal_lots();
            holder.getLots().setText(String.format("%d / %d", c.getLots_available(), c.getTotal_lots()));
            if (percentage <= Config.LowerPercentage) {
                holder.getLots().setTextColor(Color.RED);
            } else if (percentage > Config.HigherPercentage) {
                holder.getLots().setTextColor(Color.GREEN);
            } else {
                holder.getLots().setTextColor(Color.rgb(255, 153, 0));
            }
        }else{
            holder.getLots().setTextColor(Color.RED);
            holder.getLots().setText("Not Available");
        }
    }

    @Override
    public int getItemCount() {
        return this.Carparks.size();
    }

    public void setCarparks(ArrayList<Carpark> carparks) {
        this.Carparks = carparks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tBPlaceName;
        private final TextView TVDistance;
        private final TextView TVLots;

        private OnItemClickListener mOnItemClickListener;

        public ViewHolder(View view, OnItemClickListener mOnItemClickListener) {
            super(view);
            this.mOnItemClickListener = mOnItemClickListener;
            view.setOnClickListener(this);
            tBPlaceName = (TextView) view.findViewById(R.id.tBPlaceName);
            TVDistance = (TextView) view.findViewById(R.id.TVDistance);
            TVLots = (TextView) view.findViewById(R.id.TVLots);
        }

        public TextView getPlace() {
            return tBPlaceName;
        }
        public TextView getDist() {
            return TVDistance;
        }
        public TextView getLots() {
            return TVLots;
        }

        @Override
        public void onClick(View view) {
            if (this.mOnItemClickListener != null)
                this.mOnItemClickListener.onItemClick(view, getLayoutPosition());
        }
    }
}
