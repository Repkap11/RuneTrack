/**
 * UserProfileFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import java.util.ArrayList;
import java.util.Arrays;
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
import android.widget.*;

/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public class UserProfileFragment extends Fragment {

	private static final String TAG = "UserProfileFragment";

	@Override
	public void onDetach() {
		Log.e(TAG, "Fragment Detached");
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
				UserProfileSkill topHeader = new UserProfileSkill(new ArrayList<String>(Arrays.asList(new String[] { "", "Curnt ", "Runescape",
						"Stats", "Today", "", "This", "Week" })));
				UserProfileSkill header = new UserProfileSkill(new ArrayList<String>(Arrays.asList(new String[] { "", "Level", "Xp", "Rank", "Lvls",
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
			private UserProfileBounds bounds;

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
				ArrayList<String> skill = ((UserProfileSkill) this.getItem(position)).mListOfItems;
				if (!((UserProfileSkill) this.getItem(position)).mListOfItems.get(0).equals("")) {
					Drawable imageIcon = getResources().getDrawable(
							getResources().getIdentifier(
									((UserProfileSkill) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault()), "drawable",
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

	public static UserProfileBounds calculateLayoutSize(ArrayAdapter<Parcelable> arrayAdapter, Context context, ListView view) {
		// Log.e(TAG, "Calculating Bounds");
		Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point p = new Point();
		d.getSize(p);
		int oldwidth = p.x;
		int width = oldwidth - view.getPaddingLeft() - view.getPaddingRight() - ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin
				- ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
		// Toast.makeText(UserProfileFragment.this.getActivity(),"old:"+oldwidth+" new:"+width,Toast.LENGTH_SHORT).show();
		int total = 0;
		int[] totals = new int[((UserProfileSkill) arrayAdapter.getItem(0)).mListOfItems.size() - 1];
		for (int i = 1; i < totals.length + 1; i++) {
			int max = 0;
			for (int j = 0; j < arrayAdapter.getCount(); j++) {
				int curSize = ((UserProfileSkill) arrayAdapter.getItem(j)).mListOfItems.get(i).length();
				if (max < curSize) {
					max = curSize;
				}
			}

			totals[i - 1] = max + 1;// +1 for padding text with a space
			//Log.e(TAG,"Totals["+(i-1)+"] = "+totals[i-1]);
			total += max + 1;
		}
		// Log.e(TAG,"Total"+" = "+total);
		int imageSize = (int) ((width) / (total + 2) * 2);// image takes
															// two
															// spaces
															// worth of
															// size
		float textSize = refitText(" ", (width) / (total + 2) * 1);
		return new UserProfileBounds(imageSize, totals, total, width, textSize);
	}

	public static float refitText(String text, int textWidth) {
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

	public static int dpToPixals(Context context,int dp) {
		float scale = context.getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (dp * scale + 0.5f);
		return dpAsPixels;
	}
}
