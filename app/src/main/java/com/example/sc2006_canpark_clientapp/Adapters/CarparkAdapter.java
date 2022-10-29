package com.example.sc2006_canpark_clientapp.Adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sc2006_canpark_clientapp.Activities.ListViewFragment;
import com.example.sc2006_canpark_clientapp.Activities.MapViewFragment;

import java.util.ArrayList;

public class CarparkAdapter extends FragmentStateAdapter {

    private ArrayList<Fragment> arrayList = new ArrayList<>();

    public CarparkAdapter(FragmentActivity fa)
    {
        super(fa);
        arrayList.add(MapViewFragment.newInstance());
        arrayList.add(ListViewFragment.newInstance());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
            case 1:
                return arrayList.get(position);
        }
        return null;
    }

    public Fragment GetItem(int index)
    {
        return this.arrayList.get(index);
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }
}
