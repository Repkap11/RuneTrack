/**
 * DownloadIntentService.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author paul
 * 
 */
public class DownloadIntentService extends IntentService {

	public static final String PARAM_USERNAME = "PARAM_USERNAME";
	public static final String PARAM_SKILL_NUMBER = "PARAM_SKILL_NUMBER";
	public static final String PARAM_USER_PROFILE_TABLE = "PARAM_USER_PROFILE_TABLE";
	public static final String PARAM_HISTORY_GRAPH = "PARAM_HISTORY_GRAPH";
	public static final String PARAM_XP_PI_CHART = "PARAM_XP_PI_CHART";
	public static final String PARAM_WHICH_DATA = "PARAM_WHICH_DATA";
	public static final String PARAM_USER_PROFILE_TABLE2 = "PARAM_USER_PROFILE_TABLE2";
	private static final int TIMEOUT = 5 * 1000;
	public static final String PARAM_XP_COLORS = "PARAM_XP_COLORS";
	public static final String PARAM_XP_PER_SKILL = "PARAM_XP_DEGREES";
	public static final String PARAM_XP_SKILL_NAMES = "PARAM_XP_SKILL_NAMES";
	public static final String PARAM_XP_USER_GAINED_NO_XP = "PARAM_XP_USER_GAINED_NO_XP";
	public static final String PARAM_PROGRESS_ENTRIES = "PARAM_PROGRESS_ENTRIES";
	private static final String TAG = "DownloadIntentService";
	public static final String PARAM_SKILL_NAME = "PARAM_SKILL_NAME";
	public static final String PARAM_PAGE_NUMBER = "PARAM_PAGE_NUMBER";
	public static final String PARAM_RUNETRACK_HIGH_SCORES = "PARAM_RUNETRACK_HIGH_SCORES";
	public static final String PARAM_HIGH_SCORES_ENTRIES = "PARAM_HIGH_SCORES_ENTRIES";

	public DownloadIntentService() {
        super("UserNameInfoDownloader");
        Log.e(TAG,"DownloadIntentService constructed");

	}

	@Override
	public void onHandleIntent(Intent intent) {
		Log.e(TAG, "Entering onHandleIntent");
		String whichData = intent.getStringExtra(PARAM_WHICH_DATA);
		if (whichData.equals(PARAM_USER_PROFILE_TABLE)) {
			doUserProfileTable(intent);
		} else if (whichData.equals(PARAM_HISTORY_GRAPH)) {
			doHistoryGraph(intent);
		} else if (whichData.equals(PARAM_XP_PI_CHART)) {
			doXpPiChart(intent);
		} else if (whichData.equals(PARAM_RUNETRACK_HIGH_SCORES)) {
			doRuneTrackHighScores(intent);
		}
        Log.e(TAG,"Done handling download intent");
	}

