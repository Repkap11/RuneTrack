/**
 * UserProgressFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.content.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.repkap11.runetrack.*;

public class XpDistributionChartFragment extends FragmentBase {

public static final String TAG = " XpDistributionChartFragment";
public int[] downloadResult;
public int[] downloadResult2;
private PiChart mPiChart;
private ResponseReceiver receiver;
private boolean needsDownload = true;
private boolean needsToShowDownloadFailure = false;
private String userName;
private int skillNumber;
private String[] downloadResult3;
private ViewSwitcher switcherUserGainedNoXP;
private boolean needsToShowUserGainedNoXP;

public XpDistributionChartFragment() {
	// Empty constructor required for fragment subclasses
}

@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
}

@Override
public void onResume() {
	super.onResume();
	// Log.e(TAG, "onResume needsDownloadFailure " +
	// needsToShowDownloadFailure);
	// Log.e(TAG, "onResume needsDownload " + needsDownload);
	if(needsDownload) {
		setSwitchedView(FragmentBase.SWITCHED_VIEW_SPINNER);
		Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
		msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
		msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NUMBER, skillNumber);
		msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_XP_PI_CHART);
		this.getActivity().startService(msgIntent);
		IntentFilter filter = new IntentFilter(DownloadIntentService.PARAM_USERNAME);

		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		getActivity().registerReceiver(receiver, filter);

	}else {

		if(needsToShowDownloadFailure) {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
			if(needsToShowUserGainedNoXP) {
				switcherUserGainedNoXP.setDisplayedChild(1);
			}else {
				switcherUserGainedNoXP.setDisplayedChild(0);
			}
		}else {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
		}
	}
}

@Override
public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putIntArray(DownloadIntentService.PARAM_XP_COLORS, downloadResult2);
	outState.putIntArray(DownloadIntentService.PARAM_XP_PER_SKILL, downloadResult);
	outState.putStringArray(DownloadIntentService.PARAM_XP_SKILL_NAMES, downloadResult3);
	outState.putBoolean(DownloadIntentService.PARAM_XP_USER_GAINED_NO_XP, needsToShowUserGainedNoXP);
	outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
}

@Override
public void onPause() {
	super.onPause();
	if(receiver != null) {
		getActivity().unregisterReceiver(receiver);
	}
}

@Override
public void onDetach() {
	Log.e(TAG, "Fragment Detached");
	super.onDetach();
}

@Override
protected boolean isWaitingForData() {
	return false;
}

@Override
public void reloadData() {
	((MainActivity) this.getActivity()).selectPiChart(userName);
}

@Override
public boolean canScrollUp() {
	//Log.e(TAG,"XP Chart Scroll called, returned false");
	return false;
}

@Override
public Drawable onInflateContentView(ViewGroup container) {
	TextDrawable result = new TextDrawable(getResources().getString(R.string.xp_pi_chart_download_error_message));
	LayoutInflater inflater = LayoutInflater.from(this.getActivity());
	userName = getArguments().getString(MainActivity.ARG_USERNAME);
	skillNumber = getArguments().getInt(MainActivity.ARG_SKILL_NUMBER);
	View rootView = inflater.inflate(R.layout.fragment_content_xp_pi_chart, container, true);
	//switcherUserGainedNoXP = (ViewSwitcher) rootView.findViewById(R.id.switcher_user_got_no_xp);
	mPiChart = ((PiChart) rootView.findViewById(R.id.xp_pi_chart_content));

	getActivity().setTitle(userName);
	needsDownload = true;

	if(mSavedInstanceState != null) {

		needsToShowDownloadFailure = mSavedInstanceState.getBoolean("needsToShowDownloadFailure");
		Log.e(TAG, "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
		downloadResult = mSavedInstanceState.getIntArray(DownloadIntentService.PARAM_XP_PER_SKILL);
		downloadResult2 = mSavedInstanceState.getIntArray(DownloadIntentService.PARAM_XP_COLORS);
		downloadResult3 = mSavedInstanceState.getStringArray(DownloadIntentService.PARAM_XP_SKILL_NAMES);
		needsToShowUserGainedNoXP = mSavedInstanceState.getBoolean(DownloadIntentService.PARAM_XP_USER_GAINED_NO_XP);
		Log.e(TAG, "needsToShowUserGainedNoXP updated from state : " + needsToShowUserGainedNoXP);
		if(downloadResult != null) {
			needsDownload = false;
			applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
		}
		if(needsToShowUserGainedNoXP) {
			needsDownload = false;
			Log.e(TAG, "Hit this place");
		}
	}
	return result;
}

public void applyDownloadResult(final int[] downloadResult4, final int[] downloadResult2, String[] downloadResult3) {
	refreshComplete();
	mPiChart.setPiChartData(downloadResult4, downloadResult2, downloadResult3);

}

public class ResponseReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.e(TAG, "before crash");
		downloadResult = intent.getIntArrayExtra(DownloadIntentService.PARAM_XP_PER_SKILL);
		downloadResult2 = intent.getIntArrayExtra(DownloadIntentService.PARAM_XP_COLORS);
		downloadResult3 = intent.getStringArrayExtra(DownloadIntentService.PARAM_XP_SKILL_NAMES);
		needsToShowUserGainedNoXP = intent.getBooleanExtra(DownloadIntentService.PARAM_XP_USER_GAINED_NO_XP, needsToShowUserGainedNoXP);
		Log.e(TAG, "needsToShowUserGainedNoXP:" + needsToShowUserGainedNoXP);
		if(downloadResult == null || downloadResult.length == 0) {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
			needsToShowDownloadFailure = true;
			if(needsToShowUserGainedNoXP) {
				//switcherUserGainedNoXP.setDisplayedChild(1);
			}else {
				//switcherUserGainedNoXP.setDisplayedChild(0);
			}
			// Toast.makeText(UserProgressFragment.this.getActivity(),
			// "Failure", Toast.LENGTH_SHORT).show();
		}else {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
			applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
		}
	}
}
}
