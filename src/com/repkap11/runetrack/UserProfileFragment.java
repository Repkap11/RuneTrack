/**
 * UserProfileFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public class UserProfileFragment extends Fragment {

	@Override
	public void onDetach() {
		Log.e("Paul", "Fragment Detached");
		super.onDetach();
	}

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
	public void onPause() {
		super.onPause();
		if (receiver != null) {
			getActivity().unregisterReceiver(receiver);
		}
	}

	private void failureRetryOnClick(View v) {
		((MainActivity) this.getActivity()).selectUserProfileByName(userName);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (downloadResult != null) {
			outState.putParcelableArrayList(DownloadIntentService.PARAM_USERNAME, downloadResult);
			outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
		}
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
		mList = ((ListView) rootView.findViewById(R.id.content));
		getActivity().setTitle(userName);
		needsDownload = true;

		if (savedInstanceState != null) {

			needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
			Log.e("Paul", "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
			downloadResult = savedInstanceState.getParcelableArrayList(DownloadIntentService.PARAM_USERNAME);
			if (downloadResult != null) {
				needsDownload = false;
				applyDownloadResult(downloadResult);
			}
		}
		return rootView;
	}

	public class ResponseReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Log.e("Paul", "before crash");
			downloadResult = intent.getParcelableArrayListExtra(DownloadIntentService.PARAM_USERNAME);
			if (downloadResult == null || downloadResult.size() == 0) {
				switcherContent.setDisplayedChild(0);
				switcherFailure.setDisplayedChild(1);
				needsToShowDownloadFailure = true;
				Toast.makeText(UserProfileFragment.this.getActivity(), "Failure", Toast.LENGTH_SHORT).show();
			} else {
				UserProfileSkill topHeader = new UserProfileSkill("", "Curnt ", "Runescape", "Stats", "Today", "", "This", "Week");
				UserProfileSkill header = new UserProfileSkill("", "Level", "Xp", "Rank", "Lvls", "Xp", "Lvls", "Xp");
				downloadResult.add(0, topHeader);
				downloadResult.add(1, header);
				switcherContent.setDisplayedChild(1);
				applyDownloadResult(downloadResult);
			}
		}
	}

	public void applyDownloadResult(ArrayList<Parcelable> result) {
		if (result == null) {
			Log.e("Paul", "Result Null");
		} else if (this.getActivity() == null) {
			Log.e("Paul", "Activity is Null");
		} else {
			Log.e("Paul", "All good, neither null");
		}
		mList.setAdapter(new ArrayAdapter<Parcelable>(this.getActivity(), R.layout.user_profile_holder, result) {
			private UserProfileBounds bounds;

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (bounds == null)
					bounds = calculateLayoutSize();
				View returnView = null;
				ImageView skillIcon;
				if (convertView != null) {
					returnView = convertView;
					skillIcon = (ImageView) returnView.findViewById(R.id.skill_image);
				} else {
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					returnView = inflater.inflate(R.layout.user_profile_skill, mList, false);
					skillIcon = (ImageView) returnView.findViewById(R.id.skill_image);
					skillIcon.setAdjustViewBounds(true);
					skillIcon.setMaxWidth(bounds.imageSize * DisplayMetrics.DENSITY_DEFAULT);
					skillIcon.setMinimumWidth(bounds.imageSize * DisplayMetrics.DENSITY_DEFAULT);
				}
				ArrayList<String> skill = ((UserProfileSkill) this.getItem(position)).mListOfItems;
				if (!((UserProfileSkill) this.getItem(position)).skillName.equals("")) {
					Drawable imageIcon = getResources().getDrawable(
							getResources().getIdentifier(((UserProfileSkill) this.getItem(position)).skillName.toLowerCase(Locale.getDefault()),
									"drawable", getActivity().getPackageName()));
					skillIcon.setImageDrawable(imageIcon);
				} else {
					skillIcon.setImageResource(android.R.color.transparent);

				}

				View holderOfStrings = returnView.findViewById(R.id.text_lin_layout);

				for (int m = 0; m < skill.size(); m++) {
					ArrayList<View> outVar = new ArrayList<View>();
					holderOfStrings.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
					TextView skillName = (TextView) outVar.get(m);
					int dim = 0;
					if (position == 0) {
						int dimTemp = 0;
						String realText = "";
						if (m == 0) {
							dimTemp = (bounds.totals[0] + bounds.totals[1] + bounds.totals[2]);
							realText = "Current Runescape Stats";
						}
						if (m == 3) {
							dimTemp = (bounds.totals[3] + bounds.totals[4]);
							realText = "Today";
						}
						if (m == 5) {
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
						dim = (int) ((bounds.width) / (bounds.total + 2) * (bounds.totals[m]));
						String realText = String.format("%1$" + bounds.totals[m] + "s", skill.get(m));
						skillName.setText(realText);
					}
					skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
					skillName.setWidth(dim);
					if (m >= 3) {
						if (position > 1 && !skill.get(m).equals("0") && !skill.get(m).equals("?"))

							skillName.setTextColor(getResources().getColor(R.color.green_text_color));
						else
							skillName.setTextColor(getResources().getColor(R.color.dark_text_color));
					}
				}
				return returnView;
			}

			private UserProfileBounds calculateLayoutSize() {
				Log.e("Paul", "Calculating Bounds");
				Display d = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
				Point p = new Point();
				d.getSize(p);
				int width = p.x;
				int total = 0;
				int[] totals = new int[((UserProfileSkill) this.getItem(0)).mListOfItems.size()];
				for (int i = 0; i < totals.length; i++) {
					int max = 0;
					for (int j = 0; j < this.getCount(); j++) {
						int curSize = ((UserProfileSkill) this.getItem(j)).mListOfItems.get(i).length();
						if (max < curSize) {
							max = curSize;
						}
					}
					totals[i] = max + 1;// +1 for padding text with a space
					total += max + 1;
				}
				int imageSize = (int) ((width) / (total + 2) * 2);// image takes
																	// two
																	// spaces
																	// worth of
																	// size
				float textSize = refitText(" ", (width) / (total + 2) * 1);
				return new UserProfileBounds(imageSize, totals, total, width, textSize);

			}

			private float refitText(String text, int textWidth) {
				if (textWidth <= 0)
					return 0;
				float hi = 100;
				float lo = 2;
				final float threshold = 0.5f; // How close we have to be
				Paint testPaint = new Paint();

				while ((hi - lo) > threshold) {
					float size = (hi + lo) / 2;
					testPaint.setTextSize(size);
					testPaint.setTypeface(Typeface.MONOSPACE);
					if (testPaint.measureText(text) >= textWidth)
						hi = size; // too big
					else
						lo = size; // too small
				}
				// Use lo so that we undershoot rather than overshoot
				return lo;
			}

		});
	}

}
