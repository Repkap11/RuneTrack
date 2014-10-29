/**
 * UserProgressFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.graphics.drawable.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.jjoe64.graphview.*;
import com.jjoe64.graphview.GraphView.*;
import com.jjoe64.graphview.GraphViewSeries.*;
import com.repkap11.runetrack.*;

import java.text.*;
import java.util.*;

/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public class UserProgressFragment extends FragmentBase {

public static final String TAG = "UserProgressFragment";
public double[] downloadResult;
public String[] downloadResult2;
public ArrayList<Parcelable> downloadResult3;
private ListView mProgressHolder;
private ResponseReceiver receiver;
private boolean needsDownload = true;
private boolean needsToShowDownloadFailure = false;
private String userName;
private int skillNumber;
private String skillName;

public UserProgressFragment() {
	// Empty constructor required for fragment subclasses
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
		msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NAME, skillName);
		msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_HISTORY_GRAPH);
		this.getActivity().startService(msgIntent);
		IntentFilter filter = new IntentFilter(DownloadIntentService.PARAM_USERNAME);

		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new ResponseReceiver();
		getActivity().registerReceiver(receiver, filter);

	}else {

		if(needsToShowDownloadFailure) {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
		}else {
			setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
		}
	}
}

@Override
public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	if(downloadResult != null) {
		outState.putDoubleArray(DownloadIntentService.PARAM_USERNAME, downloadResult);
		outState.putStringArray(DownloadIntentService.PARAM_USER_PROFILE_TABLE, downloadResult2);
		outState.putParcelableArrayList(DownloadIntentService.PARAM_PROGRESS_ENTRIES, downloadResult3);
		outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
	}
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
	((MainActivity) this.getActivity()).selectHistoryGraph(userName, skillNumber, skillName);
}

@Override
public boolean canScrollUp() {
	return mProgressHolder.canScrollVertically(-1);
}

@Override
public Drawable onInflateContentView(ViewGroup container) {
	LayoutInflater inflater = LayoutInflater.from(this.getActivity());
	TextDrawable result = new TextDrawable(getResources(),R.string.history_graph_download_error_message);
	View rootView = inflater.inflate(R.layout.fragment_content_shared_list, container, true);
	userName = getArguments().getString(MainActivity.ARG_USERNAME);
	skillName = getArguments().getString(MainActivity.ARG_SKILL_NAME);
	skillNumber = getArguments().getInt(MainActivity.ARG_SKILL_NUMBER);
	mProgressHolder = ((ListView) rootView.findViewById(R.id.fragment_content_shared_list_list));
	mProgressHolder.setScrollingCacheEnabled(false);
	mProgressHolder.setAnimationCacheEnabled(false);
	getActivity().setTitle(userName);
	needsDownload = true;

	if(mSavedInstanceState != null) {

		needsToShowDownloadFailure = mSavedInstanceState.getBoolean("needsToShowDownloadFailure");
		Log.e(TAG, "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
		downloadResult = mSavedInstanceState.getDoubleArray(DownloadIntentService.PARAM_USERNAME);
		downloadResult2 = mSavedInstanceState.getStringArray(DownloadIntentService.PARAM_USER_PROFILE_TABLE);
		downloadResult3 = mSavedInstanceState.getParcelableArrayList(DownloadIntentService.PARAM_PROGRESS_ENTRIES);
		if(downloadResult != null) {
			needsDownload = false;
			applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
		}
	}
	return result;
}

public void applyDownloadResult(final double[] result, final String[] result2, ArrayList<Parcelable> result3) {
	refreshComplete();
	mProgressHolder.setAdapter(new ArrayAdapter<Parcelable>(getActivity(), 0, result3) {
		private DataTableBounds bounds;

		@Override
		public int getItemViewType(int position) {
			if(position == 0) {
				return 0;
			}
			if(position == 1) {
				return 1;
			}
			if(position == 2) {
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
			if(position == 0) {
				return getGraphHeader(convertView);
			}
			if(position == 1) {
				return getGraphView(convertView, result, result2);
			}

			View returnView;
			ImageView skillIcon;
			ArrayList<View> outVar;
			if(convertView != null) {
				returnView = convertView;
				DataTableViewHolder holder = (DataTableViewHolder) returnView.getTag();
				outVar = holder.mTextViews;
				skillIcon = holder.mImageView;
			}else {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				returnView = inflater.inflate(R.layout.fragment_table_row_user_progress, mProgressHolder, false);
				skillIcon = (ImageView) returnView.findViewById(R.id.fragment_table_skill_image);
				outVar = new ArrayList<View>();
				returnView.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
				returnView.setTag(new DataTableViewHolder(skillIcon, outVar));
			}
			if(convertView == null) {
				if(!((DataTable) (this.getItem(position))).mListOfItems.get(0).equals("")) {
					Drawable imageIcon = getResources().getDrawable(getResources().getIdentifier(((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault()), "drawable", getActivity().getPackageName()));
					skillIcon.setImageDrawable(imageIcon);
				}else {
					skillIcon.setImageResource(android.R.color.transparent);

				}

			}

			if(bounds == null) {
				bounds = calculateLayoutSize(this, getActivity(), (ListView) parent);
			}
			ArrayList<String> skill = ((DataTable) this.getItem(position)).mListOfItems;
			for(int m = 1; m < skill.size(); m++) {
				TextView skillName = (TextView) outVar.get(outVar.size() - m);
				int dim = (int) ((bounds.width) / (bounds.total + IMAGE_CHAR_SIZE) * (bounds.totals[m - 1]));
				// Log.e(TAG,"M:"+m);
				String realText = String.format("%1$" + bounds.totals[m - 1] + "s", skill.get(m));
				skillName.setText(realText);
				// }
				skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
				skillName.setWidth(dim);
			}
			return returnView;
		}

	});
}

protected View getGraphHeader(View convertView) {
	if(convertView != null) {
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
	if(convertView != null) {
		return convertView;
	}

	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	LinearLayout returnView = (LinearLayout) inflater.inflate(R.layout.fragment_table_row_user_progress_graph_view_container, mProgressHolder, false);

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
			if(index >= result.length) {
				index = result.length - 1;
			}
			if(isXValue) {
				return result2[index];

			}else {
				NumberFormat formatter = NumberFormat.getNumberInstance();
				formatter.setMaximumFractionDigits(0);
				return formatter.format(value);
			}
		}
	});

	GraphViewDataInterface[] points = new GraphViewDataInterface[downloadResult.length];
	for(int i = 0; i < downloadResult.length; i++) {
		points[i] = new GraphViewData(i, downloadResult[i]);
	}
	graphView.addSeries(new GraphViewSeries(skillName, new GraphViewSeriesStyle(getResources().getColor(R.color.green_text_color), dpToPixals(getActivity(), 1)), points));
	returnView.addView(graphView);
	return returnView;
}

public class ResponseReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.e(TAG, "before crash");
		downloadResult = intent.getDoubleArrayExtra(DownloadIntentService.PARAM_USER_PROFILE_TABLE);
		downloadResult2 = intent.getStringArrayExtra(DownloadIntentService.PARAM_USER_PROFILE_TABLE2);
		downloadResult3 = intent.getParcelableArrayListExtra(DownloadIntentService.PARAM_PROGRESS_ENTRIES);
		if(downloadResult == null || downloadResult3 == null || downloadResult.length == 0 || downloadResult3.size() == 0) {
			Log.e(TAG, "downloadResult3:" + downloadResult3);
			setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
			needsToShowDownloadFailure = true;
			// Toast.makeText(UserProgressFragment.this.getActivity(),
			// "Failure", Toast.LENGTH_SHORT).show();
		}else {
			downloadResult3.add(0, new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "#", "Date", "Rank", "Level", "Xp", "Xp Gained"}))));
			downloadResult3.add(0, new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "", "", "", "", "", ""}))));
			downloadResult3.add(0, new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "", "", "", "", "", ""}))));
			setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
			applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
		}
	}
}

}
