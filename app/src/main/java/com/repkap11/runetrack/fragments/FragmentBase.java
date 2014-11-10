/**
 * FragmentBase.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;
import com.repkap11.runetrack.TextDrawable;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class FragmentBase extends Fragment implements OnRefreshListener {
protected static final int SWITCHED_VIEW_SPINNER = 0;
protected static final int SWITCHED_VIEW_RETRY = 1;
protected static final int SWITCHED_VIEW_CONTENT = 2;
protected static final int IMAGE_CHAR_SIZE = 3;

private static final String TAG = FragmentBase.class.getSimpleName();
public PullToRefreshLayout mPullToRefreshLayout;
private ViewSwitcher switcherOutside;
private ViewSwitcher switcherInside;
private Drawable mDrawable;
private View mErrorMessageView = null;
private int mDownloadErrorCode;
private ResponseReceiver mReceiver;
private Bundle mDownloadedData = null;
private String mWhichData = "";
private boolean mNeedsDownload = true;

public static DataTableBounds calculateLayoutSize(ArrayAdapter<Parcelable> arrayAdapter, Context context, ListView view) {
	// Log.e(TAG, "Calculating Bounds");
	Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	Point p = new Point();
	d.getSize(p);
	int oldwidth = p.x;
	int width = oldwidth - view.getPaddingLeft() - view.getPaddingRight();
	// Log.e(TAG, "old:" + oldwidth + " new:" + width);
	int total = 0;
	int[] totals = new int[((DataTable) arrayAdapter.getItem(0)).mListOfItems.size() - 1];
	for(int i = 1; i < totals.length + 1; i++) {
		int max = 0;
		for(int j = 0; j < arrayAdapter.getCount(); j++) {
			int curSize = ((DataTable) arrayAdapter.getItem(j)).mListOfItems.get(i).length();
			if(max < curSize) {
				max = curSize;
			}
		}

		totals[i - 1] = max + 1;// +1 for padding text with a space
		// Log.e(TAG, "Totals[" + (i - 1) + "] = " + totals[i - 1]);
		total += max + 1;
	}
	// Log.e(TAG, "Total" + " = " + total);
	int imageSize = (int) ((width) / (total + IMAGE_CHAR_SIZE) * IMAGE_CHAR_SIZE);
	// image takes two spaces worth of size
	float textSize = refitText(" ", (width) / (total + IMAGE_CHAR_SIZE) * 1);
	// Log.e(TAG, "textSize" + " = " +textSize);
	return new DataTableBounds(imageSize, totals, total, width, textSize);
}

public static float refitText(String text, int textWidth) {
	if(textWidth <= 0) {
		return 0;
	}
	float hi = 100;
	float lo = 2;
	final float threshold = 0.005f; // How close we have to be
	Paint testPaint = new Paint();

	while((hi - lo) > threshold) {
		float size = (hi + lo) / 2;
		testPaint.setTextSize(size);
		testPaint.setTypeface(Typeface.MONOSPACE);
		if(testPaint.measureText(text) >= textWidth) {
			hi = size; // too big
		}else {
			lo = size; // too small
		}
	}
	// Use lo so that we undershoot rather than overshoot
	return lo;
}

public static int dpToPixals(Context context, int dp) {
	float scale = context.getResources().getDisplayMetrics().density;
	return (int) (dp * scale + 0.5f);
}

public static void makeTextViewHyperlink(TextView tv) {
	SpannableStringBuilder ssb = new SpannableStringBuilder();
	ssb.append(tv.getText());
	ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	tv.setText(ssb, TextView.BufferType.SPANNABLE);
}

private void failureRetryOnClick(View v) {
	reloadData();
}

public abstract void reloadData();

private void setErrorMessage(int textID) {
	((TextDrawable) mDrawable).setText(getResources(), textID);
	if(Build.VERSION.SDK_INT >= 16) {
		mErrorMessageView.setBackground(mDrawable);
	}else {
		mErrorMessageView.setBackgroundDrawable(mDrawable);
	}
}

@Override
public void onCreate(Bundle savedInstanceState) {
	//Toast.makeText(getActivity().getApplicationContext(), "fragment created", Toast.LENGTH_SHORT).show();
	//Log.e(TAG, "fragment created");
	super.onCreate(savedInstanceState);
	setRetainInstance(true);
	mNeedsDownload = true;
	mReceiver = new ResponseReceiver();
	IntentFilter filter = new IntentFilter(DownloadIntentService.PARAM_USERNAME);
	filter.addCategory(Intent.CATEGORY_DEFAULT);
	getActivity().getApplicationContext().registerReceiver(mReceiver, filter);
}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_content_base, container, false);
	mErrorMessageView = rootView.findViewById(R.id.content_error_message);
	if(Build.VERSION.SDK_INT >= 16) {
		mErrorMessageView.setBackground(mDrawable);
	}else {
		mErrorMessageView.setBackgroundDrawable(mDrawable);
	}
	switcherOutside = (ViewSwitcher) rootView.findViewById(R.id.switcher_outside);
	switcherInside = (ViewSwitcher) rootView.findViewById(R.id.switcher_inside);
	mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.swipe_container);

	//rootView.findViewsWithText(outViews, getResources().getString(R.string.pullable_to_refresh_view), View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
	ArrayList<View> outViews = new ArrayList<View>();
	fixedFindViewsWithText(rootView, outViews, getResources().getString(R.string.pullable_to_refresh_view));
	View[] pullableViews = new View[0];
	pullableViews = outViews.toArray(pullableViews);
	//Log.e("TAG", "Found scrollable lists:" + pullableViews.length);
	ActionBarPullToRefresh.from(getActivity()).theseChildrenArePullable(pullableViews).listener(FragmentBase.this).setup(mPullToRefreshLayout);
	//mDownloadedData = savedInstanceState;
	if(savedInstanceState != null) {
		mDownloadErrorCode = savedInstanceState.getInt(DownloadIntentService.PARAM_ERROR_CODE);
	}else {
		//booleans set by default value of class.
	}
	applyData(mNeedsDownload);
	mNeedsDownload = false;
	return rootView;
}

private void applyData(boolean needsDownload) {
	if(needsDownload) {
		setSwitchedView(FragmentBase.SWITCHED_VIEW_SPINNER);
		Intent msgIntent = requestDownload();
		mWhichData = msgIntent.getStringExtra(DownloadIntentService.PARAM_WHICH_DATA);
		//isDownloadPending = true;
		this.getActivity().getApplicationContext().startService(msgIntent);
	}else if(mDownloadedData != null) {
		if(mDownloadErrorCode != DownloadIntentService.ERROR_CODE_SUCCESS) {
			setErrorMessageBasedOnCode(mDownloadErrorCode);
			setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
		}else {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
			applyDownloadResultFromIntent(mDownloadedData);
		}
	}
	//}
}

@Override
public void onDestroy() {
	if(mReceiver != null) {
		getActivity().getApplicationContext().unregisterReceiver(mReceiver);
	}
	super.onDestroy();
}

public void fixedFindViewsWithText(ViewGroup rootView, ArrayList<View> outViews, String string) {
	CharSequence description = rootView.getContentDescription();
	if(description != null && description.toString().equals(string)) {
		//Log.e("TAG", "Found scrollable lists");
		outViews.add(rootView);
	}
	for(int i = 0; i < rootView.getChildCount(); i++) {
		View child = rootView.getChildAt(i);
		if(child instanceof ViewGroup) {
			fixedFindViewsWithText((ViewGroup) child, outViews, string);
		}
	}

}

private void setErrorMessageBasedOnCode(int errorCode) {
	int errorMessageID;
	switch(errorCode) {
		case DownloadIntentService.ERROR_CODE_UNKNOWN:
			errorMessageID = R.string.ERROR_CODE_UNKNOWN;
			break;
		case DownloadIntentService.ERROR_CODE_NOT_ON_RS_HIGHSCORES:
			errorMessageID = R.string.ERROR_CODE_NOT_ON_RS_HIGHSCORES;
			break;
		case DownloadIntentService.ERROR_CODE_NOT_ENOUGH_VIEWS:
			errorMessageID = R.string.ERROR_CODE_NOT_ENOUGH_VIEWS;
			break;
		case DownloadIntentService.ERROR_CODE_RUNETRACK_DOWN:
			errorMessageID = R.string.ERROR_CODE_RUNETRACK_DOWN;
			break;
		case DownloadIntentService.ERROR_CODE_PI_CHART_USER_GAINED_NO_XP:
			errorMessageID = R.string.ERROR_CODE_PI_CHART_USER_GAINED_NO_XP;
			break;
		default:
			Log.e(TAG,"Unexpected error code:"+errorCode+" returned");
			errorMessageID = R.string.ERROR_CODE_UNKNOWN;
	}
	setErrorMessage(errorMessageID);
}

@Override
public void onRefreshStarted(View view) {
	//Log.e(TAG, "Refresh started");
	reloadData();
}

protected void refreshComplete() {
	((MainActivity) getActivity()).refreshComplete();
}

protected abstract Intent requestDownload();

private void setSwitchedView(int state) {
	switch(state) {
		case SWITCHED_VIEW_SPINNER:
			switcherOutside.setDisplayedChild(0);
			break;
		case SWITCHED_VIEW_RETRY:
			switcherOutside.setDisplayedChild(1);
			switcherInside.setDisplayedChild(0);
			break;
		case SWITCHED_VIEW_CONTENT:
			switcherOutside.setDisplayedChild(1);
			switcherInside.setDisplayedChild(1);
			break;
	}
}

@Override
public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putInt(DownloadIntentService.PARAM_ERROR_CODE, mDownloadErrorCode);
}

public void inflateContentView(ViewGroup container) {
	mDrawable = onInflateContentView(container);
}

protected abstract Drawable onInflateContentView(ViewGroup container);

protected abstract void applyDownloadResultFromIntent(Bundle intent);

public class ResponseReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//mIsDownloadPending = false;
		//mHasDownloadedData = true;
		String whichData = intent.getStringExtra(DownloadIntentService.PARAM_WHICH_DATA);
		if(mWhichData.equals(whichData)) {
			mDownloadedData = intent.getExtras();
			mDownloadErrorCode = intent.getIntExtra(DownloadIntentService.PARAM_ERROR_CODE, mDownloadErrorCode);
			Log.e(TAG, "Got download result in base fragment error code:" + mDownloadErrorCode);
			if(mDownloadErrorCode != DownloadIntentService.ERROR_CODE_SUCCESS) {
				setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
				setErrorMessageBasedOnCode(mDownloadErrorCode);
			}else {
				setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
				applyDownloadResultFromIntent(intent.getExtras());
			}
		}else {
			//Toast.makeText(getActivity(),"Would have crashed becasue of wrong data type",Toast.LENGTH_SHORT).show();
		}
	}
}
}
