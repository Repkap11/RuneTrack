/**
 * UserProfileFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;

public class UserProfileFragment extends FragmentBase {

	private static final String TAG = "UserProfileFragment";

	private ListView mList;
	private ResponseReceiver receiver;
	public ArrayList<Parcelable> downloadResult;
	private boolean needsDownload = true;
	private boolean needsToShowDownloadFailure = false;
	private String userName;
	private ViewSwitcher switcherFailure;
	private ViewSwitcher switcherContent;
	private TextView failureRetryButton;

	public UserProfileFragment() {
		// Empty constructor required for fragment subclasses
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_profile, container, false);
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
		mList = ((ListView) rootView.findViewById(R.id.user_profile_content));
		getActivity().setTitle(userName);
		needsDownload = true;
	
		if (savedInstanceState != null) {
	
			needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
			Log.e(TAG, "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
			downloadResult = savedInstanceState.getParcelableArrayList(DownloadIntentService.PARAM_USERNAME);
			if (downloadResult != null) {
				needsDownload = false;
				applyDownloadResult(downloadResult);
			}
		}
		return rootView;
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
			msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_USER_PROFILE_TABLE);
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

	private void failureRetryOnClick(View v) {
		((MainActivity) this.getActivity()).selectUserProfileByName(userName);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (downloadResult != null) {
			outState.putParcelableArrayList(DownloadIntentService.PARAM_USERNAME, downloadResult);
			outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
		}
	}

	public class ResponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Log.e(TAG, "before crash");
			downloadResult = intent.getParcelableArrayListExtra(DownloadIntentService.PARAM_USERNAME);
			if (downloadResult == null || downloadResult.size() == 0) {
				switcherContent.setDisplayedChild(0);
				switcherFailure.setDisplayedChild(1);
				needsToShowDownloadFailure = true;
				// Toast.makeText(UserProfileFragment.this.getActivity(),
				// "Failure", Toast.LENGTH_SHORT).show();
			} else {
				DataTable topHeader = new DataTable(new ArrayList<String>(Arrays.asList(new String[] { "", "Curnt ", "Runescape",
						"Stats", "Today", "", "This", "Week" })));
				DataTable header = new DataTable(new ArrayList<String>(Arrays.asList(new String[] { "", "Level", "Xp", "Rank", "Lvls",
						"Xp", "Lvls", "Xp" })));
				downloadResult.add(0, topHeader);
				downloadResult.add(1, header);
				switcherContent.setDisplayedChild(1);
				applyDownloadResult(downloadResult);
			}
		}
	}

	public void applyDownloadResult(ArrayList<Parcelable> result) {
		if (result == null) {
			Log.e(TAG, "Result Null");
		} else if (this.getActivity() == null) {
			Log.e(TAG, "Activity is Null");
		} else {
			Log.e(TAG, "All good, neither null");
		}
		mList.setAdapter(new ArrayAdapter<Parcelable>(this.getActivity(), R.layout.user_profile_holder, result) {
			private DataTableBounds bounds;

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View returnView = null;
				ImageView skillIcon;
				if (convertView != null) {
					returnView = convertView;

				} else {
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					returnView = inflater.inflate(R.layout.user_profile_skill, mList, false);
					// skillIcon.setAdjustViewBounds(true);
					// skillIcon.setMaxWidth((int) (bounds.imageSize ));//*
					// getResources().getDisplayMetrics().density)
					// skillIcon.setMinimumWidth((int) (bounds.imageSize));
				}
				skillIcon = (ImageView) returnView.findViewById(R.id.user_profile_skill_image);
				if (bounds == null)
					bounds = calculateLayoutSize(this, getActivity(), (ListView) parent);
				ArrayList<String> skill = ((DataTable) this.getItem(position)).mListOfItems;
				if (!((DataTable) this.getItem(position)).mListOfItems.get(0).equals("")) {
					Drawable imageIcon = getResources().getDrawable(
							getResources().getIdentifier(
									((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault()), "drawable",
									getActivity().getPackageName()));
					skillIcon.setImageDrawable(imageIcon);
				} else {
					skillIcon.setImageResource(android.R.color.transparent);

				}

				View holderOfStrings = returnView.findViewById(R.id.text_lin_layout);
				ArrayList<View> outVar = new ArrayList<View>();
				holderOfStrings.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
				for (int m = 1; m < skill.size(); m++) {
					TextView skillName = (TextView) outVar.get(m - 1);
					int dim = 0;
					if (position == 0) {
						int dimTemp = 0;
						String realText = "";
						if (m == 1) {
							dimTemp = (bounds.totals[0] + bounds.totals[1] + bounds.totals[2]);
							realText = "Current Runescape Stats";
						}
						if (m == 4) {
							dimTemp = (bounds.totals[3] + bounds.totals[4]);
							realText = "Today";
						}
						if (m == 6) {
							dimTemp = (bounds.totals[5] + bounds.totals[6]);
							realText = "This Week";
						}
						if (dimTemp != 0) {
							dim = ((bounds.width) / (bounds.total + 2) * dimTemp);
							realText = String.format("%1$" + dimTemp + "s", realText);
							skillName.setText(realText);
						} else {
							skillName.setText("");
						}

					} else {
						dim = (int) ((bounds.width) / (bounds.total + 2) * (bounds.totals[m - 1]));
						// Log.e(TAG,"M:"+m);
						String realText = String.format("%1$" + bounds.totals[m - 1] + "s", skill.get(m));
						skillName.setText(realText);
					}
					skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
					skillName.setWidth(dim);
					if (m >= 4) {
						if (position > 1 && !skill.get(m).equals("0") && !skill.get(m).equals("?"))

							skillName.setTextColor(getResources().getColor(R.color.green_text_color));
						else
							skillName.setTextColor(getResources().getColor(R.color.dark_text_color));
					}
				}
				return returnView;
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
		}
	}

	@Override
	public void onDetach() {
		Log.e(TAG, "Fragment Detached");
		super.onDetach();
	}
}
