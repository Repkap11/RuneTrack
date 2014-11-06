/**
 * DownloadIntentService.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.repkap11.runetrack.fragments.FragmentBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author paul
 */
public class DownloadIntentService extends IntentService {

public static final String PARAM_WHICH_DATA = "PARAM_WHICH_DATA";
//Request types
public static final String PARAM_USER_PROFILE_TABLE = "PARAM_USER_PROFILE_TABLE";
public static final String PARAM_HISTORY_GRAPH = "PARAM_HISTORY_GRAPH";
public static final String PARAM_XP_PI_CHART = "PARAM_XP_PI_CHART";
public static final String PARAM_RUNETRACK_HIGH_SCORES = "PARAM_RUNETRACK_HIGH_SCORES";

//Request spesific value
public static final String PARAM_USERNAME = "PARAM_USERNAME";
public static final String PARAM_SKILL_NUMBER = "PARAM_SKILL_NUMBER";
public static final String PARAM_USER_PROFILE_TABLE2 = "PARAM_USER_PROFILE_TABLE2";
public static final String PARAM_XP_COLORS = "PARAM_XP_COLORS";
public static final String PARAM_XP_PER_SKILL = "PARAM_XP_DEGREES";
public static final String PARAM_XP_SKILL_NAMES = "PARAM_XP_SKILL_NAMES";
public static final String PARAM_PROGRESS_ENTRIES = "PARAM_PROGRESS_ENTRIES";
public static final String PARAM_SKILL_NAME = "PARAM_SKILL_NAME";
public static final String PARAM_PAGE_NUMBER = "PARAM_PAGE_NUMBER";
public static final String PARAM_ERROR_CODE = "PARAM_USER_PROFILE_ERROR_CODE";

public static final String PARAM_HIGH_SCORES_ENTRIES = "PARAM_HIGH_SCORES_ENTRIES";
private static final int TIMEOUT = 5 * 1000;
private static final String TAG = DownloadIntentService.class.getSimpleName();

public DownloadIntentService() {
	super(TAG);
	//Log.e(TAG, "DownloadIntentService constructed");
}

@Override
public void onHandleIntent(Intent intent) {
	//Log.e(TAG, "Entering onHandleIntent");
	Intent outIntent = new Intent();
	outIntent.setAction(PARAM_USERNAME);
	outIntent.addCategory(Intent.CATEGORY_DEFAULT);
	String whichData = intent.getStringExtra(PARAM_WHICH_DATA);
	outIntent.putExtra(PARAM_WHICH_DATA,whichData);
	try {
		if(whichData.equals(PARAM_USER_PROFILE_TABLE)) {
			doUserProfileTable(intent, outIntent);
		}else if(whichData.equals(PARAM_HISTORY_GRAPH)) {
			doHistoryGraph(intent, outIntent);
		}else if(whichData.equals(PARAM_XP_PI_CHART)) {
			doXpPiChart(intent, outIntent);
		}else if(whichData.equals(PARAM_RUNETRACK_HIGH_SCORES)) {
			doRuneTrackHighScores(intent, outIntent);
		}
		//Log.e(TAG, "Error code:" + ERROR_CODE_SUCCESS);
		outIntent.putExtra(PARAM_ERROR_CODE, ERROR_CODE_SUCCESS);
	} catch(DownloadException e) {
		//Log.e(TAG, "Error code:" + e.mErrorCode);
		outIntent.putExtra(PARAM_ERROR_CODE, e.mErrorCode);
	}
	sendBroadcast(outIntent);
	//Log.e(TAG, "Done handling download intent");
}

public static final int ERROR_CODE_SUCCESS = 0;
public static final int ERROR_CODE_UNKNOWN = 1;
public static final int ERROR_CODE_NOT_ON_RS_HIGHSCORES = 2;
public static final int ERROR_CODE_NOT_ENOUGH_VIEWS = 3;
public static final int ERROR_CODE_RUNETRACK_DOWN = 4;

private void doUserProfileTable(Intent intent, Intent outIntent) throws DownloadException {
	String userName = intent.getStringExtra(PARAM_USERNAME);
	try {
		userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
	} catch(UnsupportedEncodingException e1) {
		e1.printStackTrace();
	}
	//Log.e(TAG, "Downloading " + userName);
	Document d;
	try {
		Connection c = Jsoup.connect("http://runetrack.com/profile.php?user=" + userName);
		c.timeout(TIMEOUT);
		d = c.get();
	} catch(Exception e) {
		throw new DownloadException(ERROR_CODE_RUNETRACK_DOWN);
	}
	try {
		ArrayList<DataTable> skills = new ArrayList<DataTable>();
		Element tableElement = d.getElementsByClass("profile_table2").first().child(0);
		for(int i = 2; i < tableElement.children().size(); i++) {// For each
			// skill
			// Log.e(TAG,"Loop iteration "+i);
			Element skill = tableElement.child(i);
			String skillName = skill.child(0).child(0).attr("title").replace(String.valueOf((char) 160), "");
			String level = skill.child(1).text().replace(String.valueOf((char) 160), "");
			String xp = skill.child(2).text().replace(String.valueOf((char) 160), "");
			String rank = skill.child(3).text().replace(String.valueOf((char) 160), "");
			String todayLevel = skill.child(4).text().replace(String.valueOf((char) 160), "");
			String todayxp = skill.child(5).text().replace(String.valueOf((char) 160), "");
			String weekLevel = skill.child(6).text().replace(String.valueOf((char) 160), "");
			String weekxp = skill.child(7).text().replace(String.valueOf((char) 160), "");
			skills.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{skillName, level, xp, rank, todayLevel, todayxp, weekLevel, weekxp}))));
		}
		DataTable topHeader = new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "Curnt ", "Runescape", "Stats", "Tod", "ay", "This", "Week"})));
		DataTable header = new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "Level", "Xp", "Rank", "Lvls", "Xp", "Lvls", "Xp"})));
		skills.add(0, topHeader);
		skills.add(1, header);
		outIntent.putExtra(PARAM_USERNAME, skills);
	} catch(Exception e) {
		Element body = d.body();
		Element parent = body.child(0).child(8);
		if(parent.children().size() <= 6) {
			Node node = parent.child(0).child(0).childNode(1);
			String nodeString = node.toString();
			Element element = body.child(0).child(8).child(0).child(0).child(1);
			Log.e(TAG, nodeString);
			throw new DownloadException(ERROR_CODE_NOT_ENOUGH_VIEWS);
		}else {
			Node node = parent.childNode(2);
			String text = node.toString().replace("&nbsp;", "");
			text = text.replace("<b>", "").replace("</b>", "").trim();
			//Log.e(TAG, text);
			throw new DownloadException(ERROR_CODE_NOT_ON_RS_HIGHSCORES);
			//String nodes = node.toString();
			//Element element =  body.child(0).child(8).child(0).child(0).child(1);
		}
	}
}

