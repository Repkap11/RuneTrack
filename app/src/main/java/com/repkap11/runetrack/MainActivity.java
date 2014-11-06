package com.repkap11.runetrack;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.repkap11.runetrack.fragments.FragmentBase;
import com.repkap11.runetrack.fragments.RuneTrackHighScoresFragment;
import com.repkap11.runetrack.fragments.UserProfileFragment;
import com.repkap11.runetrack.fragments.UserProgressFragment;
import com.repkap11.runetrack.fragments.XpDistributionChartFragment;

public class MainActivity extends DrawerHandlingActivity {

public static final String ARG_USERNAME = "ARG_USERNAME";
public static final String ARG_SKILL_NUMBER = "ARG_SKILL_NUMBER";
public static final String ARG_SKILL_NAME = "ARG_SKILL_NAME";
public static final String ARG_PAGE_NUMBER = "ARG_PAGE_NUMBER";
public static final String ARG_IS_SHOWING_HIGHSCORES = "ARG_IS_SHOWING_HIGHSCORES";
public static final String TAG = "MainActivity";
public static final String FRAGMENT_TAG = "FRAGMENT_TAG";
public String mUserName;
public FragmentBase mCurrentFragment;
private boolean mIsShowingHighScores = false;
private CharSequence mTitle;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	onCreateAfterSetContentView(savedInstanceState);
	// Now find the PullToRefreshLayout to setup

	mTitle = getTitle();

	if(savedInstanceState != null) {
		mCurrentFragment = (FragmentBase) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
		mUserName = savedInstanceState.getString(ARG_USERNAME);
		mIsShowingHighScores = savedInstanceState.getBoolean(ARG_IS_SHOWING_HIGHSCORES, mIsShowingHighScores);
	}else {
		mUserName = getFirstUserName();
		// TODO what fragment to initialize to
		// selectHitsoryGraph(mUserName, 0, "Overall");
		// selectPiChart(mUserName);

		// mIsShowingHighScores = true;
		// selectRuneTrackHighScores("Overall", 1);

		selectUserProfileByName(mUserName);
	}
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {

	int result = item.getNumericShortcut() - 'a';
	if(result != -97) {
		if(mIsShowingHighScores) {
			selectRuneTrackHighScores((String) item.getTitle(), 1);
		}else {
			selectHistoryGraph(mUserName, result, (String) item.getTitle());
		}
	}

	// The action bar home/up action should open or close the drawer.
	// ActionBarDrawerToggle will take care of this.
	return super.onOptionsItemSelected(item);
}

@Override
public void selectRuneTrackHighScores(String skillName, int pageNumber) {
	super.selectRuneTrackHighScores(skillName, pageNumber);
	mIsShowingHighScores = true;
	//Log.e(TAG, "Selecting highscores for:" + skillName + " page #" + pageNumber);
	mCurrentFragment = new RuneTrackHighScoresFragment();
	Bundle args = new Bundle();

	args.putInt(ARG_PAGE_NUMBER, pageNumber);
	args.putString(ARG_SKILL_NAME, skillName);

	mCurrentFragment.setArguments(args);
	FragmentManager fragmentManager = getFragmentManager();
	fragmentManager.beginTransaction().replace(R.id.content_frame, mCurrentFragment, FRAGMENT_TAG).commitAllowingStateLoss();
	setTitle(getResources().getString(R.string.runetrack_highscores));
}

@Override
public void selectPiChart(String userName) {
	super.selectPiChart(userName);
	mIsShowingHighScores = false;
	mUserName = userName;
	//Log.e(TAG, "Selecting pi chart for:" + userName);
	mCurrentFragment = new XpDistributionChartFragment();
	Bundle args = new Bundle();
	args.putString(ARG_USERNAME, userName);
	mCurrentFragment.setArguments(args);

	FragmentManager fragmentManager = getFragmentManager();
	fragmentManager.beginTransaction().replace(R.id.content_frame, mCurrentFragment, FRAGMENT_TAG).commitAllowingStateLoss();
	setTitle(userName);
}

@Override
public void selectHistoryGraph(String userName, int skillNumber, String skillName) {
	super.selectHistoryGraph(userName, skillNumber, skillName);
	mIsShowingHighScores = false;
	// update the main content by replacing fragments
	mCurrentFragment = new UserProgressFragment();
	Bundle args = new Bundle();
	args.putString(ARG_USERNAME, userName);
	args.putInt(ARG_SKILL_NUMBER, skillNumber);
	args.putString(ARG_SKILL_NAME, skillName);
	mCurrentFragment.setArguments(args);

	FragmentManager fragmentManager = getFragmentManager();
	fragmentManager.beginTransaction().replace(R.id.content_frame, mCurrentFragment, FRAGMENT_TAG).commitAllowingStateLoss();
	setTitle(userName);
}

@Override
public void selectUserProfileByName(String userName) {
	super.selectUserProfileByName(userName);
	mIsShowingHighScores = false;
	mUserName = userName;
	// update the main content by replacing fragments
	mCurrentFragment = new UserProfileFragment();
	Bundle args = new Bundle();
	args.putString(ARG_USERNAME, userName);
	mCurrentFragment.setArguments(args);

	FragmentManager fragmentManager = getFragmentManager();
	fragmentManager.beginTransaction().replace(R.id.content_frame, mCurrentFragment, FRAGMENT_TAG).commitAllowingStateLoss();

	// update selected item and title, then close the drawer
	//mDrawerList.setItemChecked(position, true);
	setTitle(userName);
}

public void refreshComplete() {
	//if (mSwipeRefreshLayout != null) {
	//    mSwipeRefreshLayout.setRefreshing(false);
	//}
}

@Override
public boolean onCreateOptionsMenu(final Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.main, menu);
	MenuItem searchItem = menu.findItem(R.id.action_bar_search_user);
	final SearchView search = (SearchView) searchItem.getActionView();
	if (Build.VERSION.SDK_INT < 21) {
		try {
			int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
			ImageView v = (ImageView) search.findViewById(searchImgId);
			v.setImageResource(R.drawable.ic_menu_search_holo_light);
		} catch(Exception e) {
			Toast.makeText(this, "Unable to set custom search icon", Toast.LENGTH_SHORT).show();
		}
	}
	// getCurrentFocus().clearFocus();
	// search.clearFocus();
	// search.setFocusable(true);
	// search.setIconified(false);
	// search.requestFocusFromTouch();

