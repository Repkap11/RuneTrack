/**
 * HistoryGraphFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;

/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public class HistoryGraphFragment extends FragmentBase {

	public static final String TAG = "HistoryGraphFragment";

	@Override
	public void onDetach() {
		Log.e(TAG, "Fragment Detached");
		super.onDetach();
	}

	private ListView mProgressHolder;
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
	public ArrayList<Parcelable> downloadResult3;

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
		// Log.e(TAG, "onResume needsDownloadFailure " +
		// needsToShowDownloadFailure);
		// Log.e(TAG, "onResume needsDownload " + needsDownload);
		if (needsDownload) {
			switcherContent.setDisplayedChild(0);
			Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
			msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
			msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NUMBER, skillNumber);
			msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NAME, skillName);
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
			outState.putParcelableArrayList(DownloadIntentService.PARAM_PROGRESS_ENTRIES, downloadResult3);
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
		skillName = getArguments().getString(MainActivity.ARG_SKILL_NAME);
		skillNumber = getArguments().getInt(MainActivity.ARG_SKILL_NUMBER);
		mProgressHolder = ((ListView) rootView.findViewById(R.id.history_graph_content));
		getActivity().setTitle(userName);
		needsDownload = true;

		if (savedInstanceState != null) {

			needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
			Log.e(TAG, "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
			downloadResult = savedInstanceState.getDoubleArray(DownloadIntentService.PARAM_USERNAME);
			downloadResult2 = savedInstanceState.getStringArray(DownloadIntentService.PARAM_USER_PROFILE_TABLE);
			downloadResult3 = savedInstanceState.getParcelableArrayList(DownloadIntentService.PARAM_PROGRESS_ENTRIES);
			if (downloadResult != null) {
				needsDownload = false;
				applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
			}
		}
		return rootView;
	}

	public class ResponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Log.e(TAG, "before crash");
			downloadResult = intent.getDoubleArrayExtra(DownloadIntentService.PARAM_USER_PROFILE_TABLE);
			downloadResult2 = intent.getStringArrayExtra(DownloadIntentService.PARAM_USER_PROFILE_TABLE2);
			downloadResult3 = intent.getParcelableArrayListExtra(DownloadIntentService.PARAM_PROGRESS_ENTRIES);
			if (downloadResult == null || downloadResult3 == null || downloadResult.length == 0 || downloadResult3.size() == 0) {
				Log.e(TAG, "downloadResult3:" + downloadResult3);
				switcherContent.setDisplayedChild(0);
				switcherFailure.setDisplayedChild(1);
				needsToShowDownloadFailure = true;
				// Toast.makeText(HistoryGraphFragment.this.getActivity(),
				// "Failure", Toast.LENGTH_SHORT).show();
			} else {
				downloadResult3.add(
						0,
						new DataTable(new ArrayList<String>(Arrays
								.asList(new String[] { "", "#", "Date", "Rank", "Level", "Xp", "Xp Gained" }))));
				downloadResult3.add(0, new DataTable(new ArrayList<String>(Arrays.asList(new String[] { "", "", "", "", "", "", "" }))));
				downloadResult3.add(0, new DataTable(new ArrayList<String>(Arrays.asList(new String[] { "", "", "", "", "", "", "" }))));
				switcherContent.setDisplayedChild(1);
				applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
			}
		}
	}

	public void applyDownloadResult(final double[] result, final String[] result2, ArrayList<Parcelable> result3) {
		mProgressHolder.setAdapter(new ArrayAdapter<Parcelable>(getActivity(), R.layout.history_graph, result3) {
			private DataTableBounds bounds;

			@Override
			public int getItemViewType(int position) {
				if (position == 0) {
					return 0;
				}
				if (position == 1) {
					return 1;
				}
				if (position == 2) {
					return 2;
				}
				return 3;
			}

			@Override
			public int getViewTypeCount() {
				return 4;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Log.e(TAG,"Get view called for :"+position);
				if (position == 0) {
					return getGraphHeader(convertView);
				}
				if (position == 1) {
					return getGraphView(convertView, result, result2);
				}

				if (convertView == null) {
					ImageView skillIcon;
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.progress_entry, mProgressHolder, false);
					skillIcon = (ImageView) convertView.findViewById(R.id.progress_skill_image);
					if (!((DataTable) (this.getItem(position))).mListOfItems.get(0).equals("")) {
						Drawable imageIcon = getResources().getDrawable(
								getResources().getIdentifier(
										((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault()), "drawable",
										getActivity().getPackageName()));
						skillIcon.setImageDrawable(imageIcon);
					} else {
						skillIcon.setImageResource(android.R.color.transparent);

					}

				}
				if (bounds == null) {
					bounds = calculateLayoutSize(this, getActivity(), (ListView) parent);
				}
				ArrayList<String> skill = ((DataTable) this.getItem(position)).mListOfItems;
				View holderOfStrings = convertView.findViewById(R.id.text_lin_layout);
				ArrayList<View> outVar = new ArrayList<View>();
				holderOfStrings.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
				for (int m = 1; m < skill.size(); m++) {
					TextView skillName = (TextView) outVar.get(m - 1);
					int dim = (int) ((bounds.width) / (bounds.total + 2) * (bounds.totals[m - 1]));
					// Log.e(TAG,"M:"+m);
					String realText = String.format("%1$" + bounds.totals[m - 1] + "s", skill.get(m));
					skillName.setText(realText);
					// }
					skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
					skillName.setWidth(dim);
				}
				return convertView;
			}

		});
	}

	protected View getGraphHeader(View convertView) {
		if (convertView != null) {
			return convertView;
		}

		// LayoutInflater inflater = (LayoutInflater)
		// getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView returnView = new TextView(getActivity());
		// LinearLayout returnView = (LinearLayout)
		// inflater.inflate(R.layout.grid_view_holder, mProgressHolder, false);
		returnView.setText(userName + "'s Skill History in " + skillName);
		returnView.setGravity(Gravity.CENTER_HORIZONTAL);
		returnView.setTextColor(Color.BLACK);
		return returnView;
	}

	protected View getGraphView(View convertView, final double[] result, final String[] result2) {
		if (convertView != null) {
			return convertView;
		}

		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout returnView = (LinearLayout) inflater.inflate(R.layout.grid_view_holder, mProgressHolder, false);

		final GraphView graphView = new LineGraphView(this.getActivity(), "");
		graphView.getGraphViewStyle().setGridColor(Color.WHITE);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Align.RIGHT);
		graphView.getGraphViewStyle().setTextSize(dpToPixals(getActivity(), 10));
		graphView.getGraphViewStyle().setNumVerticalLabels(10);
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

		GraphViewDataInterface[] points = new GraphViewDataInterface[downloadResult.length];
		for (int i = 0; i < downloadResult.length; i++) {
			points[i] = new GraphViewData(i, downloadResult[i]);
		}
		graphView.addSeries(new GraphViewSeries(skillName, new GraphViewSeriesStyle(getResources().getColor(R.color.green_text_color),
				dpToPixals(getActivity(), 1)), points));
		returnView.addView(graphView);
		return returnView;
	}

}
