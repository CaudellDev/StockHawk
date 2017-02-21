package com.reddev_udacity.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.reddev_udacity.android.stockhawk.R;
import com.reddev_udacity.android.stockhawk.graph.HistoLineData;
import com.reddev_udacity.android.stockhawk.graph.HistoPointData;
import com.reddev_udacity.android.stockhawk.service.StockIntentService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by Tyler on 10/24/2016.
 */

public class StocksDetailActivity extends AppCompatActivity {

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
        present = dataIterator.next();

        // I need to save this info for later, too.
        while (dataIterator.hasNext()) {
            HistoPointData temp = dataIterator.next();
            dailyData.add(temp);
        }

        Collections.reverse(dailyData);

        String stock = present.getSymbol();
        String close = "$" + present.getClose();
        String volume = present.getVolume();

        TextView symbolView = (TextView) findViewById(R.id.detail_stock);
        symbolView.setText(stock);

        TextView valueView = (TextView) findViewById(R.id.detail_value);
        valueView.setText(close);

        TextView volumeView = (TextView) findViewById(R.id.detail_volume_value);
        volumeView.setText(volume);

        GraphView testGraph = (GraphView) findViewById(R.id.detail_point_graph);

        histoLineData.setColor(Color.BLACK);
        testGraph.addSeries(histoLineData);

        testGraph.getGridLabelRenderer().setNumHorizontalLabels(4);
        testGraph.getGridLabelRenderer().setNumVerticalLabels(6);
        testGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).format(value);
                } else {
                    value = Math.floor(value * 100) / 100; // Round 2 places.
                    return "$" + super.formatLabel(value, isValueX);
                }
            }
        });

        testGraph.getViewport().setMinX(histoLineData.getLowestValueX());
        testGraph.getViewport().setMaxX(histoLineData.getHighestValueX());
        testGraph.getViewport().setMaxY(histoLineData.getHighestValueY());
        testGraph.getViewport().setMinY(0);
        testGraph.getViewport().setXAxisBoundsManual(true);
        testGraph.getGridLabelRenderer().setHumanRounding(false);

    }

    public void noDataBackup() {
        // Do something? Snackbar?
        Snackbar.make(findViewById(R.id.detail_coordlayout), "No historical data available!", Snackbar.LENGTH_INDEFINITE).show();
    }
}
