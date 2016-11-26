package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;

/**
 * Created by Tyler on 10/24/2016.
 */

public class StocksDetailActivity extends Activity {
  
  public static final String INTENT_SYMBOL = "intent_symbol";
  
  @Override
  protected void onCreate(Bundle onSavedInstanceState) {
    super.onCreate(onSavedInstanceState);
    setContentView(R.layout.activity_temp_details);
    
    Intent intent = getIntent();
    String symbol = intent.getStringExtra(INTENT_SYMBOL);
    
    TextView symbolDisplay = (TextView) findViewById(R.id.details_debug_textview);
    symbolDisplay.append(symbol);
    
    GraphView testGraph = (GraphView) findViewById(R.id.test_point_graph);
    
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
            new DataPoint(0, 5),
            new DataPoint(1, 1),
            new DataPoint(2, 1),
            new DataPoint(3, 9),
            new DataPoint(4, 3),
            new DataPoint(5, 5)
    });
    
    testGraph.addSeries(series);
    
  }
}
