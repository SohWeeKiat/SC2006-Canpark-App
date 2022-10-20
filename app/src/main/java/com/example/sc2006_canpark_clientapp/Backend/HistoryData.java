package com.example.sc2006_canpark_clientapp.Backend;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HistoryData implements Serializable {
    private List<List<Integer>> data;
    private List<List<String>> hours;

    public List<List<Integer>> getData() {
        return data;
    }

    public List<List<String>> getHours() {
        return hours;
    }

    public ArrayList<Entry> GenerateEntryList(int days)
    {
        ArrayList<Entry> list = new ArrayList<>();
        if (days < 0 || days > 7 || this.data == null || days > this.data.size())
            return list;
        List<Integer> day_data = this.data.get(days);
        for(int hr = 0;hr < day_data.size();hr++){
            list.add(new Entry(hr, day_data.get(hr)));
        }
        return list;
    }

    public ArrayList<BarEntry> GenerateBarEntryList(int days)
    {
        ArrayList<BarEntry> list = new ArrayList<>();
        if (days < 0 || days > 7 || this.data == null || days > this.data.size())
            return list;
        List<Integer> day_data = this.data.get(days);
        for(int hr = 0;hr < day_data.size();hr++){
            list.add(new BarEntry(hr, day_data.get(hr)));
        }
        return list;
    }
}
