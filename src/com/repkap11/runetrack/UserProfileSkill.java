package com.repkap11.runetrack;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class UserProfileSkill implements Parcelable {
	public ArrayList<String> mListOfItems;

	public UserProfileSkill(ArrayList<String> items) {
		this.mListOfItems = items;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeStringList(mListOfItems);
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
		if (in == null) {
		} else {
			// Log.e("Paul","In is not null");
		}
		mListOfItems = new ArrayList<String>();
		in.readStringList(mListOfItems);

	}
}
