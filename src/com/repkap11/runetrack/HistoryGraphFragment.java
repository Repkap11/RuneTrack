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
import android.widget.LinearLayout;
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
public class HistoryGraphFragment extends Fragment {

	@Override
	public void onDetach() {
		Log.e("Paul", "Fragment Detached");
		super.onDetach();
	}

	private LinearLayout mGraphHolder;
	private ResponseReceiver receiver;
	public double[] downloadResult;
	public String[] downloadResult2;
	private boolean needsDownload = true;
	private boolean needsToShowDownloadFailure = false;
	private String userName;
	private ViewSwitcher switcherFailure;
	private ViewSwitcher switcherContent;
	private TextView failureRetryButton;
	private int skillNumber;
	private String skillName;

	public HistoryGraphFragment() {
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
		((MainActivity) this.getActivity()).selectHitsoryGraph(userName, skillNumber, skillName);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.e("Paul", "onResume needsDownloadFailure " + needsToShowDownloadFailure);
		Log.e("Paul", "onResume needsDownload " + needsDownload);
		if (needsDownload) {
			switcherContent.setDisplayedChild(0);
			Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
			msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
			msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NUMBER, skillNumber);
			msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_HISTORY_GRAPH);
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
			outState.putDoubleArray(DownloadIntentService.PARAM_USERNAME, downloadResult);
			outState.putStringArray(DownloadIntentService.PARAM_USER_PROFILE_TABLE, downloadResult2);
			outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.history_graph, container, false);
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

		mGraphHolder = ((LinearLayout) rootView.findViewById(R.id.content));

		getActivity().setTitle(userName);
		needsDownload = true;

		if (savedInstanceState != null) {

			needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
			Log.e("Paul", "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
			downloadResult = savedInstanceState.getDoubleArray(DownloadIntentService.PARAM_USERNAME);
			downloadResult2 = savedInstanceState.getStringArray(DownloadIntentService.PARAM_USER_PROFILE_TABLE);
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
			downloadResult = intent.getDoubleArrayExtra(DownloadIntentService.PARAM_USER_PROFILE_TABLE);
			downloadResult2 = intent.getStringArrayExtra(DownloadIntentService.PARAM_USER_PROFILE_TABLE2);
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

	public void applyDownloadResult(final double[] result, final String[] result2) {
		if (result == null) {
			Log.e("Paul", "Result Null");
		} else if (this.getActivity() == null) {
			Log.e("Paul", "Activity is Null");
		} else {
			Log.e("Paul", "All good, neither null");
		}
		final GraphView graphView = new LineGraphView(this.getActivity(), skillName);
		graphView.getGraphViewStyle().setGridColor(Color.WHITE);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Align.RIGHT);
		graphView.getGraphViewStyle().setTextSize(dpToPixals(10));
		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			@Override
			public String formatLabel(double value, boolean isXValue) {
				int index = (int) Math.round(value);
				if (index >= result.length) {
					index = result.length - 1;
				}
				if (isXValue) {
					return result2[index];

				} else {
					NumberFormat formatter = NumberFormat.getNumberInstance();
					formatter.setMaximumFractionDigits(0);
					return formatter.format(value);
				}
			}
		});
		int dpAsPixels = dpToPixals(4);
		graphView.setScalable(true);
		graphView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

		GraphViewDataInterface[] points = new GraphViewDataInterface[downloadResult.length];
		for (int i = 0; i < downloadResult.length; i++) {
			points[i] = new GraphViewData(i, downloadResult[i]);
		}
		graphView.addSeries(new GraphViewSeries(skillName,
				new GraphViewSeriesStyle(getResources().getColor(R.color.green_text_color), dpToPixals(1)), points));
		mGraphHolder.addView(graphView);

	}

	private int dpToPixals(int dp) {
		float scale = getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (dp * scale + 0.5f);
		return dpAsPixels;
	}

}
