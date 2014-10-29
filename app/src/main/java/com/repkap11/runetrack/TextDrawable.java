package com.repkap11.runetrack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.TypedValue;

public class TextDrawable extends Drawable {

private final String text;
private final Paint paint;

public TextDrawable(Resources resources,int textID) {

	this.text = resources.getString(textID);
	this.paint = new Paint();
	paint.setColor(Color.BLACK);
	float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, resources.getDisplayMetrics());
	paint.setTextSize(px);
	paint.setAntiAlias(true);
	paint.setFakeBoldText(false);
	//paint.setShadowLayer(6f, 0, 0, Color.BLACK);
	paint.setStyle(Paint.Style.FILL);
	paint.setTextAlign(Paint.Align.CENTER);
}

@Override
public void draw(Canvas canvas) {
	//Log.e("TextDrawalbe", "Text Drawable drew");
	canvas.drawText(text, canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
}

@Override
public void setAlpha(int alpha) {
	paint.setAlpha(alpha);
}

@Override
public void setColorFilter(ColorFilter cf) {
	paint.setColorFilter(cf);
}

@Override
public int getOpacity() {
	return PixelFormat.TRANSLUCENT;
}
}
