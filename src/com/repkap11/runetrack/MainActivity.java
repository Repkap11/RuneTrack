/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.repkap11.runetrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static final String ARG_USERNAME = "arg_username";
	public static final String ARG_SKILL_NUMBER = "ARG_SKILL_NUMBER";
	public static final String ARG_SKILL_NAME = "ARG_SKILL_NAME";
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private List<String> mUserNamesToShow;
	private ExpandableListView mDrawerList;
	private ExpandableListAdapter mAdapter;
	public String mUserName;
	private static final String USER_PROFILE_NAMES = "USER_PROFILE_NAMES";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer_layout);
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
		mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);

		mUserNamesToShow = getStringArrayPref(prefs, USER_PROFILE_NAMES);
		if (mUserNamesToShow.size() == 0) {
			Log.e("Paul", "Not using saved values");
			mUserNamesToShow = new ArrayList<String>(Arrays.asList(new String[] { "Repkap11", "Za phod", "Great One", "Zezima", "Repkam09",
					"S U O M I", "Jake", "Drumgun", "Alkan" }));
		}
		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mAdapter = new BaseExpandableListAdapter() {

			@Override
			public boolean isChildSelectable(int groupPosition, int childPosition) {
				if (groupPosition == getGroupCount() - 1) {
					return false;
				} else {
					return true;
				}
			}

			@Override
			public boolean hasStableIds() {
				return true;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
				// Log.e("Paul", "Getting view " + position);
				TextView view = (TextView) getLayoutInflater().inflate(R.layout.drawer_list_item, parent, false);
				if (groupPosition == getGroupCount() - 1) {
					view.setText("Add New User");
					view.setTag(false);// is user
				} else {
					view.setText(mUserNamesToShow.get(groupPosition));
					view.setTag(true);// is user
				}

				return view;
			}

			@Override
			public long getGroupId(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getGroupCount() {
				return mUserNamesToShow.size() + 1;
			}

			@Override
			public Object getGroup(int groupPosition) {
				// TODO Auto-generated method stub
				return "New Group Data Position:" + groupPosition;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				if (groupPosition == getGroupCount() - 1) {
					return 0;
				} else {
					return 2;
				}
			}

			@Override
			public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
				// Log.e("Paul", "Getting view " + position);
				TextView view = (TextView) convertView;
				if (view == null) {
					view = (TextView) getLayoutInflater().inflate(R.layout.drawer_list_sub_item, parent, false);
				}
				view = (TextView) getLayoutInflater().inflate(R.layout.drawer_list_sub_item, parent, false);
				view.setText(childPosition == 0 ? "    User Profile":"    Weekly Xp Distribution");
				return view;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				return 0;
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		mDrawerList.setOnChildClickListener(new DrawerChildClickListener());
		mDrawerList.setOnItemLongClickListener(new DrawerGroupLongClickListener());
		mDrawerList.setOnGroupClickListener(new DrawerGroupClickListener());
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mDrawerToggle = new ActionBarDrawerToggle(this, // host Activity
				mDrawerLayout, // DrawerLayout object
				R.drawable.ic_drawer, // nav drawer image to replace 'Up' caret
				R.string.drawer_open, // / "open drawer" description for
										// accessibility
				R.string.drawer_close // "close drawer" description for
										// accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);

				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerStateChanged(int newState) {
				if (newState == DrawerLayout.STATE_DRAGGING) {
					invalidateOptionsMenu();
				}
			}
		};
		mDrawerList.setAdapter(mAdapter);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			mUserName = mUserNamesToShow.get(0);
			selectUserProfileByName(mUserName);
			// selectHitsoryGraph(mUserName, 0, "Overall");
			selectPiChart(mUserName);
		} else {
			// Fragment will take care of most of the state...
			mUserName = savedInstanceState.getString(ARG_USERNAME);

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(ARG_USERNAME, mUserName);
		super.onSaveInstanceState(outState);
	}

	private void savePreferences() {
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		Set<String> set = new HashSet<String>();
		set.addAll(mUserNamesToShow);
		Editor editor = prefs.edit();
		editor.putStringSet(USER_PROFILE_NAMES, set);
		setStringArrayPref(editor, USER_PROFILE_NAMES, mUserNamesToShow);
		editor.apply();
	}

	public static void setStringArrayPref(Editor editor, String key, List<String> values) {
		JSONArray a = new JSONArray();
		for (int i = 0; i < values.size(); i++) {
			a.put(values.get(i));
		}
		if (!values.isEmpty()) {
			editor.putString(key, a.toString());
		} else {
			editor.putString(key, null);
		}
	}

	public static ArrayList<String> getStringArrayPref(SharedPreferences prefs, String key) {
		String json = prefs.getString(key, null);
		ArrayList<String> urls = new ArrayList<String>();
		if (json != null) {
			try {
				JSONArray a = new JSONArray(json);
				for (int i = 0; i < a.length(); i++) {
					String url = a.optString(i);
					urls.add(url);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return urls;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		final SearchView search = (SearchView) menu.findItem(R.id.action_bar_search_user).getActionView();
		// getCurrentFocus().clearFocus();
		// search.clearFocus();
		// search.setFocusable(true);
		// search.setIconified(false);
		// search.requestFocusFromTouch();
		search.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				// Do something
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				// Do something
				selectUserProfileByName(query);
				// search.clearFocus();
				// search.setIconified(true);
				menu.findItem(R.id.action_bar_search_user).collapseActionView();

				return true;
			}
		};
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("TAG", "Clicked");
				menu.findItem(R.id.action_bar_search_user).collapseActionView();

			}
		};
		OnFocusChangeListener focus = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.e("Paul", "Focus Changed");
				if (!hasFocus) {
					menu.findItem(R.id.action_bar_search_user).collapseActionView();
				}
			}
		};
		search.setOnQueryTextListener(queryTextListener);
		search.setOnQueryTextFocusChangeListener(focus);

		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		MenuItem searchItem = menu.findItem(R.id.action_bar_search_user);
		searchItem.setVisible(!drawerOpen);
		searchItem.collapseActionView();
		// search.clearFocus();
		// search.setIconified(false);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int result = item.getNumericShortcut() - 'a';
		if (result != -97) {
			selectHitsoryGraph(mUserName, result, (String) item.getTitle());
		}

		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_bar_search_user:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DrawerGroupClickListener implements OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
			Log.e("Paul", "onGroupClick called");
			if (view.getTag() != null) {
				if (view.getTag().equals(true)) {
					return false;
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
					builder.setTitle("Enter Username");
					// Set up the input
					final EditText input = new EditText(MainActivity.this);
					input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
					builder.setView(input);
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String userName = input.getText().toString();
							mUserNamesToShow.add(userName);
							((BaseAdapter) mAdapter).notifyDataSetChanged();
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
			} else {
				return false;
			}
		}
	}

	private class DrawerChildClickListener implements OnChildClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
			Log.e("Paul", "onChildClick called:"+groupPosition+":"+childPosition);
			if (childPosition == 0){
				selectUserProfileByName(mUserNamesToShow.get(groupPosition));
			}
			if (childPosition == 1){
				selectPiChart(mUserNamesToShow.get(groupPosition));
			}
			return true;

		}
	}

	private class DrawerGroupLongClickListener implements ListView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View view, final int groupPosition, long dontknow) {
			Log.e("Paul", "onGroupLongClick called");
			if (view.getTag() != null) {
				if (view.getTag().equals(true)) {
					AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
					adb.setTitle("Delete User?");
					adb.setMessage("Are you sure you want to remove\n" + mUserNamesToShow.get(groupPosition));
					adb.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mUserNamesToShow.remove(groupPosition);
							((BaseAdapter) mAdapter).notifyDataSetChanged();
							savePreferences();
						}
					});
					adb.setNegativeButton("No", null);
					adb.show();
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	void selectUserProfileByName(String userName) {
		mUserName = userName;
		// update the main content by replacing fragments
		Fragment fragment = new UserProfileFragment();
		Bundle args = new Bundle();
		args.putString(ARG_USERNAME, userName);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "UserProfile").commit();

		// update selected item and title, then close the drawer
		// mDrawerList.setItemChecked(position, true);
		setTitle(userName);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	/**
	 * @param string
	 */
	public void selectPiChart(String userName) {
		mUserName = userName;
		Log.e("Paul","Selecting pi chart for:"+userName);
		Fragment fragment = new XpDistributionChartFragment();
		Bundle args = new Bundle();
		args.putString(ARG_USERNAME, userName);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "XpPiChart").commit();
		setTitle(userName);
		mDrawerLayout.closeDrawer(mDrawerList);
		
	}

	void selectHitsoryGraph(String userName, int skillNumber, String skillName) {
		// update the main content by replacing fragments
		Fragment fragment = new HistoryGraphFragment();
		Bundle args = new Bundle();
		args.putString(ARG_USERNAME, userName);
		args.putInt(ARG_SKILL_NUMBER, skillNumber);
		args.putString(ARG_SKILL_NAME, skillName);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, "HistoryGraph").commit();

		// update selected item and title, then close the drawer
		// mDrawerList.setItemChecked(position, true);
		setTitle(userName);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
}