	search.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
	final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
		@Override
		public boolean onQueryTextSubmit(String query) {
			// Do something
			selectUserProfileByName(query);
			MenuItem searchItem = menu.findItem(R.id.action_bar_search_user);
			SearchView searchView = (SearchView) searchItem.getActionView();
			searchView.onActionViewCollapsed();
			return true;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			// Do something
			return true;
		}
	};
	OnFocusChangeListener focus = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			//Log.e(TAG, "Focus Changed");
			if(!hasFocus) {
				MenuItem searchItem = menu.findItem(R.id.action_bar_search_user);
				SearchView searchView = (SearchView) searchItem.getActionView();
				searchView.onActionViewCollapsed();
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
	MenuItem searchItem = menu.findItem(R.id.action_bar_search_user);
	searchItem.collapseActionView();
	return super.onPrepareOptionsMenu(menu);
}

@Override
public void setTitle(CharSequence title) {
	mTitle = title;
	getActionBar().setTitle(mTitle);
}

private void showInputMethod(View view) {
	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	if(imm != null) {
		imm.showSoftInput(view, 0);
	}
}

@Override
protected void onSaveInstanceState(Bundle outState) {
	outState.putBoolean(ARG_IS_SHOWING_HIGHSCORES, mIsShowingHighScores);
	outState.putString(ARG_USERNAME, mUserName);
	super.onSaveInstanceState(outState);
}

public boolean canFragmentScrollUp() {
	if(mCurrentFragment != null) {
		return mCurrentFragment.canScrollUp();
	}
	return true;
}
}
