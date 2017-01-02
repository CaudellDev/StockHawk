package com.sam_chordas.android.stockhawk.graph;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jjoe64.graphview.series.DataPointInterface;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoPointData implements DataPointInterface, Parcelable {

    private static final String LOG_TAG = HistoPointData.class.getSimpleName();

    private String symbol;
    private String date;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String adj_close;
    
    public HistoPointData(JSONObject data) {
        Utils.log5(LOG_TAG, "HistoPointData String: " + data);
        Log.v(LOG_TAG, "Json is null: " + (data == null));

        try {
            symbol = data.getString("Symbol");
            date = data.getString("Date");
            open = data.getString("Open");
            high = data.getString("High");
            low = data.getString("Low");

            close = data.getString("Close");
            volume = data.getString("Volume");
            adj_close = data.getString("Adj_Close");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    // #####--- Graph Library ---#####
    
    @Override
    public double getX() {
//        Log.v(LOG_TAG, "getX: date - " + date);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        double x;

        try {
            x = format.parse(date).getTime() * 100;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

//        Utils.log5(LOG_TAG, "Date and time: " + date + ", " + x);
        return x;
    }
    
    @Override
    public double getY() {
        return Double.parseDouble(close);
    }
    
    // #####=====================#####
    
    // Getter and Setters....

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setDate(String date) { this.date = date; }
    public void setOpen(String open) { this.open = open; }
    public void setHigh(String high) { this.high = high; }
    public void setLow(String low) { this.low = low; }
    public void setClose(String close) { this.close = close; }
    public void setVolume(String volume) { this.volume = volume; }
    public void setAdjClose(String adj_close) { this.adj_close = adj_close; }

    public String getSymbol() { return symbol; }
    public String getDate() { return date; }
    public String getOpen() { return open; }
    public String getHigh() { return high; }
    public String getLow() { return low; }
    public String getClose() { return close; }
    public String getVolume() { return volume; }
    public String getAdjClose() { return adj_close; }
    
    // #####--- Parcel Stuff ---#####

    public int describeContents() {
         return 0;
     }

     public void writeToParcel(Parcel out, int flags) {
         out.writeString(symbol);
         out.writeString(date);
         out.writeString(open);
         out.writeString(high);
         out.writeString(low);
         out.writeString(close);
         out.writeString(volume);
         out.writeString(adj_close);

         Log.v(LOG_TAG, "writeToParcel, getX: " + getX());
     }

     public static final Parcelable.Creator<HistoPointData> CREATOR = new Parcelable.Creator<HistoPointData>() {
         public HistoPointData createFromParcel(Parcel in) {
             return new HistoPointData(in);
         }

         public HistoPointData[] newArray(int size) {
             return new HistoPointData[size];
         }
     };

     private HistoPointData(Parcel in) {
         symbol = in.readString();
         date = in.readString();
         open = in.readString();
         high = in.readString();
         low = in.readString();
         close = in.readString();
         volume = in.readString();
         adj_close = in.readString();

         Log.v(LOG_TAG, "From parcel, getX: " + getX());
     }
}
