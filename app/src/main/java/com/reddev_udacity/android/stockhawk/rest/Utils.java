package com.reddev_udacity.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.util.Log;
import com.reddev_udacity.android.stockhawk.data.QuoteColumns;
import com.reddev_udacity.android.stockhawk.data.QuoteProvider;
import com.reddev_udacity.android.stockhawk.graph.HistoPointData;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper functions for the program.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;

        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));

                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results");
                    jsonObject = jsonObject.getJSONObject("quote");
                    batchOperations.add(buildBatchOperation(jsonObject));
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
//                        Log.v(LOG_TAG, "JSON Array \"query\":" + resultsArray.toString());

                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
            e.printStackTrace();
        }
        return batchOperations;
    }

    private static String truncateBidPrice(String bidPrice) {
        if (!bidPrice.equals("null")) {
            bidPrice = String.format(Locale.ENGLISH, "%.2f", Float.parseFloat(bidPrice));
        }
        return bidPrice;
    }

    private static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0,1);
        String ampersand = "";

        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }

        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.ENGLISH, "%.2f", round);

        StringBuilder changeBuilder = new StringBuilder(change);
        changeBuilder.insert(0, weight);
        changeBuilder.append(ampersand);
        change = changeBuilder.toString();

        return change;
    }

    private static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(QuoteProvider.Quotes.CONTENT_URI);

        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);

            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return builder.build();
    }
    
    public static ArrayList<HistoPointData> parseHistoricalJson(String json) {
//        Log.v(LOG_TAG, "parseHistoricalJson data: " + json);
        
        ArrayList<HistoPointData> dailyData = new ArrayList<>();
        
        try {
            JSONObject jsonObj = new JSONObject(json);
            jsonObj = jsonObj.getJSONObject("query").getJSONObject("results");
            JSONArray jsonArr = jsonObj.getJSONArray("quote");
            int length = jsonArr.length();
            for (int i = 0; i < length; i++) {
                // Do stuff...
                JSONObject currObj = jsonArr.optJSONObject(length - (i+1)); // I need to reverse the order so the date is acs
                HistoPointData currPoint = new HistoPointData(currObj);
                dailyData.add(currPoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dailyData;
    }

    public static boolean isJsonValid(String json) {
        int nulls = 0;

        JSONObject obj;

        try {
            obj = new JSONObject(json);
            obj = obj.getJSONObject("query").getJSONObject("results").getJSONObject("quote");

//            Utils.log5(LOG_TAG, "isJsonValid - JSON String quote: " + obj);
//            Log.v(LOG_TAG, "isJsonValid - JSON String, is change null: " + (obj.getString("Change") == null));
//            Log.v(LOG_TAG, "isJsonValid - JSON String, is change null: " + (obj.isNull("Change")));

            // Increment nulls each time it equals null.
            if (obj.isNull("Change")) nulls++;
            if (obj.isNull("Bid")) nulls++;
            if (obj.isNull("ChangeinPercent")) nulls++;
            if (obj.isNull("Volume")) nulls++;
            if (obj.isNull("Open")) nulls++;
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Utils.log5(LOG_TAG, "isJsonValid - nulls: " + nulls + ", valid: " + (nulls <= 3));

        // https://query.yahooapis.com/v1/public/yql?q=select+*+from+yahoo.finance.quotes+where+symbol+in+%28%22ddgj%22%29&format=json&diagnostics=true&env=store://datatables.org/alltableswithkeys&callback=
        return nulls <= 3; // If it's more than 3, it's probably an invalid stock. Will experiment with other values too.
    }

    public static boolean hasOnlyLetters(String symbol) {

        char[] temp = symbol.toCharArray();
//        Log.v(LOG_TAG, "hasOnlyLetters - temp: " + temp);

        for (char curr : temp) {
//            Log.v(LOG_TAG, "hasOnlyLetters - curr: " + curr);
            if (curr < 'A' || curr > 'Z') {
                return false;
            }
        }

        return true;
    }

    public static void log5(String TAG, String MSG) {
//        for (int i = 0; i < 5; i ++) Log.v(TAG, "--|");
//        Log.v(TAG, MSG);
    }
}
