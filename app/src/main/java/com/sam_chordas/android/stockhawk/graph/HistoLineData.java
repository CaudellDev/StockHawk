package com.sam_chordas.android.stockhawk.graph;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Tyler on 12/19/2016.
 */

public class HistoLineData extends LineGraphSeries<DataPoint> implements Parcelable {

    private static final String LOG_TAG = HistoLineData.class.getSimpleName();

    public static final String HISTO_TAG = "histo_data";

    public HistoLineData(Context context, String json) {
        ArrayList<JSONObject> jsonObjects = Utils.parseHistoricalJson(context, json);
        // Convert to DataPoints and add to the LineData.......
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        // Add the array somehow....
        Iterator<DataPoint> values = getValues(getLowestValueX(), getHighestValueX());
        while (values.hasNext()) {
            DataPoint val = values.next();
            out.writeValue(val);
        }
    }

    public static final Parcelable.Creator<HistoLineData> CREATOR
            = new Parcelable.Creator<HistoLineData>() {
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
        ArrayList<DataPoint> values = new ArrayList<>();
        for (int i = 0; i < in.dataSize(); i++) {
            DataPoint val = (DataPoint) in.readValue(DataPoint.class.getClassLoader());

            Log.v(LOG_TAG, "Rebuilding HistoLineData Parcel. DataPoint " + i + ", " + val);

            values.add(val);
        }
    }
}
