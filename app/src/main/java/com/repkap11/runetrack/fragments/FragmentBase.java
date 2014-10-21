/**
 * FragmentBase.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;
import com.repkap11.runetrack.TextDrawable;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class FragmentBase extends Fragment implements
        OnRefreshListener {
    protected static final int SWITCHED_VIEW_SPINNER = 0;
    protected static final int SWITCHED_VIEW_RETRY = 1;
    protected static final int SWITCHED_VIEW_CONTENT = 2;
    private static final String TAG = "FragmentBase";
    private TextView failureRetryButton;
    public PullToRefreshLayout mPullToRefreshLayout;
    private ViewSwitcher switcherOutside;
    private ViewSwitcher switcherInside;
    protected static final int IMAGE_CHAR_SIZE = 3;

    public static DataTableBounds calculateLayoutSize(ArrayAdapter<Parcelable> arrayAdapter, Context context, ListView view) {
        // Log.e(TAG, "Calculating Bounds");
        Display d = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        int oldwidth = p.x;
        //int width = oldwidth - view.getPaddingLeft() - view.getPaddingRight() - ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin
        //        - ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
        int width = oldwidth - view.getPaddingLeft() - view.getPaddingRight();
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
        int imageSize = (int) ((width) / (total + IMAGE_CHAR_SIZE) * IMAGE_CHAR_SIZE);// image takes
        // two
        // spaces
        // worth of
        // size
        float textSize = refitText(" ", (width) / (total + IMAGE_CHAR_SIZE) * 1);
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
        return (int) (dp * scale + 0.5f);
    }

    public static void makeTextViewHyperlink(TextView tv) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(tv.getText());
        ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb, TextView.BufferType.SPANNABLE);
    }

    protected abstract boolean isWaitingForData();

    private void failureRetryOnClick(View v) {
        reloadData();
    }

    public abstract void reloadData();

    protected void onCreatePostSetContentView(View rootView, int errorMessageResource) {
        /*
        failureRetryButton = (TextView) rootView.findViewById(R.id.user_profile_error_message);
        failureRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failureRetryOnClick(v);
            }
        });
        */

        switcherOutside = (ViewSwitcher) rootView.findViewById(R.id.switcher_outside);
        switcherInside = (ViewSwitcher) rootView.findViewById(R.id.switcher_inside);
        mPullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.swipe_container);
        View view = switcherInside.getChildAt(0);
        View[] viewsToAdd;
        if (view instanceof ListView) {
            ListView lv = ((ListView) switcherInside.getChildAt(0));
            lv.setBackground(new TextDrawable(getResources().getString(errorMessageResource)));
            viewsToAdd = new View[]{switcherInside.getChildAt(1), switcherInside.getChildAt(0)};
        } else if (view instanceof ViewSwitcher) {
            ViewSwitcher vs = ((ViewSwitcher) switcherInside.getChildAt(0));
            RelativeLayout rl = ((RelativeLayout) switcherInside.getChildAt(1));
            View view0 = vs.getChildAt(0);
            View view1 = vs.getChildAt(1);
            ListView view2 = (ListView)rl.getChildAt(0);
            viewsToAdd = new View[]{switcherInside.getChildAt(1), view0, view1, view2};
        } else {
            viewsToAdd = new View[]{switcherInside.getChildAt(1)};
        }
        ActionBarPullToRefresh.from(getActivity())
                .theseChildrenArePullable(viewsToAdd)
                .listener(this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onRefreshStarted(View view) {
        reloadData();
    }

    protected void refreshComplete() {
        ((MainActivity) getActivity()).refreshComplete();
    }

    public abstract boolean canScrollUp();

    protected void setSwitchedView(int state) {
        switch (state) {
            case SWITCHED_VIEW_SPINNER:
                switcherOutside.setDisplayedChild(0);
                break;
            case SWITCHED_VIEW_RETRY:
                switcherOutside.setDisplayedChild(1);
                switcherInside.setDisplayedChild(0);
                break;
            case SWITCHED_VIEW_CONTENT:
                switcherOutside.setDisplayedChild(1);
                switcherInside.setDisplayedChild(1);
                break;
        }
    }
}
