package com.repkap11.runetrack;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

public class TableEntryView extends RelativeLayout {
    private int mNumberOfTextColumns;
    private String TAG = TableEntryView.class.getSimpleName();

    public TableEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TableEntry, 0, 0);
        try {
            mNumberOfTextColumns = a.getInt(R.styleable.TableEntry_numberOfTextColumns, 0);
        } finally {
            a.recycle();
        }
        int OFFSET = 47;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //setGravity(Gravity.RIGHT);
        for (int i = 0; i < mNumberOfTextColumns; i++) {
            View textElement = inflater.inflate(R.layout.fragment_table_text_element, this, false);
            textElement.setId(i+OFFSET);
            RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                newParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            } else {
                newParams.addRule(RelativeLayout.LEFT_OF, i+OFFSET - 1);
            }
            newParams.addRule(RelativeLayout.CENTER_VERTICAL);
            textElement.setLayoutParams(newParams);
            addView(textElement);
        }
        View imageElement = inflater.inflate(R.layout.fragment_table_image_element, this, false);
        RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        newParams.addRule(RelativeLayout.LEFT_OF,mNumberOfTextColumns -1 + OFFSET);
        newParams.addRule(RelativeLayout.ALIGN_TOP,0+OFFSET);
        newParams.addRule(RelativeLayout.ALIGN_BOTTOM,0+OFFSET);
        newParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        newParams.addRule(RelativeLayout.CENTER_VERTICAL);
        imageElement.setLayoutParams(newParams);
        addView(imageElement);

    }
}
