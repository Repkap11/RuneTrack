package com.repkap11.runetrack;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.*;
import android.widget.*;

public class PiChart extends View {
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

		// Figure out how big we can make the pie.
		int diameter = (int) Math.min(ww, hh);
		rectf = new RectF(0, 0, diameter, diameter);
		setMeasuredDimension(diameter, diameter);
	}

	public PiChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private float[] value_degree;
	private int[] colors;
	RectF rectf;
	float temp = 0;

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (value_degree != null) {
			temp = 0;
			for (int i = 0; i < value_degree.length; i++) {// values2.length;
															// i++) {
				paint.setColor(colors[i%colors.length]);
				//paint.setColor(Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
				canvas.drawArc(rectf, (temp+90) % 360, value_degree[i], true, paint);
				temp += value_degree[i];
			}
			Log.e("Paul","EndAngle:"+temp);
		}
	}

	public void setPiChartData(float[] degrees, int[] colors) {
		this.value_degree = degrees;
		this.colors = colors;
		invalidate();
	}
	private Toast mToast;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float x = event.getX();
		float y = event.getY();
		if (mToast != null){
			mToast.cancel();
		}
		if (mToast == null){
			mToast = Toast.makeText(getContext(),"" ,Toast.LENGTH_SHORT);
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mToast.setText("Touch Down x="+x+"  y="+y );
				//Handle Touch Down
				break;
			case MotionEvent.ACTION_MOVE:
				mToast.setText("Touch x="+x+"  y="+y );
				//Handle Touch Move
				break;
			case MotionEvent.ACTION_UP:
				mToast.setText("Touch Up x="+x+"  y="+y);
				//Handle Touch Up
				break;
		}
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.show();
		return false;
	}
}
