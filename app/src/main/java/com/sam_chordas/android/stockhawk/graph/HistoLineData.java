package com.sam_chordas.android.stockhawk.graph;

/**
 * Created by Tyler on 12/19/2016.
 */

public class HistoLineData extends LineGraphSeries<HistoPointData> implements Parcelable {
  
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    // Add the array somehow....
    out.writeInt(null);
  }

  public static final Parcelable.Creator<MyParcelable> CREATOR
         = new Parcelable.Creator<MyParcelable>() {
     public MyParcelable createFromParcel(Parcel in) {
         return new MyParcelable(in);
     }

     public MyParcelable[] newArray(int size) {
         return new MyParcelable[size];
     }
  };

  private MyParcelable(Parcel in) {
    // Get an array, or loop and add each item
//      mData = in.readInt();
  }
  
  protected class HistoPointData implements DataPointInterface {
    private double x;
    private double y;
    private String date;
    private String value;
    
    public void setDate(String date)   { this.date = date; }
    public void setValue(String value) { this.value = value; }
    public void setX(double x) { this.x = x; }
    
    @Override
    public double getX() {
      // Do stuff to date....
      return 10;
    }
    
    @Override
    public double getY() {
      // Do stuff to value...
      return 2;
    }
  }
}
