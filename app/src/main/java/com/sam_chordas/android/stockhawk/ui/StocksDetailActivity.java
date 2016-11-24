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
  }
}
