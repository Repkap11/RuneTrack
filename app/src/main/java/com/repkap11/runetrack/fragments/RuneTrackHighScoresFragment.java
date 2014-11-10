/**
 * UserProgressFragment.java
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;
import com.repkap11.runetrack.TextDrawable;

import java.util.ArrayList;
import java.util.Locale;

public class RuneTrackHighScoresFragment extends FragmentBase {

public static final String TAG = "RuneTrackHighScoresFragment";
public ArrayList<Parcelable> downloadResult;
private ListView mProgressHolder;
private int pageNumber;
private String skillName;

public RuneTrackHighScoresFragment() {
	// Empty constructor required for fragment subclasses
}

@Override
public void reloadData() {
	((MainActivity) this.getActivity()).selectRuneTrackHighScores(skillName, pageNumber);
}

@Override
protected Intent requestDownload() {
	Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
	msgIntent.putExtra(DownloadIntentService.PARAM_PAGE_NUMBER, pageNumber);
	msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NAME, skillName);
	msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_RUNETRACK_HIGH_SCORES);
	return msgIntent;
}

@Override
public Drawable onInflateContentView(ViewGroup container) {
	LayoutInflater inflater = LayoutInflater.from(this.getActivity());
	//TODO change to its own layout?
	TextDrawable result = new TextDrawable(getResources(), R.string.highscores_download_error_message);
	View rootView = inflater.inflate(R.layout.fragment_content_shared_list, container, true);
	skillName = getArguments().getString(MainActivity.ARG_SKILL_NAME);
	pageNumber = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);
	mProgressHolder = ((ListView) rootView.findViewById(R.id.fragment_content_shared_list_list));
	getActivity().setTitle(getResources().getString(R.string.runetrack_highscores));
	return result;
}

@Override
protected void applyDownloadResultFromIntent(Bundle bundle) {
	downloadResult = bundle.getParcelableArrayList(DownloadIntentService.PARAM_HIGH_SCORES_ENTRIES);
	applyDownloadResult(downloadResult);
}

private void applyDownloadResult(ArrayList<Parcelable> result) {
	refreshComplete();
	mProgressHolder.setAdapter(new ArrayAdapter<Parcelable>(getActivity(), 0, result) {
		private DataTableBounds bounds;

		@Override
		public int getItemViewType(int position) {
			if(position == 0) {
				return 0;
			}
			if(position == 1) {
				return 1;
			}
			return 2;
		}

		@Override
		public int getViewTypeCount() {
			return 3;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Log.e(TAG,"Get view called for :"+position);
			if(position == 0) {
				if(convertView != null) {
					return convertView;
				}else {
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.fragment_table_row_highscores_next_previous, mProgressHolder, false);
					Button next = (Button) convertView.findViewById(R.id.table_row_highscore_next_button);
					Button previous = (Button) convertView.findViewById(R.id.table_row_highscore_previous_button);
					if(RuneTrackHighScoresFragment.this.pageNumber == 1) {
						previous.setVisibility(View.GONE);
					}
					next.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							RuneTrackHighScoresFragment.this.pageNumber++;
							reloadData();
						}
					});
					previous.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							RuneTrackHighScoresFragment.this.pageNumber--;
							reloadData();
						}
					});

					return convertView;
				}
			}
			if(position == 1 && convertView != null) {
				return convertView;
			}
			if(convertView == null) {
				ImageView skillIcon;
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.fragment_table_row_high_score, mProgressHolder, false);
				skillIcon = (ImageView) convertView.findViewById(R.id.fragment_table_skill_image);
				if(!((DataTable) (this.getItem(position))).mListOfItems.get(0).equals("") && position != 1) {
					//String iconName = ((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault());
					// Log.e(TAG,"Loading Icon: "+iconName);
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
			View holderOfStrings = convertView.findViewById(R.id.text_lin_layout);
			ArrayList<View> outVar = new ArrayList<View>();
			holderOfStrings.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
			for(int m = 1; m < skill.size(); m++) {
				final TextView skillName = (TextView) outVar.get(outVar.size() - m);
				int dim = (int) ((bounds.width) / (bounds.total + IMAGE_CHAR_SIZE) * (bounds.totals[m - 1]));
				final String realText = String.format("%1$" + bounds.totals[m - 1] + "s", skill.get(m));
				// realText = realText.replace(' ', '%');
				skillName.setText(realText);
				if(m == 1) {
					skillName.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							((MainActivity) RuneTrackHighScoresFragment.this.getActivity()).selectUserProfileByName(realText);
						}
					});
				}
				// }
				skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
				skillName.setWidth(dim);
			}
			return convertView;
		}

	});
}
}