package com.reddev_udacity.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.reddev_udacity.android.stockhawk.R;
import com.reddev_udacity.android.stockhawk.graph.HistoLineData;
import com.reddev_udacity.android.stockhawk.graph.HistoPointData;
import com.reddev_udacity.android.stockhawk.service.StockIntentService;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

public class StocksDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = StocksDetailActivity.class.getSimpleName();

    private ArrayList<HistoPointData> dailyData;
    private HistoPointData present;
  
    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        //    String symbol = intent.getStringExtra(StockIntentService.INTENT_SYMBOL);
//        HistoLineData histoLineData = intent.getParcelableExtra(HistoLineData.HISTO_TAG);

        Bundle extra = intent.getBundleExtra(HistoLineData.HISTO_TAG);
        HistoLineData histoLineData = extra.getParcelable(HistoLineData.HISTO_TAG);

        Iterator<HistoPointData> dataIterator = histoLineData.getValues(histoLineData.getLowestValueX(), histoLineData.getHighestValueX());

        if (!dataIterator.hasNext()) {
            noDataBackup();
            return;
        }

        dailyData = new ArrayList<>();

        // I need to save this info for later, too.
        while (dataIterator.hasNext()) {
            HistoPointData temp = dataIterator.next();
            dailyData.add(temp);
        }

        Collections.reverse(dailyData);
        present = dailyData.get(0);

        Log.v(LOG_TAG, "onCreate, present data: " + present);

        String stock = present.getSymbol();
        String close = "$" + present.getClose();
        String volume = present.getVolume();

        TextView symbolLabel = (TextView) findViewById(R.id.detail_stock_label);
        TextView valueLabel = (TextView) findViewById(R.id.detail_value_label);
        TextView volumeLabel = (TextView) findViewById(R.id.detail_volume_label);

        symbolLabel.setContentDescription(getString(R.string.stock_desc, stock));
        valueLabel.setContentDescription(getString(R.string.value_desc, close));
        volumeLabel.setContentDescription(getString(R.string.volume_desc, volume));

        TextView symbolView = (TextView) findViewById(R.id.detail_stock);
        symbolView.setText(stock);
        symbolView.setContentDescription(getString(R.string.stock_desc, stock));

        TextView valueView = (TextView) findViewById(R.id.detail_value);
        valueView.setText(close);
        valueView.setContentDescription(getString(R.string.value_desc, close));

        TextView volumeView = (TextView) findViewById(R.id.detail_volume_value);
        volumeView.setText(volume);
        volumeView.setContentDescription(getString(R.string.volume_desc, volume));

        GraphView stockGraph = (GraphView) findViewById(R.id.detail_point_graph);

        histoLineData.setColor(Color.BLACK);
        histoLineData.setThickness(3);
        histoLineData.setOnDataPointTapListener(new OnDataPointTapListener() {
            // Tap function works with the TalkBack, but you have to click and then double click
            // first for some reason. It'll then get the accurate-ish click data.
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(getApplicationContext(), "Graph Point Selected: " + dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        stockGraph.addSeries(histoLineData);

        stockGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        stockGraph.getGridLabelRenderer().setNumVerticalLabels(6);
        stockGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).format(value);
                } else {
                    Log.v(LOG_TAG, "Label formatter, before round - value: " + value);

                    value = Math.floor(value * 100) / 100; // Round 2 places.

                    Log.v(LOG_TAG, "Label formatter, after round - value: " + value);

                    NumberFormat nf = NumberFormat.getCurrencyInstance();
                    return nf.format(value);
                }
            }
        });


        stockGraph.getViewport().setMinX(histoLineData.getLowestValueX());
        stockGraph.getViewport().setMaxX(histoLineData.getHighestValueX());
        stockGraph.getViewport().setMaxY(histoLineData.getHighestValueY());
        stockGraph.getViewport().setMinY(0);
        stockGraph.getViewport().setXAxisBoundsManual(true);
        stockGraph.getGridLabelRenderer().setHumanRounding(false);

    }

    public void noDataBackup() {
        // Do something? Snackbar?
        Snackbar.make(findViewById(R.id.detail_coordlayout), "No historical data available!", Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