	private void doRuneTrackHighScores(Intent intent) {
		String skillName = intent.getStringExtra(PARAM_SKILL_NAME);
		int pageNumber = intent.getIntExtra(PARAM_PAGE_NUMBER, 0);
		ArrayList<DataTable> userEntries = new ArrayList<DataTable>();

		try {
			Connection c = Jsoup.connect("http://runetrack.com/high_scores.php?skill=" + skillName + "&page=" + pageNumber);
			c.timeout(TIMEOUT);
			Document d = c.get();
			Log.e(TAG, "Downloading done " + skillName);

			Element ele = d.getElementsByClass("profile_table").get(1).child(0);
			// Log.e(TAG,"ele:"+ele.text());
			for (int i = 2; i < ele.children().size(); i++) {
				// Log.e(TAG,"Loop iteration "+i);
				Element skill = ele.child(i);
				// String skillName =
				// skill.child(0).child(0).attr("alt").replace(String.valueOf((char)
				// 160), "");
				String userName = skill.child(1).text().replace(String.valueOf((char) 160), "");
				String rsRank = skill.child(2).text().replace(String.valueOf((char) 160), "");
				String level = skill.child(3).text().replace(String.valueOf((char) 160), "");
				String xp = skill.child(4).text().replace(String.valueOf((char) 160), "");
				userEntries.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[] { skillName, userName, rsRank, level, xp }))));
			}

		} catch (Exception e) {
			userEntries = null;
			e.printStackTrace();
			Log.e(TAG, "Caught exception downloading, highscores is empty");

		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_HIGH_SCORES_ENTRIES, userEntries);
		sendBroadcast(broadcastIntent);
	}

	private void doXpPiChart(Intent intent) {
		int[] colors;
		int[] xpPerSkill;
		String[] skillNames;
		boolean userGainedNoXp = false;
		String userName = intent.getStringExtra(PARAM_USERNAME);
		try {
			userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Log.e(TAG, "Downloading pi chart" + userName);
		try {
			Connection c = Jsoup.connect("http://runetrack.com/includes/profile_chart.php?user=" + userName);
			c.timeout(TIMEOUT);
			Document d = c.get();
			Element e = d.body();
			Log.e(TAG, "Downloading done " + userName);
			String xpString = e.text();
			JSONObject mainObject = new JSONObject(xpString);
			JSONArray temp = mainObject.getJSONArray("elements");
			JSONObject temp2 = temp.getJSONObject(0);
			// Log.e(TAG, temp2.toString(2));
			JSONArray degreesArray = null;
			try {
				degreesArray = temp2.getJSONArray("values");
			} catch (JSONException ex) {
				userGainedNoXp = true;
			}
			// Log.e(TAG, degreesArray.toString(2));
			xpPerSkill = new int[degreesArray.length()];// null degreesArray
														// means userGainedNoXp
														// = true;
			skillNames = new String[degreesArray.length()];
			// long totalxp = 0;
			for (int i = 0; i < degreesArray.length(); i++) {
				String skillName = degreesArray.getJSONObject(i).getString("label");
				skillNames[i] = skillName;
				int xp = degreesArray.getJSONObject(i).getInt("value");
				xpPerSkill[i] = xp;
				// totalxp += xp;
			}
			JSONArray colorsArray = temp2.getJSONArray("colours");
			colors = new int[colorsArray.length()];
			for (int i = 0; i < colorsArray.length(); i++) {
				int color = Color.parseColor(colorsArray.getString(i));
				// Log.e(TAG, "Color:"+colorsArray.getString(i)+" : "+color);
				colors[i] = color;
			}

		} catch (Exception e) {
			xpPerSkill = null;
			colors = null;
			skillNames = null;
			// e.printStackTrace();
			Log.e(TAG, "Caught exception downloading, pi chart is empty");

		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_XP_PER_SKILL, xpPerSkill);
		broadcastIntent.putExtra(PARAM_XP_COLORS, colors);
		broadcastIntent.putExtra(PARAM_XP_SKILL_NAMES, skillNames);
		broadcastIntent.putExtra(PARAM_XP_USER_GAINED_NO_XP, userGainedNoXp);
		sendBroadcast(broadcastIntent);
	}

	private void doHistoryGraph(Intent intent) {
		Intent broadcastIntent = doGraphParseing(intent);
		doProgressEntries(intent, broadcastIntent);
	}

	private void doProgressEntries(Intent intent, Intent broadcastIntent) {
		ArrayList<Parcelable> entries;
		String userName = intent.getStringExtra(PARAM_USERNAME);
		String skillName = intent.getStringExtra(PARAM_SKILL_NAME);
		try {
			userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			// Connection c =
			// Jsoup.connect("http://runetrack.com/progress.php?user=" +
			// userName + "&skill=" + skillName);
			Connection c = Jsoup.connect("http://runetrack.com/progress.php?user=" + userName + "&skill=" + skillName + "&view=all#more");
			c.timeout(TIMEOUT);
			Document d = c.get();
			Log.e(TAG, "Downloading done " + userName);

			Element ele = d.getElementsByClass("profile_table").get(1).child(0);
			Log.e(TAG, "Size2: " + ele.children().size());
			entries = new ArrayList<Parcelable>(ele.children().size());
			for (int i = 2; i < ele.children().size(); i++) {// For each
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
				entries.add(new DataTable(new ArrayList<String>(Arrays
						.asList(new String[] { skillName2, dayNumber, date, rank, level, xp, xpgained }))));
			}

		} catch (Exception e) {
			e.printStackTrace();
			entries = null;
			Log.e(TAG, "Caught exception downloading, progress entries");
		}
		broadcastIntent.putExtra(PARAM_PROGRESS_ENTRIES, entries);
		sendBroadcast(broadcastIntent);

	}

	private Intent doGraphParseing(Intent intent) {
		double[] points;
		String[] labels;
		String userName = intent.getStringExtra(PARAM_USERNAME);
		int skillNumber = intent.getIntExtra(PARAM_SKILL_NUMBER, 0);
		try {
			userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Log.e(TAG, "Downloading graph" + userName + ":" + skillNumber);
		try {
			Connection c = Jsoup.connect("http://runetrack.com/includes/progress_chart.php?user=" + userName + "@" + skillNumber);
			c.timeout(TIMEOUT);
			Document d = c.get();
			Element e = d.body();
			Log.e(TAG, "Downloading done " + userName);
			String xpString = e.text();

			JSONObject mainObject = new JSONObject(xpString);

			JSONArray xpValues = mainObject.getJSONArray("elements").getJSONObject(0).getJSONArray("values");
			// String webText = xpValues.toString(2);
			JSONArray datesValues = mainObject.getJSONObject("x_axis").getJSONObject("labels").getJSONArray("labels");
			// elementsArray.getJSONObject(0).getJSONArray("values");
			String webText = datesValues.length() + ":" + xpValues.length();
			points = new double[xpValues.length()];
			labels = new String[datesValues.length()];
			for (int i = 0; i < xpValues.length(); i++) {
				points[i] = xpValues.getDouble(i);
				labels[i] = datesValues.getString(i);
			}

			Log.e(TAG, "Web Text:" + webText);
		} catch (Exception e) {
			points = null;
			labels = null;
			e.printStackTrace();
			Log.e(TAG, "Caught exception downloading, graph is empty");

		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_USER_PROFILE_TABLE, points);
		broadcastIntent.putExtra(PARAM_USER_PROFILE_TABLE2, labels);
		return broadcastIntent;
	}

	private void doUserProfileTable(Intent intent) {
		String userName = intent.getStringExtra(PARAM_USERNAME);
		ArrayList<DataTable> skills = new ArrayList<DataTable>();
		try {
			userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Log.e(TAG, "Downloading " + userName);
		try {
			Connection c = Jsoup.connect("http://runetrack.com/profile.php?user=" + userName);
			Document d = c.get();
			Element ele = d.getElementsByClass("profile_table2").first().child(0);

			for (int i = 2; i < ele.children().size(); i++) {// For each
																// skill
				// Log.e(TAG,"Loop iteration "+i);
				Element skill = ele.child(i);
				String skillName = skill.child(0).child(0).attr("title").replace(String.valueOf((char) 160), "");
				String level = skill.child(1).text().replace(String.valueOf((char) 160), "");
				String xp = skill.child(2).text().replace(String.valueOf((char) 160), "");
				String rank = skill.child(3).text().replace(String.valueOf((char) 160), "");
				String todayLevel = skill.child(4).text().replace(String.valueOf((char) 160), "");
				String todayxp = skill.child(5).text().replace(String.valueOf((char) 160), "");
				String weekLevel = skill.child(6).text().replace(String.valueOf((char) 160), "");
				String weekxp = skill.child(7).text().replace(String.valueOf((char) 160), "");
				skills.add(new DataTable(new ArrayList<String>(Arrays.asList(new String[] { skillName, level, xp, rank, todayLevel, todayxp,
						weekLevel, weekxp }))));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Caught exception downloading, settings skills to empty");
			skills = new ArrayList<DataTable>();
		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_USERNAME, skills);
		sendBroadcast(broadcastIntent);

	}

}
