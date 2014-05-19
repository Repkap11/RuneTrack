/**
 * FragmentBase.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import android.widget.*;
import android.text.*;
import android.text.style.*;

public class FragmentBase extends Fragment {
	private static final String TAG = "FragmentBase";

	public static DataTableBounds calculateLayoutSize(ArrayAdapter<Parcelable> arrayAdapter, Context context, ListView view) {
		// Log.e(TAG, "Calculating Bounds");
		Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		Point p = new Point();
		d.getSize(p);
		int oldwidth = p.x;
		int width = oldwidth - view.getPaddingLeft() - view.getPaddingRight() - ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin
				- ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
		// Log.e(TAG, "old:" + oldwidth + " new:" + width);
		int total = 0;
		int[] totals = new int[((DataTable) arrayAdapter.getItem(0)).mListOfItems.size() - 1];
		for (int i = 1; i < totals.length + 1; i++) {
			int max = 0;
			for (int j = 0; j < arrayAdapter.getCount(); j++) {
				int curSize = ((DataTable) arrayAdapter.getItem(j)).mListOfItems.get(i).length();
				if (max < curSize) {
					max = curSize;
				}
			}

			totals[i - 1] = max + 1;// +1 for padding text with a space
			// Log.e(TAG, "Totals[" + (i - 1) + "] = " + totals[i - 1]);
			total += max + 1;
		}
		// Log.e(TAG, "Total" + " = " + total);
		int imageSize = (int) ((width) / (total + 2) * 2);// image takes
															// two
															// spaces
															// worth of
															// size
		float textSize = refitText(" ", (width) / (total + 2) * 1);
		// Log.e(TAG, "textSize" + " = " +textSize);
		return new DataTableBounds(imageSize, totals, total, width, textSize);
	}

	public static float refitText(String text, int textWidth) {
		if (textWidth <= 0)
			return 0;
		float hi = 100;
		float lo = 2;
		final float threshold = 0.005f; // How close we have to be
		Paint testPaint = new Paint();

		while ((hi - lo) > threshold) {
			float size = (hi + lo) / 2;
			testPaint.setTextSize(size);
			testPaint.setTypeface(Typeface.MONOSPACE);
			if (testPaint.measureText(text) >= textWidth)
				hi = size; // too big
			else
				lo = size; // too small
		}
		// Use lo so that we undershoot rather than overshoot
		return lo;
	}

	public static int dpToPixals(Context context, int dp) {
		float scale = context.getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (dp * scale + 0.5f);
		return dpAsPixels;
	}
	public static void makeTextViewHyperlink(TextView tv) {
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		ssb.append(tv.getText());
		ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv.setText(ssb, TextView.BufferType.SPANNABLE);
	}
}