private void doHistoryGraph(Intent intent, Intent outIntent) throws DownloadException {
	doGraphParseing(intent, outIntent);
	doProgressEntries(intent, outIntent);
}

private void doXpPiChart(Intent intent, Intent outIntent) throws DownloadException {
	int[] colors;
	int[] xpPerSkill;
	String[] skillNames;
	boolean userGainedNoXp = false;
	String userName = intent.getStringExtra(PARAM_USERNAME);
	try {
		userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
	} catch(UnsupportedEncodingException e1) {
		e1.printStackTrace();
	}
	//Log.e(TAG, "Downloading pi chart" + userName);
	Document d;
	try {
		Connection c = Jsoup.connect("http://runetrack.com/includes/profile_chart.php?user=" + userName);
		c.timeout(TIMEOUT);
		d = c.get();
	} catch(Exception e) {
		throw new DownloadException(ERROR_CODE_RUNETRACK_DOWN);
	}
	try {
		Element e = d.body();
		//Log.e(TAG, "Downloading done " + userName);
		String xpString = e.text();
		JSONObject mainObject = new JSONObject(xpString);
		JSONArray temp = mainObject.getJSONArray("elements");
		JSONObject temp2 = temp.getJSONObject(0);
		// Log.e(TAG, temp2.toString(2));
		JSONArray degreesArray = null;
		try {
			degreesArray = temp2.getJSONArray("values");
		} catch(JSONException ex) {
			userGainedNoXp = true;
		}
		// Log.e(TAG, degreesArray.toString(2));
		xpPerSkill = new int[degreesArray.length()];// null degreesArray
		// means userGainedNoXp
		// = true;
		skillNames = new String[degreesArray.length()];
		// long totalxp = 0;
		for(int i = 0; i < degreesArray.length(); i++) {
			String skillName = degreesArray.getJSONObject(i).getString("label");
			skillNames[i] = skillName;
			int xp = degreesArray.getJSONObject(i).getInt("value");
			xpPerSkill[i] = xp;
			// totalxp += xp;
		}
		JSONArray colorsArray = temp2.getJSONArray("colours");
		colors = new int[colorsArray.length()];
		for(int i = 0; i < colorsArray.length(); i++) {
			int color = Color.parseColor(colorsArray.getString(i));
			// Log.e(TAG, "Color:"+colorsArray.getString(i)+" : "+color);
			colors[i] = color;
		}
		outIntent.putExtra(PARAM_XP_PER_SKILL, xpPerSkill);
		outIntent.putExtra(PARAM_XP_COLORS, colors);
		outIntent.putExtra(PARAM_XP_SKILL_NAMES, skillNames);
	} catch(Exception e) {
		Log.e(TAG, "Caught exception downloading, pi chart is empty");
		throw new DownloadException(ERROR_CODE_UNKNOWN);//TODO make known

	}
}

