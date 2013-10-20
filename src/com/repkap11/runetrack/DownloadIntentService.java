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

	public static final String PARAM_USERNAME = "user_name";

	/**
	 * @param name
	 */
	public DownloadIntentService() {
		super("UserNameInfoDownloader");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onHandleIntent(Intent intent) {
		//Log.e("Paul", "Entering onHandleIntent");
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
		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PARAM_USERNAME);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_USERNAME, skills);
		sendBroadcast(broadcastIntent);

	}

}
