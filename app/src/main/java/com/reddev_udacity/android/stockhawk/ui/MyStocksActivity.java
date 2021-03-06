package com.reddev_udacity.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.reddev_udacity.android.stockhawk.R;
import com.reddev_udacity.android.stockhawk.data.QuoteColumns;
import com.reddev_udacity.android.stockhawk.data.QuoteProvider;
import com.reddev_udacity.android.stockhawk.graph.HistoLineData;
import com.reddev_udacity.android.stockhawk.rest.QuoteCursorAdapter;
import com.reddev_udacity.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.reddev_udacity.android.stockhawk.rest.Utils;
import com.reddev_udacity.android.stockhawk.service.StockIntentService;
import com.reddev_udacity.android.stockhawk.service.StockTaskService;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.reddev_udacity.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MyStocksActivity.class.getSimpleName();

    public static final String BAD_STOCK_NOTFOUND_TAG = "bad_stock_notfound_broadcast";
    public static final String BAD_STOCK_INVALID_TAG = "bad_stock_invalid_broadcast";

    public static final String ACTION_DATA_UPDATED = "com.reddev_udacity.android.stockhawk.ACTION_DATA_UPDATED";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Intent mServiceIntent;
    private TaskReceiver mTaskReceiver;
    private ItemTouchHelper mItemTouchHelper;
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Context mContext;
    private Cursor mCursor;
    private MenuItem mItem;
    private String errorDialogBadStock;
    private String errorDialogBadStockTag;
    private boolean errorDialogMsg;
    private boolean details_started;
    boolean isConnected;
    boolean init = true;

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        errorDialogMsg = false;
        details_started = false;

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        setContentView(R.layout.activity_my_stocks);

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(this, StockIntentService.class);
        if (savedInstanceState == null || init) {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (isConnected){
                init = false;
                startService(mServiceIntent);
            } else {
                // Empty view visible code here
                noNetworkView();
                noNetworkToast();
            }
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(this, null);

        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this,
            new RecyclerViewItemClickListener.OnItemClickListener() {
                @Override public void onItemClick(View v, int position) {
                    //TODO:
                    // do something on item click
//                    Log.i(LOG_TAG, "RecyclerView onItemTouchListener - position: " + position);

                    mCursor.moveToPosition(position);
                    int columnIndex = mCursor.getColumnIndex(QuoteColumns.SYMBOL);
                    String symbol = mCursor.getString(columnIndex);

//                    Log.i(LOG_TAG, "You clicked on: " + symbol);

                    // Start the service and create the new activity.
                    if (isConnected) {
                        mServiceIntent.putExtra(StockIntentService.INTENT_TAG, StockIntentService.INTENT_DETAIL);
                        mServiceIntent.putExtra(StockIntentService.INTENT_SYMBOL, symbol);

                        startService(mServiceIntent);
                    } else {
                        noNetworkToast();
                    }
                }
            }));

        mRecyclerView.setAdapter(mCursorAdapter);


        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (isConnected) {

                    String inputHint = getString(R.string.input_hint);
                    if (errorDialogMsg) {
                        if (errorDialogBadStockTag.equals(MyStocksActivity.BAD_STOCK_NOTFOUND_TAG)) {
                            inputHint = getString(R.string.error_hint_not_found, errorDialogBadStock);
                        } else if (errorDialogBadStockTag.equals(MyStocksActivity.BAD_STOCK_INVALID_TAG)) {
                            inputHint = getString(R.string.error_hint_special_char, errorDialogBadStock);
                        }

                        errorDialogMsg = false;
                    }

                    new MaterialDialog.Builder(mContext).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(inputHint, "", new MaterialDialog.InputCallback() {
                                @Override public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
//                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
//                                                                        new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
//                                                                        new String[] { input.toString() }, null);

                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[]{QuoteColumns.SYMBOL}, QuoteColumns.SYMBOL + "= ?",
                                            new String[]{input.toString()}, null);

                                    if (c != null) {
                                        Log.d(LOG_TAG, "Cursor count: " + c.getCount());
                                    } else {
                                        Log.d(LOG_TAG, "Cursor is null");
                                        return;
                                    }

                                    if (c.getCount() != 0) {
                                        Toast toast = Toast.makeText(MyStocksActivity.this, R.string.stock_already_saved, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("symbol", input.toString());
                                        startService(mServiceIntent);
                                    }

//                                    c.close();
                                }
                    }).show();

                } else {
                    // Empty view code here
                    noNetworkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter, mRecyclerView);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mTitle = getTitle();
        if (isConnected) {
            long period = 3600L;
            long flex = 10L;
            String periodicTag = "periodic";

            // create a periodic task to pull stocks once every hour after the app has been opened. This
            // is so Widget data stays up to date.
            PeriodicTask periodicTask = new PeriodicTask.Builder()
                    .setService(StockTaskService.class)
                    .setPeriod(period)
                    .setFlex(flex)
                    .setTag(periodicTag)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setRequiresCharging(false)
                    .build();

            // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
            // are updated.
            GcmNetworkManager.getInstance(this).schedule(periodicTask);
        }
        
        // Setup BroadcastReciever to get data from service
        // when user clicks on a Stock, and to launch new activity.
        mTaskReceiver = new TaskReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mTaskReceiver, new IntentFilter(TaskReceiver.RECEIVER_TAG));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recheck for network in case it changed
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        details_started = false;
    }

    public void noNetworkView() {
        if (mRecyclerView == null) mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        View emptyView = findViewById(R.id.recycler_empty_view);
        Button retryBtn = (Button) findViewById(R.id.recycler_empty_retry_btn);

        mRecyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                View coord_view = findViewById(R.id.stock_activity_coord_layout);
                if (isConnected) {
                    connectedNetworkView();
                    if (coord_view != null) Snackbar.make(coord_view, R.string.network_conn_found, Snackbar.LENGTH_SHORT).show();
//                    else Log.e(LOG_TAG, "(Is Connected) Coordinator View is null! Oh no! :(");
                } else {
                    if (coord_view != null) Snackbar.make(coord_view, R.string.network_conn_not_found, Snackbar.LENGTH_SHORT).show();
//                    else Log.e(LOG_TAG, "(Not Connected) Coordinator View is null! Oh no! :(");
                }
            }
        });
    }

    public void connectedNetworkView() {
        if (mRecyclerView == null) mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        View emptyView = findViewById(R.id.recycler_empty_view);

        emptyView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void noNetworkToast(){
        Snackbar.make(mRecyclerView, getString(R.string.network_toast), Snackbar.LENGTH_SHORT).show();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    private void updateWidgets() {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(this.getPackageName());
        sendBroadcast(dataUpdatedIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
        restoreActionBar();

        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.action_change_units) {
                mItem = menu.getItem(i);
                break;
            }
        }

        updateMenuItemDesc();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_change_units){
//            Log.v(LOG_TAG, "onOptionsItemSelected - Change Units selected.");

            // this is for changing stock changes from percent value to dollar value
            Utils.showPercent = !Utils.showPercent;
            this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
            updateWidgets();

            updateMenuItemDesc();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        Log.v(LOG_TAG, "onCreateLoader - args: " + args);

        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                              QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                                QuoteColumns.ISCURRENT + " = ?",
                                new String[]{"1"},
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
//        Log.v(LOG_TAG, "onLoadFinished - data: " + data);

        mCursorAdapter.swapCursor(data);


        updateWidgets();

        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
//        Log.v(LOG_TAG, "onLoaderReset - loader: " + loader);

        mCursorAdapter.swapCursor(null);
    }
    
    public class TaskReceiver extends BroadcastReceiver {

        public static final String RECEIVER_TAG = "task_receiver_tag";
        
        @Override
        public void onReceive(Context context, Intent intent) {

            String tag = intent.getStringExtra(RECEIVER_TAG);

//            Log.v(LOG_TAG, "TaskReceiver tag: " + tag);

            switch (tag) {
                case HistoLineData.HISTO_TAG:

                    String data = intent.getStringExtra(HistoLineData.HISTO_TAG);
//                    Log.v(LOG_TAG, "onMessageRecived: " + data);

                    HistoLineData histoLineData = new HistoLineData(data);

                    Bundle extra = new Bundle();
                    extra.putParcelable(HistoLineData.HISTO_TAG, histoLineData);

                    // Testing activity launch and sending info
                    Intent details = new Intent(context, StocksDetailActivity.class);
                    details.putExtra(HistoLineData.HISTO_TAG, extra);

                    if (!details_started) {
                        details_started = true;
                        startActivity(details);
                    }

                    break;
                case BAD_STOCK_NOTFOUND_TAG:
                case BAD_STOCK_INVALID_TAG:

                    errorDialogBadStock = intent.getStringExtra(tag);
                    errorDialogBadStockTag = tag;
                    errorDialogMsg = true;

                    // Reopen the dialog to prompt the user to enter in another stock.
                    mFab.performClick();

                    break;
                default:
                    Log.e(LOG_TAG, "Unknown tag: " + tag);
            }
        }
    }

    private void updateMenuItemDesc() {
        String desc;

        if (Utils.showPercent) {
            desc = getString(R.string.menu_change_units_cp);
        } else {
            desc = getString(R.string.menu_change_units_cv);
        }

        mItem.setTitle(desc);
    }
}
