package com.reddev_udacity.android.stockhawk.service;

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
    
    public static final String INTENT_TAG = "tag";
    public static final String INTENT_ADD = "add";
    public static final String INTENT_DETAIL = "detail";
    public static final String INTENT_SYMBOL = "symbol";

    public StockIntentService(){
    super(StockIntentService.class.getName());
    }

    public StockIntentService(String name) {
    super(name);
    }

    @Override protected void onHandleIntent(Intent intent) {
//        Log.d(StockIntentService.class.getSimpleName(), "Stock Intent Service");

        StockTaskService stockTaskService = new StockTaskService(this);
        Bundle args = new Bundle();

        if (intent.getStringExtra(INTENT_TAG).equals(INTENT_ADD)) {
            args.putString(INTENT_SYMBOL, intent.getStringExtra(INTENT_SYMBOL));

        } else if (intent.getStringExtra(INTENT_TAG).equals(INTENT_DETAIL)) {

            String data = intent.getCharSequenceExtra(INTENT_SYMBOL).toString();
//            Log.i(LOG_TAG, "onHandleIntent -- Detail Symbol: " + data);

            args.putString(INTENT_SYMBOL, data);
        }

        stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(INTENT_TAG), args));
    }
}
