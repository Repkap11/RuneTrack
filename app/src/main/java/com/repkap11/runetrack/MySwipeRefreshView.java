package com.repkap11.runetrack;

import android.content.Context;
import android.util.AttributeSet;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;

public class MySwipeRefreshView extends PullToRefreshLayout {
    private MainActivity mActivity;
    public MySwipeRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (MainActivity) context;
    }

    //@Override
    //public boolean canChildScrollUp()
    //{
    //    return mActivity.canFragmentScrollUp();
    //    //your condition to check scrollview reached at top while scrolling
    //}
}
