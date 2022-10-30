package com.example.sc2006_canpark_clientapp.Adapters;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sc2006_canpark_clientapp.R;
import com.example.sc2006_canpark_clientapp.Utils.OnItemClickListener;

public class CarparkListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView tBPlaceName;
    private final TextView TVDistance;
    private final TextView TVLots;

    private OnItemClickListener mOnItemClickListener;

    public CarparkListViewHolder(View view, OnItemClickListener mOnItemClickListener) {
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