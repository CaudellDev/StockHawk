package com.reddev_udacity.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reddev_udacity.android.stockhawk.R;
import com.reddev_udacity.android.stockhawk.data.QuoteColumns;
import com.reddev_udacity.android.stockhawk.data.QuoteProvider;
import com.reddev_udacity.android.stockhawk.service.StockIntentService;
import com.reddev_udacity.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.reddev_udacity.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder> implements ItemTouchHelperAdapter, View.OnClickListener {

    private static final String LOG_TAG = QuoteCursorAdapter.class.getSimpleName();

    private static Context mContext;
    private static Typeface robotoLight;
    private boolean isPercent;

    private String deletedSymbol;
    private int deletedSymbolPos;
    private View parent;

    public QuoteCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
        View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_quote, parent, false);

        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
        viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));
        int sdk = Build.VERSION.SDK_INT;

        if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1) {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            } else {
                viewHolder.change.setBackground(mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
            }
        } else {
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.change.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            } else {
                viewHolder.change.setBackground(mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
            }
        }

        if (Utils.showPercent) {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
        } else {
            viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("change")));
        }
    }

    @Override public void onItemDismiss(int position, RecyclerView rv) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));

        mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);

        // Snackbar for undo
        deletedSymbol = symbol;
        deletedSymbolPos = position;


        Snackbar snackbar = Snackbar.make(rv, "You deleted " + symbol + ".", Snackbar.LENGTH_LONG)
                                    .setAction("UNDO", this);

        snackbar.show();

        notifyItemRemoved(position);
    }

    @Override public int getItemCount() {
    return super.getItemCount();
    }

    @Override
    public void onClick(View v) {
        Log.d(LOG_TAG, "Snackbar callback function - UNDO button clicked for symbol: " + deletedSymbol);

        Intent intent = new Intent(mContext, StockIntentService.class);
        intent.putExtra("tag", "add");
        intent.putExtra("symbol", deletedSymbol);
        intent.putExtra("position", deletedSymbolPos);
        mContext.startService(intent);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener {

        public final TextView symbol;
        public final TextView bidPrice;
        public final TextView change;

        public ViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            symbol.setTypeface(robotoLight);
            bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            change = (TextView) itemView.findViewById(R.id.change);
        }

        @Override
        public void onItemSelected(){
        itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear(){
        itemView.setBackgroundColor(0);
        }

        @Override
        public void onClick(View v) {
            Log.i(LOG_TAG, "onClick -> " + v);
        }
    }
}
