/**
 * HtmlDownload.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack;

import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.Toast;

public class HtmlDownload extends AsyncTask<String, Void, ArrayList<UserProfileSkill>> {
	private Context context;

	public HtmlDownload(Context context) {
		this.context = context;
	}

	@Override
	protected ArrayList<UserProfileSkill> doInBackground(String... params) {
		String userName = Html.escapeHtml(params[0]);
		try {
			Connection c = Jsoup.connect("http://runetrack.com/profile.php?user=" + userName);
			Document d = c.get();
			Element ele = d.getElementsByClass("profile_table2").first().child(0);
			ArrayList<UserProfileSkill> skills = new ArrayList<UserProfileSkill>();
			for (int i = 2; i < ele.children().size(); i++) {// For each skill
				Element skill = ele.child(i);
				String skillName = skill.child(0).child(0).attr("title");
				String level = skill.child(1).text();
				String xp = skill.child(2).text();
				String rank = skill.child(3).text();
				String todayLevel = skill.child(4).text();
				String todayxp = skill.child(5).text();
				String weekLevel = skill.child(6).text();
				String weekxp = skill.child(7).text();
				skills.add(new UserProfileSkill(skillName, level, xp, rank, todayLevel, todayxp, weekLevel, weekxp));
			}
			return skills;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(ArrayList<UserProfileSkill> result) {
		if (result != null) {
			Toast.makeText(context, "Operation Done: " + result.toString(), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, "Operation Failed", Toast.LENGTH_LONG).show();
		}
	}
}
