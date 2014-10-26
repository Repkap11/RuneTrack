package com.repkap11.runetrack;

import android.util.*;
import android.view.*;
import android.widget.*;

import java.util.*;

/**
 * Created by paul on 10/19/14.
 */
public class DataTableViewHolder {
private static final String TAG = DataTableViewHolder.class.getSimpleName();
public ArrayList<View> mTextViews;
public ImageView mImageView;

public DataTableViewHolder(ImageView skillIcon, ArrayList<View> outVar) {
	if(skillIcon == null) {
		Log.e(TAG, "Error, dont cache a null imageview");
	}
	this.mImageView = skillIcon;
	this.mTextViews = outVar;
}
}
