/**
 * UserProgressFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.PiChart;
import com.repkap11.runetrack.R;
import com.repkap11.runetrack.TextDrawable;

public class XpDistributionChartFragment extends FragmentBase {

public static final String TAG = " XpDistributionChartFragment";
public int[] downloadResult;
public int[] downloadResult2;
private PiChart mPiChart;
private String userName;
private int skillNumber;
private String[] downloadResult3;

public XpDistributionChartFragment() {
	// Empty constructor required for fragment subclasses
}

@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
}

@Override
public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	outState.putIntArray(DownloadIntentService.PARAM_XP_COLORS, downloadResult2);
	outState.putIntArray(DownloadIntentService.PARAM_XP_PER_SKILL, downloadResult);
	outState.putStringArray(DownloadIntentService.PARAM_XP_SKILL_NAMES, downloadResult3);
}

@Override
public void onDetach() {
	//Log.e(TAG, "Fragment Detached");
	super.onDetach();
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
protected Intent requestDownload() {
	Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
	msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
	msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NUMBER, skillNumber);
	msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_XP_PI_CHART);
	return msgIntent;
}

@Override
public Drawable onInflateContentView(ViewGroup container) {
	TextDrawable result = new TextDrawable(getResources(), R.string.xp_pi_chart_download_error_message);
	LayoutInflater inflater = LayoutInflater.from(this.getActivity());
	userName = getArguments().getString(MainActivity.ARG_USERNAME);
	skillNumber = getArguments().getInt(MainActivity.ARG_SKILL_NUMBER);
	View rootView = inflater.inflate(R.layout.fragment_content_xp_pi_chart, container, true);
	mPiChart = ((PiChart) rootView.findViewById(R.id.xp_pi_chart_content));
	getActivity().setTitle(userName);
	return result;
}

@Override
protected void applyDownloadResultFromIntent(Bundle bundle) {
	downloadResult = bundle.getIntArray(DownloadIntentService.PARAM_XP_PER_SKILL);
	downloadResult2 = bundle.getIntArray(DownloadIntentService.PARAM_XP_COLORS);
	downloadResult3 = bundle.getStringArray(DownloadIntentService.PARAM_XP_SKILL_NAMES);
	applyDownloadResult(downloadResult, downloadResult2, downloadResult3);

}

public void applyDownloadResult(final int[] downloadResult4, final int[] downloadResult2, String[] downloadResult3) {
	refreshComplete();
	mPiChart.setPiChartData(downloadResult4, downloadResult2, downloadResult3);

}
}
