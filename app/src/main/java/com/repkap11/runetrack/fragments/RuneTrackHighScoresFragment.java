/**
 * HistoryGraphFragment.java
 * $Id:$
 * $Log:$
 * @author Paul Repka psr2608
 */
package com.repkap11.runetrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.repkap11.runetrack.DataTable;
import com.repkap11.runetrack.DataTableBounds;
import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class RuneTrackHighScoresFragment extends FragmentBase {

    public static final String TAG = "RuneTrackHighScoresFragment";
    public ArrayList<Parcelable> downloadResult;
    private ListView mProgressHolder;
    private ResponseReceiver receiver;
    private boolean needsDownload = true;
    private boolean needsToShowDownloadFailure = false;
    private int pageNumber;
    private String skillName;
    public RuneTrackHighScoresFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onDetach() {
        Log.e(TAG, "Fragment Detached");
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history_graph, container, false);
        skillName = getArguments().getString(MainActivity.ARG_SKILL_NAME);
        pageNumber = getArguments().getInt(MainActivity.ARG_PAGE_NUMBER);
        mProgressHolder = ((ListView) rootView.findViewById(R.id.history_graph_content));
        onCreatePostSetContentView(rootView,R.string.highscores_download_error_message);
        getActivity().setTitle(getResources().getString(R.string.runetrack_highscores));
        needsDownload = true;

        if (savedInstanceState != null) {

            needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
            Log.e(TAG, "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
            downloadResult = savedInstanceState.getParcelableArrayList(DownloadIntentService.PARAM_HIGH_SCORES_ENTRIES);
            if (downloadResult != null) {
                needsDownload = false;
                applyDownloadResult(downloadResult);
            }
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Log.e(TAG, "onResume needsDownloadFailure " +
        // needsToShowDownloadFailure);
        // Log.e(TAG, "onResume needsDownload " + needsDownload);
        if (needsDownload) {
            setSwitchedView(FragmentBase.SWITCHED_VIEW_SPINNER);
            Intent msgIntent = new Intent(this.getActivity(), DownloadIntentService.class);
            msgIntent.putExtra(DownloadIntentService.PARAM_PAGE_NUMBER, pageNumber);
            msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NAME, skillName);
            msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_RUNETRACK_HIGH_SCORES);
            this.getActivity().startService(msgIntent);
            IntentFilter filter = new IntentFilter(DownloadIntentService.PARAM_USERNAME);

            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ResponseReceiver();
            getActivity().registerReceiver(receiver, filter);

        } else {

            if (needsToShowDownloadFailure) {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
            } else {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
    }

    @Override
    public void reloadData() {
        ((MainActivity) this.getActivity()).selectRuneTrackHighScores(skillName, pageNumber);
    }

    @Override
    protected boolean isWaitingForData() {
        return false;
    }

    @Override
    public boolean canScrollUp() {
        return mProgressHolder.canScrollVertically(-1);
    }

    public void applyDownloadResult(ArrayList<Parcelable> result) {
        refreshComplete();
        mProgressHolder.setAdapter(new ArrayAdapter<Parcelable>(getActivity(), R.layout.fragment_history_graph, result) {
            private DataTableBounds bounds;

            @Override
            public int getItemViewType(int position) {
                if (position == 0) {
                    return 0;
                }
                if (position == 1) {
                    return 1;
                }
                return 2;
            }

            @Override
            public int getViewTypeCount() {
                return 3;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Log.e(TAG,"Get view called for :"+position);
                if (position == 0) {
                    if (convertView != null) {
                        return convertView;
                    } else {
                        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = inflater.inflate(R.layout.fragment_high_score_table_next_previous, mProgressHolder, false);
                        Button next = (Button) convertView.findViewById(R.id.runetrack_high_score_next_button);
                        Button previous = (Button) convertView.findViewById(R.id.runetrack_high_score_previous_button);
                        if (RuneTrackHighScoresFragment.this.pageNumber == 1) {
                            previous.setVisibility(View.GONE);
                        }
                        next.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RuneTrackHighScoresFragment.this.pageNumber++;
                                reloadData();
                            }
                        });
                        previous.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                RuneTrackHighScoresFragment.this.pageNumber--;
                                reloadData();
                            }
                        });

                        return convertView;
                    }
                }
                if (position == 1 && convertView != null) {
                    return convertView;
                }
                if (convertView == null) {
                    ImageView skillIcon;
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.fragment_high_score_table_entry, mProgressHolder, false);
                    skillIcon = (ImageView) convertView.findViewById(R.id.fragment_table_skill_image);
                    if (!((DataTable) (this.getItem(position))).mListOfItems.get(0).equals("") && position != 1) {
                        //String iconName = ((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault());
                        // Log.e(TAG,"Loading Icon: "+iconName);
                        Drawable imageIcon = getResources().getDrawable(
                                getResources().getIdentifier(
                                        ((DataTable) this.getItem(position)).mListOfItems.get(0).toLowerCase(Locale.getDefault()), "drawable",
                                        getActivity().getPackageName()));
                        skillIcon.setImageDrawable(imageIcon);
                    } else {
                        skillIcon.setImageResource(android.R.color.transparent);

                    }

                }
                if (bounds == null) {
                    bounds = calculateLayoutSize(this, getActivity(), (ListView) parent);
                }
                ArrayList<String> skill = ((DataTable) this.getItem(position)).mListOfItems;
                View holderOfStrings = convertView.findViewById(R.id.text_lin_layout);
                ArrayList<View> outVar = new ArrayList<View>();
                holderOfStrings.findViewsWithText(outVar, "Temp Text", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                for (int m = 1; m < skill.size(); m++) {
                    final TextView skillName = (TextView) outVar.get(outVar.size() - m);
                    int dim = (int) ((bounds.width) / (bounds.total + IMAGE_CHAR_SIZE) * (bounds.totals[m - 1]));
                    final String realText = String.format("%1$" + bounds.totals[m - 1] + "s", skill.get(m));
                    // realText = realText.replace(' ', '%');
                    skillName.setText(realText);
                    if (m == 1) {
                        skillName.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ((MainActivity) RuneTrackHighScoresFragment.this.getActivity()).selectUserProfileByName(realText);
                            }
                        });
                    }
                    // }
                    skillName.setTextSize(TypedValue.COMPLEX_UNIT_PX, bounds.textSize);
                    skillName.setWidth(dim);
                }
                return convertView;
            }

        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (downloadResult != null) {
            outState.putParcelableArrayList(DownloadIntentService.PARAM_HIGH_SCORES_ENTRIES, downloadResult);
            outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
        }
    }

    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.e(TAG, "before crash");
            downloadResult = intent.getParcelableArrayListExtra(DownloadIntentService.PARAM_HIGH_SCORES_ENTRIES);
            if (downloadResult == null || downloadResult.size() == 0) {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
                needsToShowDownloadFailure = true;
                // Toast.makeText(HistoryGraphFragment.this.getActivity(),
                // "Failure", Toast.LENGTH_SHORT).show();
            } else {
                downloadResult.add(0, new DataTable(
                        new ArrayList<String>(Arrays.asList(new String[]{"RT Rank", "Name", "RS Rank", "Level", "XP"}))));
                downloadResult.add(0, new DataTable(new ArrayList<String>(Arrays.asList(new String[]{"", "", "", "", ""}))));
                setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
                applyDownloadResult(downloadResult);
            }
        }
    }
}
