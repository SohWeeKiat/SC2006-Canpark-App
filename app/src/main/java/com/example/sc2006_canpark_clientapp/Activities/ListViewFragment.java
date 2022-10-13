package com.example.sc2006_canpark_clientapp.Activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sc2006_canpark_clientapp.Adapters.CarparkListAdapter;
import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.OnItemClickListener;
import com.example.sc2006_canpark_clientapp.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListViewFragment extends Fragment {
    private RecyclerView lVCarparkList;
    private CarparkListAdapter adapter = new CarparkListAdapter(new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            CarparkActivity ca = (CarparkActivity)getActivity();
            ca.OnSelection(position);
        }
    });

    public ListViewFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ListViewFragment newInstance() {
        ListViewFragment fragment = new ListViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list_view, container, false);
        this.lVCarparkList = (RecyclerView) v.findViewById(R.id.LVCarparkList);
        this.lVCarparkList.setLayoutManager(new LinearLayoutManager(getContext()));
        this.lVCarparkList.setAdapter(this.adapter);
        return v;
    }

    public void SetCarparks(ArrayList<Carpark> data)
    {
        this.adapter.setCarparks(data);
        this.adapter.notifyDataSetChanged();
    }
}