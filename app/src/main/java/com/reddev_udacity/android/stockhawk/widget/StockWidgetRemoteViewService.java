package com.reddev_udacity.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.reddev_udacity.android.stockhawk.R;
import com.reddev_udacity.android.stockhawk.data.QuoteColumns;
import com.reddev_udacity.android.stockhawk.data.QuoteDatabase;
import com.reddev_udacity.android.stockhawk.data.QuoteProvider;
import com.reddev_udacity.android.stockhawk.rest.Utils;

public class StockWidgetRemoteViewService extends RemoteViewsService {

    private static final String LOG_TAG = StockWidgetRemoteViewService.class.getSimpleName();

    private static final String[] STOCK_COLUMNS = {
            QuoteDatabase.QUOTES + "." + QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.ISUP
    };


    static final int INDEX_STOCK_ID = 0;
    static final int INDEX_STOCK_SYMBOL = 1;
    static final int INDEX_STOCK_PERCENT_CHANGE = 2;
    static final int INDEX_STOCK_CHANGE = 3;
    static final int INDEX_STOCK_BIDPRICE = 4;
    static final int INDEX_STOCK_ISUP = 5;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        // Create anonymous RemoteViewsFactory instance
        return new RemoteViewsFactory() {
            private Cursor data;

            @Override
            public void onCreate() {
//                Log.v(LOG_TAG, "#######--- onCreate ---#######");
            }

            @Override
            public void onDataSetChanged() {

                if (data != null) {
                    data.close();
                }

                final long token = Binder.clearCallingIdentity();
                data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        STOCK_COLUMNS,
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        QuoteColumns.SYMBOL + " ASC"
                );

                Binder.restoreCallingIdentity(token);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                int count = data == null ? 0 : data.getCount();
//                Log.v(LOG_TAG, "getCount(): " + count);
                return count;
            }

            @Override
            public RemoteViews getViewAt(int index) {
//                Log.v(LOG_TAG, "#######--- RemoteViewsFactory: getViewAt (index = " + index + ") ---#######");

                if (index == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(index)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                String stock_symbol = data.getString(INDEX_STOCK_SYMBOL);
                String stock_price = data.getString(INDEX_STOCK_BIDPRICE);
                String stock_change = data.getString(INDEX_STOCK_CHANGE);
                String stock_change_percent = data.getString(INDEX_STOCK_PERCENT_CHANGE);
                String description;

                views.setTextViewText(R.id.stock_symbol, stock_symbol);
                views.setTextViewText(R.id.bid_price, stock_price);

                if (data.getInt(INDEX_STOCK_ISUP) == 1) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);

                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                if (Utils.showPercent) {
                    views.setTextViewText(R.id.change, stock_change_percent + " "); // Space for aesthetic
                    description = getString(R.string.a11y_stock_summary, stock_symbol, stock_price, stock_change_percent);
                } else {
                    views.setTextViewText(R.id.change, stock_change.trim() + " "); // Space for aesthetic
                    description = getString(R.string.a11y_stock_summary, stock_symbol, stock_price, stock_change);
                }

                setRemoteContentDescription(views, description);

                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.stock_list_item, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int index) {
                if (data.moveToPosition(index)) return data.getLong(INDEX_STOCK_ID);
                return index;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}