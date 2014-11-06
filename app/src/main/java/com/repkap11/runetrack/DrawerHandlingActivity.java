package com.repkap11.runetrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DrawerHandlingActivity extends Activity {
public static final String DRAWER_IS_USER = "DRAWER_IS_USER";
public static final String DRAWER_IS_RUNETRACK_HIGH_SCORES = "DRAWER_IS_RUNETRACK_HIGH_SCORES";
public static final String DRAWER_IS_ADD_USER = "DRAWER_IS_ADD_USER";
private static final String TAG = DrawerHandlingActivity.class.getSimpleName();
private static final String USER_PROFILE_NAMES = "USER_PROFILE_NAMES";
private DrawerLayout mDrawerLayout;
private ActionBarDrawerToggle mDrawerToggle;
private List<String> mUserNamesToShow;
private ExpandableListView mDrawerList;
private BaseExpandableListAdapter mAdapter;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
}

@Override
public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig);
	// Pass any configuration change to the drawer toggls
	mDrawerToggle.onConfigurationChanged(newConfig);
}

protected void onCreateAfterSetContentView(Bundle savedInstanceState) {
	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
	mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
	// set a custom shadow that overlays the main content when the drawer
	// opens
	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
	// set up the drawer's list view with items and click listener
	mAdapter = new BaseExpandableListAdapter() {

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			if(groupPosition == getGroupCount() - 1) {
				return false;
			}else {
				return true;
			}
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			// Log.e(TAG, "Getting view " + position);
			TextView view = (TextView) getLayoutInflater().inflate(R.layout.activity_main_side_drawer_list_item, parent, false);
			if(groupPosition == getGroupCount() - 1) {
				view.setText("Add New User");
				view.setTag(MainActivity.DRAWER_IS_ADD_USER);// is user
			}else if(groupPosition == 0) {
				view.setText(getString(R.string.runetrack_highscores));
				view.setTag(MainActivity.DRAWER_IS_RUNETRACK_HIGH_SCORES);// is
				// user
			}else {
				view.setText(mUserNamesToShow.get(groupPosition - 1));
				view.setTag(MainActivity.DRAWER_IS_USER);// is user
			}

			return view;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public int getGroupCount() {
			return mUserNamesToShow.size() + 2;// 1 for RuneTrack highscores
			// 1 for add user
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			if(groupPosition == getGroupCount() - 1) {
				return 0;
			}else if(groupPosition == 0) {
				return 0;
			}else {
				return 2;
			}
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			// Log.e(TAG, "Getting view " + position);
			if(convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.activity_main_side_drawer_list_sub_item, parent, false);
			}
			TextView view = (TextView) convertView.findViewById(R.id.drawer_list_sub_item_textview);
			view.setText(childPosition == 0 ? "     User Profile" : "      Weekly Xp Distribution");
			return view;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}
	};
	SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
	mUserNamesToShow = getStringArrayPref(prefs, USER_PROFILE_NAMES);
	if(mUserNamesToShow.size() == 0) {
		//Log.e(TAG, "Not using saved values");
		mUserNamesToShow = new ArrayList<String>(Arrays.asList(new String[]{"Repkap11", "Zezima", "Suomi", "Jake", "Drumgun", "Alkan"}));
	}
	mDrawerList.setOnChildClickListener(new DrawerChildClickListener());
	mDrawerList.setOnItemLongClickListener(new DrawerGroupLongClickListener());
	mDrawerList.setOnGroupClickListener(new DrawerGroupClickListener());
	if(getActionBar() != null) {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}
	mDrawerToggle = new ActionBarDrawerToggle(this, // host Activity
			mDrawerLayout, // DrawerLayout object
			R.string.drawer_open, // / "open drawer" description for
			// accessibility
			R.string.drawer_close // "close drawer" description for
			// accessibility
	) {
		public void onDrawerOpened(View drawerView) {
			//getSupportActionBar().setTitle(mDrawerTitle);

			invalidateOptionsMenu(); // creates call to
			// onPrepareOptionsMenu()
		}

		public void onDrawerClosed(View view) {
			//getSupportActionBar().setTitle(mTitle);
			invalidateOptionsMenu(); // creates call to
			// onPrepareOptionsMenu()
		}

		public void onDrawerStateChanged(int newState) {
			if(newState == DrawerLayout.STATE_DRAGGING) {
				invalidateOptionsMenu();
			}
		}
	};
	mDrawerList.setAdapter(mAdapter);
	mDrawerLayout.setDrawerListener(mDrawerToggle);

}

