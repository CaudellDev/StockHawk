package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService {

    private String LOG_TAG = StockTaskService.class.getSimpleName();

    public static final String DETAIL_SYMBOL = "detail_symbol";
    public static final String TAG_EXTRA = "tag";
    public static final String ADD_EXTRA = "add";
    public static final String SYMBOL_EXTRA = "symbol";

    
    private static final String URL_SEC = "https://";
    private static final String URL_INSEC = "http://";
    
    private static final String URL_BASE = "query.yahooapis.com/v1/public/yql?q=";
    
    private static final String URL_SELECT = "select * from ";
    private static final String URL_SYMBOL = " where symbol in ";
    private static final String URL_INIT = "(\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";
    
    private static final String URL_QUOTE = "yahoo.finance.quotes";
    private static final String URL_HISTORY = "yahoo.finance.historicaldata";
    
    private static final String URL_STDATE = " and startDate = ";
    private static final String URL_ENDATE = " and endDate = ";
    
    private static final String URL_FORMAT = "&format=json";
    private static final String URL_DIAG = "&diagnostics=true";
    private static final String URL_ENV = "&env=store://datatables.org/alltableswithkeys";
    private static final String URL_CALLBK = "&callback=";

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;


    public StockTaskService() {}

    public StockTaskService(Context context){
    mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor;
        if (mContext == null) {
            mContext = this;
        }

        StringBuilder urlStringBuilder = new StringBuilder();
        String usedTag = params.getTag();
        String usedApi = "";
        String usedSymbol = "";
        
        // If the URL needs these params, they will be populated
        String usedStDate = "";
        String usedEnDate = "";

        // Combine these because they both need a query cursor?
        if (usedTag.equals("init") || usedTag.equals("periodic")) {
            isUpdate = true;
            usedApi = URL_QUOTE;

            initQueryCursor = mContext.getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[] { "Distinct " + QuoteColumns.SYMBOL }, 
                        null, null, null);

            // If it's empty, initialize it with some basic stocks.
            if (initQueryCursor.getCount() == 0 || initQueryCursor == null) {
                // Init task. Populates DB with quotes for the symbols seen below
                usedSymbol = URL_INIT;

            } else if (initQueryCursor != null) { // Periodic updates?
                DatabaseUtils.dumpCursor(initQueryCursor);
                initQueryCursor.moveToFirst();

                mStoredSymbols.append("("); // Starting parenth

                // Go through all of the Stocks and add them to a StringBuilder instance.
                for (int i = 0; i < initQueryCursor.getCount(); i++) {
                    mStoredSymbols.append("\""+
                        initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"))+"\",");
                        initQueryCursor.moveToNext();
                }

                // Not sure what this does?? Replaces the last character with a ")"?
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")"); // Ending Parenth?

                usedSymbol = mStoredSymbols.toString();
            }
        } else if (usedTag.equals("add")) {
            // TODO: Check for the position param in here? Or was the selected symbol passed into params?
            isUpdate = false;
            usedApi = URL_QUOTE;

            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            usedSymbol = "(\"" + stockInput + "\")";
        } else if (usedTag.equals("detail")) {
            usedApi = URL_HISTORY;
            usedSymbol = "(\"" + params.getExtras().getString(StockIntentService.INTENT_SYMBOL) + "\")";

            // TODO: Get actual dates
            usedStDate = URL_STDATE + "\"" + "2016-11-21" + "\"";
            usedEnDate = URL_ENDATE + "\"" + "2016-11-28" + "\"";
        } else {
            if (usedTag.isEmpty()) usedTag = "<empty>";
            Log.e(LOG_TAG, "This tag, " + usedTag + " was not recognized. Cannot complete the Task Service.");
            return GcmNetworkManager.RESULT_FAILURE;
        }
        
        // Build the URL here, all at once
        urlStringBuilder.append(URL_SEC)                // https
                        .append(URL_BASE)               // yahooapis.com
                        .append(encode(URL_SELECT))     // select *
                        .append(encode(usedApi))        // quote or historical data
                        .append(encode(URL_SYMBOL))     // where symbol in
                        .append(encode(usedSymbol))
                        .append((usedStDate))
                        .append((usedEnDate))
                        .append(URL_FORMAT).append((URL_DIAG)).append((URL_ENV)).append((URL_CALLBK));
        
        Log.i(LOG_TAG, "urlStringBuilder = " + urlStringBuilder.toString());

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();
            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;

                Log.i(LOG_TAG, "URL: " + urlString + "\n\n");
                Log.i(LOG_TAG, "Get Response: " + getResponse);

                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(QuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                                             null, null);
                    }
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY, Utils.quoteJsonToContentVals(getResponse));
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert ", e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }



    private String getQuoteUrl(String stock) {
        return "";
    }
    
    private String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void sendMessageToActivity(String json) {
        Intent intent = new Intent("HistoricalDetailData");

        intent.putExtra(StockIntentService.INTENT_DETAIL, json);
    }
}
