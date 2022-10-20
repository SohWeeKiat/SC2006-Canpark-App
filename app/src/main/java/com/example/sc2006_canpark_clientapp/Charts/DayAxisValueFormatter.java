package com.example.sc2006_canpark_clientapp.Charts;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;


public class DayAxisValueFormatter extends ValueFormatter {
    private final String[] days = new String[]{"Mo", "Tu", "Wed", "Th", "Fr", "Sa", "Su"};

    @Override
    public String getBarLabel(BarEntry barEntry)
    {
        return "Sunday";
    }
}
