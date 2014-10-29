package com.repkap11.runetrack;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import com.repkap11.runetrack.fragments.*;

import java.text.*;
import java.util.*;

public class PiChart extends View implements OnTouchListener {
private static final String TAG = "PiChart";
RectF rectf = new RectF(0, 0, 0, 0);
float temp = 0;
private Point mCenterPoint = new Point(0, 0);
private int mDiameter;
private MainActivity mActivity;
private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
private int[] colors;
private String[] skillNames;
private Toast mToast;
private int[] xpPerSkill;
private float[] mDegrees;
private int mPreviousIndex;

public PiChart(Context context, AttributeSet attrs) {
	super(context, attrs);
	mActivity = (MainActivity) context;
	this.setOnTouchListener(this);
}

@Override
protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	if(mDegrees != null) {
		temp = 0;
		for(int i = 0; i < mDegrees.length; i++) {// values2.length;
			// i++) {
			paint.setColor(colors[i % colors.length]);
			if(i != mPreviousIndex) {
				paint.setAlpha(153);
			}
			// paint.setColor(Color.rgb(rand.nextInt(256),
			// rand.nextInt(256), rand.nextInt(256)));
			canvas.drawArc(rectf, (temp + 90) % 360, mDegrees[i], true, paint);
			temp += mDegrees[i];
		}
		Log.e(TAG, "EndAngle:" + temp);
	}
}

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// Try for a width based on our minimum
	int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
	int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

	// Whatever the width ends up being, ask for a height that would let the
	// pie
	// get as big as it can
	//int minh = MeasureSpec.getSize(w) - (int) 10 + getPaddingBottom() + getPaddingTop();
	int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int) 10, heightMeasureSpec, 0);// Account
	// for
	// padding
	float xpad = (float) (getPaddingLeft() + getPaddingRight());
	float ypad = (float) (getPaddingTop() + getPaddingBottom());

	// Account for the label
	// if (mShowText) xpad += mTextWidth;

	float ww = (float) w - xpad;
	float hh = (float) h - ypad;

	// Figure out how big we can make the pie
	mDiameter = (int) Math.min(ww, hh);
	rectf.set(0, 0, mDiameter, mDiameter);
	mCenterPoint.set(mDiameter / 2, mDiameter / 2);
	setMeasuredDimension(mDiameter, mDiameter);
}

public void setPiChartData(int[] xpPerSkill, int[] colors, String[] skillNames) {
	this.xpPerSkill = xpPerSkill;
	this.colors = colors;
	this.skillNames = skillNames;
	this.mPreviousIndex = -1;
	this.mDegrees = new float[xpPerSkill.length];
	int totalxp = 0;
	for(int i = 0; i < xpPerSkill.length; i++) {
		totalxp += xpPerSkill[i];
	}
	for(int i = 0; i < mDegrees.length; i++) {
		mDegrees[i] = (float) xpPerSkill[i] * 360 / (float) totalxp;
		// Log.e("Paul", "Angle::"+degrees[i]+":"+skillNames[i]);
	}
	invalidate();
}

@Override
public boolean onTouch(View v, MotionEvent event) {
	boolean returnValue = false;
	switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			((XpDistributionChartFragment) (mActivity.mCurrentFragment)).mPullToRefreshLayout.setEnabled(false);
		case MotionEvent.ACTION_MOVE:
			returnValue = showTouchMessage(event);
			// Handle Touch Move
			break;
		case MotionEvent.ACTION_UP:
			((XpDistributionChartFragment) (mActivity.mCurrentFragment)).mPullToRefreshLayout.setEnabled(true);
			if(mToast != null) {
				mToast.cancel();
				mPreviousIndex = -1;
				invalidate();
			}
			break;
	}
	return true;
}

private boolean showTouchMessage(MotionEvent event) {
	if(mToast != null) {
		mToast.cancel();
	}
	mToast = Toast.makeText(this.getContext(), "", Toast.LENGTH_LONG);

	double angle = getAngle(event.getX(), event.getY());

	double curAngle = 0;
	int index = -1;
	while(curAngle < angle && index < mDegrees.length - 1) {
		index++;
		curAngle += mDegrees[index];

	}
	double distance = getDistance(event.getX(), event.getY());
	if((distance < mDiameter / 2)) {
		// String text = "Touch Down x=" + event.getX() + "  y=" +
		// event.getY() + " Angle=" + angle + " Skill=" + skillNames[index];
		String text = skillNames[index] + "\n" + NumberFormat.getNumberInstance(Locale.US).format(xpPerSkill[index]) + " XP (" + String.format("%.2f", mDegrees[index] / 3.60) + "%)";
		// Log.e(TAG, text);
		// mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setText(text);
		mToast.show();
		if(mPreviousIndex != index) {
			invalidate();
		}
		mPreviousIndex = index;
		return true;
	}else {
		if(mPreviousIndex != -1) {
			invalidate();
		}
		mPreviousIndex = -1;
		return true;
	}

}

public double getAngle(float x, float y) {
	double dx = x - mCenterPoint.x;
	// Minus to correct for coord re-mapping
	double dy = -(y - mCenterPoint.y);

	return (Math.toDegrees(Math.atan2(dx, dy))) + 180;

	// We need to map to coord system when 0 degree is at 3 O'clock, 270 at
	// 12 O'clock
}

public double getDistance(float x, float y) {
	double dx = x - mCenterPoint.x;
	double dy = -(y - mCenterPoint.y);
	return Math.sqrt(dx * dx + dy * dy);
}
}
