package com.sam_chordas.android.stockhawk.graph;

public class HistoDataPoint implements DataPointInterface {
    
    private String date;
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String adj_close;
    
    public HistoDataPoint(JSONObject data) {
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
        return Double.getValue(close);
    }
    
    // #####=====================#####
    
    // Getter and Setters....
}