public static ArrayList<String> getStringArrayPref(SharedPreferences prefs, String key) {
	String json = prefs.getString(key, null);
	ArrayList<String> urls = new ArrayList<String>();
	if(json != null) {
		try {
			JSONArray a = new JSONArray(json);
			for(int i = 0; i < a.length(); i++) {
				String url = a.optString(i);
				urls.add(url);
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	return urls;
}

@Override
protected void onPostCreate(Bundle savedInstanceState) {
	super.onPostCreate(savedInstanceState);
	// Sync the toggle state after onRestoreInstanceState has occurred.
	mDrawerToggle.syncState();
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	if(mDrawerToggle.onOptionsItemSelected(item)) {
		return true;
	}
	return false;
}

protected String getFirstUserName() {
	return mUserNamesToShow.get(0);
}

public void selectRuneTrackHighScores(String skillName, int pageNumber) {
	mDrawerLayout.closeDrawer(mDrawerList);
}

public void selectPiChart(String userName) {
	mDrawerLayout.closeDrawer(mDrawerList);
}

public void selectHistoryGraph(String userName, int skillNumber, String skillName) {
	mDrawerLayout.closeDrawer(mDrawerList);
}

public void selectUserProfileByName(String userName) {
	mDrawerLayout.closeDrawer(mDrawerList);
}

private void savePreferences() {
	SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
	Set<String> set = new HashSet<String>();
	set.addAll(mUserNamesToShow);
	SharedPreferences.Editor editor = prefs.edit();
	editor.putStringSet(USER_PROFILE_NAMES, set);
	setStringArrayPref(editor, USER_PROFILE_NAMES, mUserNamesToShow);
	editor.apply();
}

public static void setStringArrayPref(SharedPreferences.Editor editor, String key, List<String> values) {
	JSONArray a = new JSONArray();
	for(int i = 0; i < values.size(); i++) {
		a.put(values.get(i));
	}
	if(!values.isEmpty()) {
		editor.putString(key, a.toString());
	}else {
		editor.putString(key, null);
	}
}

private class DrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {
	@Override
	public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
		//Log.e(TAG, "onGroupClick called");
		if(view.getTag() != null) {
			if(view.getTag().equals(DRAWER_IS_USER)) {
				return false;
			}else if(view.getTag().equals(DRAWER_IS_RUNETRACK_HIGH_SCORES)) {
				selectRuneTrackHighScores("Overall", 1);
			}else {
				AlertDialog.Builder builder = new AlertDialog.Builder(DrawerHandlingActivity.this);
				builder.setTitle("Enter Username");
				// Set up the input
				final EditText input = new EditText(DrawerHandlingActivity.this);
				input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				builder.setView(input);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String userName = input.getText().toString();
						mUserNamesToShow.add(userName);
						mAdapter.notifyDataSetChanged();
						savePreferences();
						selectUserProfileByName(userName);
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builder.show();
			}
			return true;
		}else {
			return false;
		}
	}
}

private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {
	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		//Log.e(TAG, "onChildClick called:" + groupPosition + ":" + childPosition);
		int selectPosition = groupPosition - 1;
		if(childPosition == 0) {
			selectUserProfileByName(mUserNamesToShow.get(selectPosition));// -1
			// for
			// runetrack
			// high
			// scores
			// being
			// first
		}
		if(childPosition == 1) {
			selectPiChart(mUserNamesToShow.get(selectPosition));
		}
		return true;

	}
}

private class DrawerGroupLongClickListener implements ListView.OnItemLongClickListener {

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view, final int position, long dontknow) {
		// Log.e(TAG, "onGroupLongClick called");
		if(view.getTag() != null) {
			if(view.getTag().equals(MainActivity.DRAWER_IS_USER)) {
				final long packedPosition = mDrawerList.getExpandableListPosition(position);
				final int selectPosition = ExpandableListView.getPackedPositionGroup(packedPosition) - 1;// for
				// highscores
				AlertDialog.Builder adb = new AlertDialog.Builder(DrawerHandlingActivity.this);
				adb.setTitle("Delete User?");
				//Log.e(TAG, "selectPosition:" + selectPosition);
				adb.setMessage("Are you sure you want to remove\n" + mUserNamesToShow.get(selectPosition));
				adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mUserNamesToShow.remove(selectPosition);
						mAdapter.notifyDataSetChanged();
						savePreferences();
					}
				});
				adb.setNegativeButton("No", null);
				adb.show();
				return true;
			}else {// is add user or highscores
				return false;
			}
		}else {
			return false;
		}
	}
}

}