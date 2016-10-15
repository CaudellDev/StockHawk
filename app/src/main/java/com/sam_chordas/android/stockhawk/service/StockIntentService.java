package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.TaskParams;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StockIntentService extends IntentService {

    private static final String LOG_TAG = StockIntentService.class.getSimpleName();

    public static final String DETAIL_SYMBOL = "detail_symbol";

    public StockIntentService(){
    super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
    super(name);
    }

    @Override protected void onHandleIntent(Intent intent) {
        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");
        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();

        if (intent.getStringExtra("tag").equals("add")) {
            args.putString("symbol", intent.getStringExtra("symbol"));
        } else if (intent.getStringExtra("tag").equals("detail")) {
            String data = intent.getCharSequenceExtra(DETAIL_SYMBOL).toString();
            Log.i(LOG_TAG, "onHandleIntent -- Detail Symbol: " + data);

            // Remove once data is actually getting passed and log is actually printing.
            return;
        }
        // We can call OnRunTask from the intent service to force it to run immediately instead of
        // scheduling a task.
        stockTaskService.onRunTask(new TaskParams(intent.getStringExtra("tag"), args));
    }
}
