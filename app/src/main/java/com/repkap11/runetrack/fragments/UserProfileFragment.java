/**
 * UserProfileFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.DataTableViewHolder;
import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;
import com.repkap11.runetrack.TextDrawable;

import java.util.ArrayList;
import java.util.Locale;

public class UserProfileFragment extends FragmentBase {

private static final String TAG = UserProfileFragment.class.getSimpleName();
public ArrayList<Parcelable> downloadResult;
private ListView mList;
private String userName;

public UserProfileFragment() {
	// Empty constructor required for fragment subclasses
}

public void reloadData() {
	((MainActivity) this.getActivity()).selectUserProfileByName(userName);
}

@Override
protected Intent requestDownload() {
	Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
	msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
	msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_USER_PROFILE_TABLE);
	return msgIntent;
}

@Override
public Drawable onInflateContentView(ViewGroup container) {
	LayoutInflater inflater = LayoutInflater.from(this.getActivity());
	//Log.e(TAG, "On create view called userprofile fragment");
	TextDrawable result = new TextDrawable(getResources(), R.string.ERROR_CODE_UNKNOWN);
	View rootView = inflater.inflate(R.layout.fragment_content_shared_list, container, true);
	userName = getArguments().getString(MainActivity.ARG_USERNAME);
	mList = ((ListView) rootView.findViewById(R.id.fragment_content_shared_list_list));
	getActivity().setTitle(userName);
	return result;
}

@Override
protected void applyDownloadResultFromIntent(Bundle bundle) {
	downloadResult = bundle.getParcelableArrayList(DownloadIntentService.PARAM_USERNAME);
	applyDownloadResult(downloadResult);

}

private void applyDownloadResult(ArrayList<Parcelable> result) {
	refreshComplete();
	if(result == null) {
		//Log.e(TAG, "Result Null");
	}else if(this.getActivity() == null) {
		//Log.e(TAG, "Activity is Null");
	}else {
		//Log.e(TAG, "All good, neither null");
	}
	mList.setAdapter(new ArrayAdapter<Parcelable>(this.getActivity(), 0, result) {
		private DataTableBounds bounds;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(bounds == null) {
				bounds = calculateLayoutSize(this, getActivity(), (ListView) parent);
			}
			View returnView = null;
			ImageView skillIcon;
			ArrayList<View> outVar;
			if(convertView != null) {
				returnView = convertView;
				DataTableViewHolder holder = (DataTableViewHolder) returnView.getTag();
				outVar = holder.mTextViews;
				skillIcon = holder.mImageView;
			}else {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				returnView = inflater.inflate(R.layout.fragment_table_row_user_profile, mList, false);
				skillIcon = (ImageView) returnView.findViewById(R.id.fragment_table_skill_image);
				View holderOfStrings = returnView.findViewById(R.id.text_lin_layout);
				outVar = new ArrayList<View>();
				holderOfStrings.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
				returnView.setTag(new DataTableViewHolder(skillIcon, outVar));
			}

			if(!((DataTable) this.getItem(position)).mListOfItems.get(0).equals("")) {
				Drawable imageIcon = getResources().getDrawable(getResources().getIdentifier(((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault()), "drawable", getActivity().getPackageName()));
				skillIcon.setImageDrawable(imageIcon);
			}else {
				skillIcon.setImageResource(android.R.color.transparent);

			}

			ArrayList<String> skill = ((DataTable) this.getItem(position)).mListOfItems;
			//Log.e(TAG,"Number of textElements:"+outVar.size());
			for(int m = 1; m < skill.size(); m++) {
				TextView skillName = (TextView) outVar.get(outVar.size() - m);
				int dim = 0;
				if(position == 0) {
					int dimTemp = 0;
					String realText = "";
					if(m == 1) {
						dimTemp = (bounds.totals[0] + bounds.totals[1] + bounds.totals[2]);
						realText = "Current Runescape Stats";
					}
					if(m == 4) {
						dimTemp = (bounds.totals[3] + bounds.totals[4]);
						realText = "Today";
					}
					if(m == 6) {
						dimTemp = (bounds.totals[5] + bounds.totals[6]);
						realText = "This Week";
					}
					if(dimTemp != 0) {
						dim = ((bounds.width) / (bounds.total + IMAGE_CHAR_SIZE) * dimTemp);
						realText = String.format("%1$" + dimTemp + "s", realText);
						skillName.setText(realText);
					}else {
						skillName.setText("");
					}

				}else {
					dim = (int) ((bounds.width) / (bounds.total + IMAGE_CHAR_SIZE) * (bounds.totals[m - 1]));
					// Log.e(TAG,"M:"+m);
					String realText = String.format("%1$" + bounds.totals[m - 1] + "s", skill.get(m));
					skillName.setText(realText);
				}
				skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
				skillName.setWidth(dim);
				if(m >= 4) {
					if(position > 1 && !skill.get(m).equals("0") && !skill.get(m).equals("?"))

					{
						skillName.setTextColor(getResources().getColor(R.color.green_text_color));
					}else {
						skillName.setTextColor(getResources().getColor(R.color.dark_text_color));
					}
				}
			}
			return returnView;
		}
	});
}
}
