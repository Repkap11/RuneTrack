package com.repkap11.runetrack;

import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class PiChart extends View implements OnTouchListener {
	private static final String TAG = "PiChart";
	private Point mCenterPoint;
	private int mDiameter;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Try for a width based on our minimum
		int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
		int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

		// Whatever the width ends up being, ask for a height that would let the
		// pie
		// get as big as it can
		int minh = MeasureSpec.getSize(w) - (int) 10 + getPaddingBottom() + getPaddingTop();
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
		rectf = new RectF(0, 0, mDiameter, mDiameter);
		mCenterPoint = new Point(mDiameter / 2, mDiameter / 2);
		setMeasuredDimension(mDiameter, mDiameter);
	}

	public PiChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOnTouchListener(this);
	}

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private int[] colors;
	RectF rectf;
	float temp = 0;
	private String[] skillNames;
	private Toast mToast;
	private int[] xpPerSkill;
	private float[] mDegrees;
	private int mPreviousIndex;

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (mDegrees != null) {
			temp = 0;
			for (int i = 0; i < mDegrees.length; i++) {// values2.length;
														// i++) {
				paint.setColor(colors[i % colors.length]);
				if (i != mPreviousIndex) {
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

	public void setPiChartData(int[] xpPerSkill, int[] colors, String[] skillNames) {
		this.xpPerSkill = xpPerSkill;
		this.colors = colors;
		this.skillNames = skillNames;
		this.mPreviousIndex = -1;
		this.mDegrees = new float[xpPerSkill.length];
		int totalxp = 0;
		for (int i = 0; i < xpPerSkill.length; i++) {
			totalxp += xpPerSkill[i];
		}
		for (int i = 0; i < mDegrees.length; i++) {
			mDegrees[i] = (float) xpPerSkill[i] * 360 / (float) totalxp;
			// Log.e("Paul", "Angle::"+degrees[i]+":"+skillNames[i]);
		}
		invalidate();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			showTouchMessage(event);
			// Handle Touch Move
			break;
		case MotionEvent.ACTION_UP:
			if (mToast != null) {
				mToast.cancel();
				mPreviousIndex = -1;
				invalidate();
			}
			break;
		}
		return true;
	}

	/**
	 * @param event
	 */
	private void showTouchMessage(MotionEvent event) {
		if (mToast != null) {
			mToast.cancel();
		}
		mToast = Toast.makeText(this.getContext(), "", Toast.LENGTH_LONG);

		double angle = getAngle(event.getX(), event.getY());

		double curAngle = 0;
		int index = -1;
		while (curAngle < angle && index -1< mDegrees.length) {
			index++;
			curAngle += mDegrees[index];
			
		}
		double distance = getDistance(event.getX(), event.getY());
		if ((distance < mDiameter / 2)) {
			// String text = "Touch Down x=" + event.getX() + "  y=" +
			// event.getY() + " Angle=" + angle + " Skill=" + skillNames[index];
			String text = skillNames[index] + "\n" + NumberFormat.getNumberInstance(Locale.US).format(xpPerSkill[index]) + " XP ("
					+ String.format("%.2f", mDegrees[index] / 3.60) + "%)";
			// Log.e(TAG, text);
			// mToast.setDuration(Toast.LENGTH_SHORT);
			mToast.setText(text);
			mToast.show();
			if (mPreviousIndex != index) {
				invalidate();
			}
			mPreviousIndex = index;
		} else {
			if (mPreviousIndex != -1) {
				invalidate();
			}
			mPreviousIndex = -1;
		}

	}

	public double getDistance(float x, float y) {
		double dx = x - mCenterPoint.x;
		double dy = -(y - mCenterPoint.y);
		return Math.sqrt(dx * dx + dy * dy);
	}

	public double getAngle(float x, float y) {
		double dx = x - mCenterPoint.x;
		// Minus to correct for coord re-mapping
		double dy = -(y - mCenterPoint.y);

		double inRads = (Math.toDegrees(Math.atan2(dx, dy))) + 180;

		// We need to map to coord system when 0 degree is at 3 O'clock, 270 at
		// 12 O'clock
		return inRads;
	}
}
