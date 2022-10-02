package com.example.sc2006_canpark_clientapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;

public class SearchLocationResultAdapter extends RecyclerView.Adapter<SearchLocationResultAdapter.ViewHolder> {

    @NonNull
    private List<AutocompletePrediction> result = new ArrayList<>();
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_location_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AutocompletePrediction p = this.result.get(position);
        holder.getPlace().setText(p.getPrimaryText(STYLE_BOLD));
        holder.getAddress().setText(p.getSecondaryText(STYLE_BOLD));
    }

    @Override
    public int getItemCount()
    {
        return result.size();
    }

    public void SetResult(List<AutocompletePrediction > result)
    {
        this.result = result;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView tBPlaceName;
        private final TextView tBAddress;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            tBPlaceName = (TextView) view.findViewById(R.id.tBPlaceName);
            tBAddress = (TextView) view.findViewById(R.id.tBAddress);
        }
        public TextView getPlace() {
            return tBPlaceName;
        }
        public TextView getAddress() {
            return tBAddress;
        }

        @Override
        public void onClick(View view) {
            Intent c = new Intent(view.getContext(), CarparkActivity.class);
            startActivity(view.getContext(),c, null);
            Toast.makeText(view.getContext(), "position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
        }
    }


}