private void doRuneTrackHighScores(Intent intent, Intent outIntent) throws DownloadException {
	String skillName = intent.getStringExtra(PARAM_SKILL_NAME);
	int pageNumber = intent.getIntExtra(PARAM_PAGE_NUMBER, 0);
	ArrayList<DataTable> userEntries = new ArrayList<DataTable>();
	Document d;
	try {
		Connection c = Jsoup.connect("http://runetrack.com/high_scores.php?skill=" + skillName + "&page=" + pageNumber);
		c.timeout(TIMEOUT);
		d = c.get();
		//Log.e(TAG, "Downloading done " + skillName);
	} catch(Exception e) {
		throw new DownloadException(ERROR_CODE_RUNETRACK_DOWN);
	}
	try {
		userEntries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "", "", "", ""}))));
		userEntries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"RT Rank", "Name", "RS Rank", "Level", "XP"}))));
		Element ele = d.getElementsByClass("profile_table").get(1).child(0);
		// Log.e(TAG,"ele:"+ele.text());
		for(int i = 2; i < ele.children().size(); i++) {
			// Log.e(TAG,"Loop iteration "+i);
			Element skill = ele.child(i);
			// String skillName =
			// skill.child(0).child(0).attr("alt").replace(String.valueOf((char)
			// 160), "");
			String userName = skill.child(1).text().replace(String.valueOf((char) 160), "");
			String rsRank = skill.child(2).text().replace(String.valueOf((char) 160), "");
			String level = skill.child(3).text().replace(String.valueOf((char) 160), "");
			String xp = skill.child(4).text().replace(String.valueOf((char) 160), "");
			userEntries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{skillName, userName, rsRank, level, xp}))));
		}
		outIntent.setAction(PARAM_USERNAME);
		outIntent.addCategory(Intent.CATEGORY_DEFAULT);
		outIntent.putExtra(PARAM_HIGH_SCORES_ENTRIES, userEntries);
	} catch(Exception e) {
		userEntries = null;
		e.printStackTrace();
		//Log.e(TAG, "Caught exception downloading, highscores is empty");
		throw new DownloadException(ERROR_CODE_UNKNOWN);//TODO make known
	}

}

