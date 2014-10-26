package com.repkap11.runetrack;

import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.util.*;

public class SwipeRefreshLoadFailingView extends LinearLayout {
private MainActivity mActivity;

public SwipeRefreshLoadFailingView(Context context, AttributeSet attrs) {
	super(context, attrs);
	Log.e("TAG", "CustomView constructed");
	mActivity = (MainActivity) context;
	/*
	TypedArray a = context.getTheme().obtainStyledAttributes(
			attrs,
			R.styleable.SwipeRefreshLoadFailingView,
			0, 0);
	try {
		int childLayout = a.getResourceId(R.styleable.SwipeRefreshLoadFailingView_mainContent, 0);

	} finally {
		a.recycle();
	}
	*/
	mActivity.mCurrentFragment.inflateContentView(this);
	Log.e("TAG", "View added to base");

}

@Override
public void findViewsWithText(ArrayList<View> outViews, CharSequence text, int flags) {
	Log.e("TAG", "Method called" + outViews.size());
	super.findViewsWithText(outViews, text, flags);
}
}
