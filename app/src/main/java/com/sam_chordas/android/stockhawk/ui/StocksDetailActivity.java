package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.graph.HistoLineData;
import com.sam_chordas.android.stockhawk.graph.HistoPointData;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

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

        TextView symbolView = (TextView) findViewById(R.id.detail_stock);
        symbolView.setText(stock);

        TextView valueView = (TextView) findViewById(R.id.detail_value);
        valueView.setText(close);

        GraphView testGraph = (GraphView) findViewById(R.id.detail_point_graph);

        testGraph.addSeries(histoLineData);

        testGraph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        testGraph.getGridLabelRenderer().setNumHorizontalLabels(3);

        testGraph.getViewport().setMinX(histoLineData.getLowestValueX());
        testGraph.getViewport().setMaxX(histoLineData.getHighestValueX());
        testGraph.getViewport().setXAxisBoundsManual(true);
        testGraph.getGridLabelRenderer().setHumanRounding(false);
    }

    public void noDataBackup() {
        // Do something? Snackbar?
        Snackbar.make(findViewById(R.id.detail_coordlayout), "No historical data available!", Snackbar.LENGTH_INDEFINITE).show();
    }
}
