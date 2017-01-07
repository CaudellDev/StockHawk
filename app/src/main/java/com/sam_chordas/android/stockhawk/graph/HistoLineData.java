package com.sam_chordas.android.stockhawk.graph;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Tyler on 12/19/2016.
 */

public class HistoLineData extends LineGraphSeries<HistoPointData> implements Parcelable {

    private static final String LOG_TAG = HistoLineData.class.getSimpleName();
    public static final String HISTO_TAG = "histo_data";

    private String symbol;

    public HistoLineData(String json) {
        ArrayList<HistoPointData> dailyData = Utils.parseHistoricalJson(json);
        symbol = dailyData.get(0).getSymbol();

        HistoPointData[] array = new HistoPointData[dailyData.size()];
        array = dailyData.toArray(array);

//        for (HistoPointData day : array) {
//            Log.v(LOG_TAG, "getX: " + day.getX());
//        }

        resetData(array);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(symbol);
        // Add the array somehow....
        Iterator<HistoPointData> values = getValues(getLowestValueX(), getHighestValueX());
        while (values.hasNext()) {
            HistoPointData val = values.next();
            out.writeParcelable(val, flags);
        }
    }

    public static final Parcelable.Creator<HistoLineData> CREATOR = new Parcelable.Creator<HistoLineData>() {
        public HistoLineData createFromParcel(Parcel in) {
            return new HistoLineData(in);
        }

        public HistoLineData[] newArray(int size) {
            return new HistoLineData[size];
        }
    };

    private HistoLineData(Parcel in) {
        // Get an array, or loop and add each item
//      mData = in.readInt();

        symbol = in.readString();

        ArrayList<HistoPointData> values = new ArrayList<>();
        for (int i = 0; i < in.dataSize(); i++) {
            HistoPointData val = in.readParcelable(HistoPointData.class.getClassLoader());

            if (val == null) break; // Parcel has a bunch of null elements for some reason - stop once that's reached.
            Log.v(LOG_TAG, "Rebuilding HistoLineData Parcel. HistoPointData " + i + ", " + val.getX());

            values.add(val);
        }
        
        HistoPointData[] array = new HistoPointData[values.size()];
        array = values.toArray(array);
        resetData(array);
    }
}
