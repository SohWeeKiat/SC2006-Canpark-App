package com.example.sc2006_canpark_clientapp.Activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sc2006_canpark_clientapp.Adapters.CarparkListAdapter;
import com.example.sc2006_canpark_clientapp.Backend.Carpark;
import com.example.sc2006_canpark_clientapp.Backend.CarparkSortType;
import com.example.sc2006_canpark_clientapp.Utils.OnItemClickListener;
import com.example.sc2006_canpark_clientapp.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListViewFragment extends Fragment {
    private RecyclerView lVCarparkList;
    private Spinner cmBSortSelection;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        this.cmBSortSelection = v.findViewById(R.id.cmbSortSelection);
        this.cmBSortSelection.setAdapter(adapter);
        this.cmBSortSelection.setOnItemSelectedListener(OnSortSelection);

        adapter.add("Distance");
        adapter.add("Availability");
        return v;
    }

    public void SetCarparks(ArrayList<Carpark> data)
    {
        this.adapter.setCarparks(data);
        this.adapter.notifyDataSetChanged();
    }

    private final AdapterView.OnItemSelectedListener OnSortSelection = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            CarparkActivity cp = (CarparkActivity) getActivity();
            cp.SortCarparks(CarparkSortType.values()[i]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}