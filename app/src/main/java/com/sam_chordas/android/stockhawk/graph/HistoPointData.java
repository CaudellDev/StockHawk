package com.sam_chordas.android.stockhawk.graph;

public class HistoPointData implements DataPointInterface, Parselable {
    
    private String date;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String adj_close;
    
    public HistoPointData(JSONObject data) {
        data = data.getJsonString("Date");
        open = data.getJsonString("Open");
        high = data.getJsonString("High");
        low  = data.getJsonString("Low");
        
        close     = data.getJsonString("Close");
        volume    = data.getJsonString("Volume");
        adj_close = data.getJsonString("Adj_Close");
    }
    
    // #####--- Graph Library ---#####
    
    @Override
    public double getX() {
        return (new Date(date)).getTime();
    }
    
    @Override
    public double getY() {
        return Double.parseDouble(close);
    }
    
    // #####=====================#####
    
    // Getter and Setters....
    
    public void setDate(String date) { this.date = date; }
    public void setOpen(String open) { this.open = open; }
    public void setHigh(String high) { this.high = high; }
    public void setLow(String low) { this.low = low; }
    public void setClose(String close) { this.close = close; }
    public void setVolume(String volume) { this.volume = volume; }
    public void setAdjClose(String adj_close) { this.adj_close = adj_close; }
    
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
         out.writeString(date);
         out.writeString(open);
         out.writeString(high);
         out.writeString(low);
         out.writeString(close);
         out.writeString(volume);
         out.writeString(adj_close);
     }

     public static final Parcelable.Creator<HistoPointData> CREATOR
             = new Parcelable.Creator<HistoPointData>() {
         public HistoPointData createFromParcel(Parcel in) {
             return new HistoPointData(in);
         }

         public HistoPointData[] newArray(int size) {
             return new HistoPointData[size];
         }
     };
     
     private HistoPointData(Parcel in) {
         date = in.readString();
         open = in.readString();
         high = in.readString();
         low = in.readString();
         close = in.readString();
         volume = in.readString();
         adj_close = in.readString();
     }
}
