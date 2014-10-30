package com.repkap11.runetrack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.TypedValue;

public class TextDrawable extends Drawable {

private String text;
private String text2;
private Paint paint;
private float pxTextSize = 0;

public TextDrawable(Resources resources,int textID) {
	setText(resources, textID);
	this.text2 = resources.getString(R.string.drag_down_to_try_again);
	this.paint = new Paint();
	paint.setColor(Color.BLACK);
	pxTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, resources.getDisplayMetrics());
	paint.setTextSize(pxTextSize);
	paint.setAntiAlias(true);
	paint.setFakeBoldText(false);
	//paint.setShadowLayer(6f, 0, 0, Color.BLACK);
	paint.setStyle(Paint.Style.FILL);
	paint.setTextAlign(Paint.Align.CENTER);

}
public void setText(Resources resources,int textID){
	this.text = resources.getString(textID);

}
@Override
public void draw(Canvas canvas) {
	//Log.e("TextDrawalbe", "Text Drawable drew");
	canvas.drawText(text, canvas.getWidth() / 2, (canvas.getHeight() / 2) - 0.75f*pxTextSize, paint);
	canvas.drawText(text2, canvas.getWidth() / 2, (canvas.getHeight() / 2) + 0.75f*pxTextSize, paint);
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
