/**
 * DownloadIntentService.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * @author paul
 * 
 */
public class DownloadIntentService extends IntentService {

	public static final String PARAM_USERNAME = "PARAM_USERNAME";
	public static final String PARAM_SKILL_NUMBER = "PARAM_SKILL_NUMBER";
	public static final String PARAM_USER_PROFILE_TABLE = "PARAM_USER_PROFILE_TABLE";
	public static final String PARAM_HISTORY_GRAPH = "PARAM_HISTORY_GRAPH";
	public static final String PARAM_WHICH_DATA = "PARAM_WHICH_DATA";
	public static final String PARAM_USER_PROFILE_TABLE2 = "PARAM_USER_PROFILE_TABLE2";
	private static final int TIMEOUT = 5*1000;

	/**
	 * @param name
	 */
	public DownloadIntentService() {
		super("UserNameInfoDownloader");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onHandleIntent(Intent intent) {
		// Log.e("Paul", "Entering onHandleIntent");
		String whichData = intent.getStringExtra(PARAM_WHICH_DATA);
		if (whichData.equals(PARAM_USER_PROFILE_TABLE)) {
			doUserProfileTable(intent);
		} else if (whichData.equals(PARAM_HISTORY_GRAPH)) {
			doHistoryGraph(intent);
		}

	}

	/**
	 * @param intent
	 */
	private void doHistoryGraph(Intent intent) {
		double[] points;
		String[] labels;
		String userName = intent.getStringExtra(PARAM_USERNAME);
		int skillNumber = intent.getIntExtra(PARAM_SKILL_NUMBER, 0);
		try {
			userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Log.e("Paul", "Downloading graph" + userName +":"+skillNumber);
		try {
			Connection c = Jsoup.connect("http://runetrack.com/includes/progress_chart.php?user=" + userName + "@" + skillNumber);
			c.timeout(TIMEOUT);
			Document d = c.get();
			Element e = d.body();
			Log.e("Paul", "Downloading done " + userName);
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
				labels[i] =  datesValues.getString(i);
			}

			Log.e("Paul", "Web Text:" + webText);
		} catch (Exception e) {
			points = null;
			labels = null;
			e.printStackTrace();
			Log.e("Paul", "Caught exception downloading, graph is empty");

		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_USER_PROFILE_TABLE, points);
		broadcastIntent.putExtra(PARAM_USER_PROFILE_TABLE2, labels);
		sendBroadcast(broadcastIntent);
	}

	/**
	 * @param intent
	 */
	private void doUserProfileTable(Intent intent) {
		String userName = intent.getStringExtra(PARAM_USERNAME);
		ArrayList<UserProfileSkill> skills = new ArrayList<UserProfileSkill>();
		try {
			userName = URLEncoder.encode(userName, Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Log.e("Paul", "Downloading " + userName);
		try {
			Connection c = Jsoup.connect("http://runetrack.com/profile.php?user=" + userName);
			Document d = c.get();
			Element ele = d.getElementsByClass("profile_table2").first().child(0);

			for (int i = 2; i < ele.children().size(); i++) {// For each
																// skill
				// Log.e("Paul","Loop iteration "+i);
				Element skill = ele.child(i);
				String skillName = skill.child(0).child(0).attr("title").replace(String.valueOf((char) 160), "");
				String level = skill.child(1).text().replace(String.valueOf((char) 160), "");
				String xp = skill.child(2).text().replace(String.valueOf((char) 160), "");
				String rank = skill.child(3).text().replace(String.valueOf((char) 160), "");
				String todayLevel = skill.child(4).text().replace(String.valueOf((char) 160), "");
				String todayxp = skill.child(5).text().replace(String.valueOf((char) 160), "");
				String weekLevel = skill.child(6).text().replace(String.valueOf((char) 160), "");
				String weekxp = skill.child(7).text().replace(String.valueOf((char) 160), "");
				skills.add(new UserProfileSkill(skillName, level, xp, rank, todayLevel, todayxp, weekLevel, weekxp));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Paul", "Caught exception downloading, settings skills to empty");
			skills = new ArrayList<UserProfileSkill>();
		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_USERNAME, skills);
		sendBroadcast(broadcastIntent);

	}

}