private void doGraphParseing(Intent intent, Intent outIntent) throws DownloadException {
	double[] points;
	String[] labels;
	String userName = intent.getStringExtra(PARAM_USERNAME);
	int skillNumber = intent.getIntExtra(PARAM_SKILL_NUMBER, 0);
	try {
		userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
	} catch(UnsupportedEncodingException e1) {
		throw new DownloadException(ERROR_CODE_RUNETRACK_DOWN);
		//e1.printStackTrace();
	}
	//Log.e(TAG, "Downloading graph" + userName + ":" + skillNumber);
	Document d;
	try {
		Connection c = Jsoup.connect("http://runetrack.com/includes/progress_chart.php?user=" + userName + "@" + skillNumber);
		c.timeout(TIMEOUT);
		d = c.get();
	} catch(Exception e) {
		throw new DownloadException(ERROR_CODE_RUNETRACK_DOWN);
	}
	try {
		Element e = d.body();
		//Log.e(TAG, "Downloading done " + userName);
		String xpString = e.text();

		JSONObject mainObject = new JSONObject(xpString);

		JSONArray xpValues = mainObject.getJSONArray("elements").getJSONObject(0).getJSONArray("values");
		// String webText = xpValues.toString(2);
		JSONArray datesValues = mainObject.getJSONObject("x_axis").getJSONObject("labels").getJSONArray("labels");
		// elementsArray.getJSONObject(0).getJSONArray("values");
		String webText = datesValues.length() + ":" + xpValues.length();
		final int numDataPoints = 200;
		points = new double[numDataPoints];
		labels = new String[numDataPoints];
		for(int i = 0; i < numDataPoints; i++) {
			int index = (int) Math.floor((float) i * ((float) (xpValues.length() - 1) / (float) numDataPoints));
			points[i] = xpValues.getDouble(index);
			labels[i] = datesValues.getString(index);
		}
		outIntent.putExtra(PARAM_USER_PROFILE_TABLE, points);
		outIntent.putExtra(PARAM_USER_PROFILE_TABLE2, labels);
		//Log.e(TAG, "Web Text:" + webText);
	} catch(Exception e) {
		e.printStackTrace();
		//Log.e(TAG, "Caught exception downloading, graph is empty");
		throw new DownloadException(ERROR_CODE_UNKNOWN);//TODO Figure it out
	}

}

private void doProgressEntries(Intent intent, Intent outIntent) throws DownloadException {
	String userName = intent.getStringExtra(PARAM_USERNAME);
	String skillName = intent.getStringExtra(PARAM_SKILL_NAME);
	try {
		userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
	} catch(UnsupportedEncodingException e1) {
		e1.printStackTrace();
	}
	Document d;
	try {
		Connection c = Jsoup.connect("http://runetrack.com/progress.php?user=" + userName + "&skill=" + skillName);

		//Connection c = Jsoup.connect("http://runetrack.com/progress.php?user=" + userName + "&skill=" + skillName + "&view=all#more");
		c.timeout(TIMEOUT);
		d = c.get();
	} catch(Exception e) {
		throw new DownloadException(ERROR_CODE_RUNETRACK_DOWN);
	}
	try {

		//Log.e(TAG, "Downloading done " + userName);
		Element ele = d.getElementsByClass("profile_table").get(1).child(0);
		ArrayList<Parcelable> entries = new ArrayList<Parcelable>(ele.children().size()+3);
		//Log.e(TAG, "Size2: " + ele.children().size());
		entries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "#", "Date", "Rank", "Level", "Xp", "Xp Gained"}))));
		entries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "", "", "", "", "", ""}))));
		entries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "", "", "", "", "", ""}))));
		for(int i = 2; i < ele.children().size(); i++) {// For each
			// skill
			// Log.e(TAG,"Loop iteration "+i);
			Element dayEntry = ele.child(i);
			String skillName2 = dayEntry.child(0).child(0).attr("title").replace(String.valueOf((char) 160), "");
			// Log.e(TAG,"skillName:"+skillName);
			String dayNumber = dayEntry.child(0).text().replace(String.valueOf((char) 160), "");
			// Log.e(TAG, "dayNumber:" + dayNumber);
			String date = dayEntry.child(1).text().replace(String.valueOf((char) 160), "");
			String rank = dayEntry.child(2).text().replace(String.valueOf((char) 160), "");
			String level = dayEntry.child(3).text().replace(String.valueOf((char) 160), "");
			String xp = dayEntry.child(4).text().replace(String.valueOf((char) 160), "");
			String xpgained = dayEntry.child(5).text().replace(String.valueOf((char) 160), "");
			entries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[]{skillName2, dayNumber, date, rank, level, xp, xpgained}))));
		}
		outIntent.putExtra(PARAM_PROGRESS_ENTRIES, entries);
	} catch(Exception e) {
		throw new DownloadException(ERROR_CODE_UNKNOWN);//TODO figure out what one
	}
}

class DownloadException extends Exception {
	public int mErrorCode;

	public DownloadException(int errorCode) {
		mErrorCode = errorCode;
	}
}
}
