/**
 * HistoryGraphFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import java.text.NumberFormat;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public class XpDistributionChartFragment extends Fragment {

	@Override
	public void onDetach() {
		Log.e("Paul", "Fragment Detached");
		super.onDetach();
	}

	private PiChart mPiChart;
	private ResponseReceiver receiver;
	public float[] downloadResult;
	public int[] downloadResult2;
	private boolean needsDownload = true;
	private boolean needsToShowDownloadFailure = false;
	private String userName;
	private ViewSwitcher switcherFailure;
	private ViewSwitcher switcherContent;
	private TextView failureRetryButton;
	private int skillNumber;
	private String skillName;

	public XpDistributionChartFragment() {
		// Empty constructor required for fragment subclasses
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
		}
	}

	private void failureRetryOnClick(View v) {
		((MainActivity) this.getActivity()).selectPiChart(userName);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Log.e("Paul", "onResume needsDownloadFailure " +
		// needsToShowDownloadFailure);
		// Log.e("Paul", "onResume needsDownload " + needsDownload);
		if (needsDownload) {
			switcherContent.setDisplayedChild(0);
			Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
			msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
			msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NUMBER, skillNumber);
			msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_XP_PI_CHART);
			this.getActivity().startService(msgIntent);
			IntentFilter filter = new IntentFilter(DownloadIntentService.PARAM_USERNAME);

			filter.addCategory(Intent.CATEGORY_DEFAULT);
			receiver = new ResponseReceiver();
			getActivity().registerReceiver(receiver, filter);

		} else {

			if (needsToShowDownloadFailure) {
				switcherContent.setDisplayedChild(0);
				switcherFailure.setDisplayedChild(1);
			} else {
				switcherContent.setDisplayedChild(1);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (downloadResult != null) {
			outState.putIntArray(DownloadIntentService.PARAM_XP_COLORS, downloadResult2);
			outState.putFloatArray(DownloadIntentService.PARAM_XP_DEGREES, downloadResult);
			outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.xp_pi_chart, container, false);
		failureRetryButton = (TextView) rootView.findViewById(R.id.user_profile_error_message);
		failureRetryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				failureRetryOnClick(v);
			}
		});
		switcherContent = (ViewSwitcher) rootView.findViewById(R.id.switcher_loading_content);
		switcherFailure = (ViewSwitcher) rootView.findViewById(R.id.switcher_loading_failure);
		userName = getArguments().getString(MainActivity.ARG_USERNAME);
		skillNumber = getArguments().getInt(MainActivity.ARG_SKILL_NUMBER);

		mPiChart = ((PiChart) rootView.findViewById(R.id.content));

		getActivity().setTitle(userName);
		needsDownload = true;

		if (savedInstanceState != null) {

			needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
			Log.e("Paul", "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
			downloadResult = savedInstanceState.getFloatArray(DownloadIntentService.PARAM_XP_DEGREES);
			downloadResult2 = savedInstanceState.getIntArray(DownloadIntentService.PARAM_XP_COLORS);
			if (downloadResult != null) {
				needsDownload = false;
				applyDownloadResult(downloadResult, downloadResult2);
			}
		}
		return rootView;
	}

	public class ResponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Log.e("Paul", "before crash");
			downloadResult = intent.getFloatArrayExtra(DownloadIntentService.PARAM_XP_DEGREES);
			downloadResult2 = intent.getIntArrayExtra(DownloadIntentService.PARAM_XP_COLORS);
			if (downloadResult == null || downloadResult.length == 0) {
				switcherContent.setDisplayedChild(0);
				switcherFailure.setDisplayedChild(1);
				needsToShowDownloadFailure = true;
				// Toast.makeText(HistoryGraphFragment.this.getActivity(),
				// "Failure", Toast.LENGTH_SHORT).show();
			} else {
				switcherContent.setDisplayedChild(1);
				applyDownloadResult(downloadResult, downloadResult2);
			}
		}
	}

	public void applyDownloadResult(final float[] downloadResult, final int[] downloadResult2) {
 
		 mPiChart.setPiChartData(downloadResult, downloadResult2);

	}

	private int dpToPixals(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (dp * scale + 0.5f);
		return dpAsPixels;
	}

}
