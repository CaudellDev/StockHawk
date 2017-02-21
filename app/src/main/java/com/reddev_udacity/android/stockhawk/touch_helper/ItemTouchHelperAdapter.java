package com.reddev_udacity.android.stockhawk.touch_helper;

import android.support.v7.widget.RecyclerView;

/**
 * Created by sam_chordas on 10/6/15.
 * credit to Paul Burke (ipaulpro)
 * Interface to enable swipe to delete
 */
public interface ItemTouchHelperAdapter {
    void onItemDismiss(int position, RecyclerView rv);
}
