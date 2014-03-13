package com.repkap11.runetrack;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class UserProfileSkill implements Parcelable {

	public final String skillName;
	public ArrayList<String> mListOfItems;

	public UserProfileSkill(String skillName, String level, String xp, String rank, String todayLevel, String todayxp, String weekLevel, String weekxp) {
		this.skillName = skillName;
		this.mListOfItems = new ArrayList<String>(7);
		//mListOfItems.add(skillName);
		mListOfItems.add(level);
		mListOfItems.add(xp);
		mListOfItems.add(rank);
		mListOfItems.add(todayLevel);
		mListOfItems.add(todayxp);
		mListOfItems.add(weekLevel);
		mListOfItems.add(weekxp);
	}

	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeStringList(mListOfItems);
		out.writeString(skillName);
	}
	
	public static final Parcelable.Creator<UserProfileSkill> CREATOR = new Parcelable.Creator<UserProfileSkill>() {
		public UserProfileSkill createFromParcel(Parcel in) {
			return new UserProfileSkill(in);
		}
		@Override
		public UserProfileSkill[] newArray(int size) {
			return new UserProfileSkill[size];
		}
	};

	private UserProfileSkill(Parcel in) {
		if (in == null){
		Log.e("Paul","Why is in null?");
		}else {
			//Log.e("Paul","In is not null");
		}
		mListOfItems = new ArrayList<String>();
		in.readStringList(mListOfItems);
		
		this.skillName = in.readString();

	}
}
