package com.repkap11.runetrack;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class DataTable implements Parcelable {
	public ArrayList<String> mListOfItems;

    public DataTable(ArrayList<String> strings) {
        mListOfItems = strings;
    }



    @Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeStringList(mListOfItems);
	}

	public static final Parcelable.Creator<DataTable> CREATOR = new Parcelable.Creator<DataTable>() {
		public DataTable createFromParcel(Parcel in) {
			return new DataTable(in);
		}

		@Override
		public DataTable[] newArray(int size) {
			return new DataTable[size];
		}
	};

	public DataTable(Parcel in) {
		if (in == null) {
		} else {
			// Log.e("Paul","In is not null");
		}
		mListOfItems = new ArrayList<String>();
		in.readStringList(mListOfItems);

	}
}
