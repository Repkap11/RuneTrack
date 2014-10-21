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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.repkap11.runetrack.DownloadIntentService;
import com.repkap11.runetrack.MainActivity;
import com.repkap11.runetrack.PiChart;
import com.repkap11.runetrack.R;

public class XpDistributionChartFragment extends FragmentBase {

    public static final String TAG = " XpDistributionChartFragment";
    public int[] downloadResult;
    public int[] downloadResult2;
    private PiChart mPiChart;
    private ResponseReceiver receiver;
    private boolean needsDownload = true;
    private boolean needsToShowDownloadFailure = false;
    private String userName;
    private int skillNumber;
    private String[] downloadResult3;
    private ViewSwitcher switcherUserGainedNoXP;
    private boolean needsToShowUserGainedNoXP;
    public XpDistributionChartFragment() {
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
    public void onPause() {
        super.onPause();
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
    }

    @Override
    public void reloadData() {
        ((MainActivity) this.getActivity()).selectPiChart(userName);
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
            msgIntent.putExtra(DownloadIntentService.PARAM_USERNAME, userName);
            msgIntent.putExtra(DownloadIntentService.PARAM_SKILL_NUMBER, skillNumber);
            msgIntent.putExtra(DownloadIntentService.PARAM_WHICH_DATA, DownloadIntentService.PARAM_XP_PI_CHART);
            this.getActivity().startService(msgIntent);
            IntentFilter filter = new IntentFilter(DownloadIntentService.PARAM_USERNAME);

            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ResponseReceiver();
            getActivity().registerReceiver(receiver, filter);

        } else {

            if (needsToShowDownloadFailure) {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
                if (needsToShowUserGainedNoXP) {
                    switcherUserGainedNoXP.setDisplayedChild(1);
                } else {
                    switcherUserGainedNoXP.setDisplayedChild(0);
                }
            } else {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(DownloadIntentService.PARAM_XP_COLORS, downloadResult2);
        outState.putIntArray(DownloadIntentService.PARAM_XP_PER_SKILL, downloadResult);
        outState.putStringArray(DownloadIntentService.PARAM_XP_SKILL_NAMES, downloadResult3);
        outState.putBoolean(DownloadIntentService.PARAM_XP_USER_GAINED_NO_XP, needsToShowUserGainedNoXP);
        outState.putBoolean("needsToShowDownloadFailure", needsToShowDownloadFailure);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userName = getArguments().getString(MainActivity.ARG_USERNAME);
        skillNumber = getArguments().getInt(MainActivity.ARG_SKILL_NUMBER);
        View rootView = inflater.inflate(R.layout.fragment_xp_pi_chart, container, false);
        //View textViewUserGainedNoXP = rootView.findViewById(R.id.user_gained_no_xp_error_message);
        //textViewUserGainedNoXP.setText(userName + " gained no xp this week.");
        switcherUserGainedNoXP = (ViewSwitcher) rootView.findViewById(R.id.switcher_user_got_no_xp);
        onCreatePostSetContentView(rootView,R.string.xp_pi_chart_download_error_message);
        mPiChart = ((PiChart) rootView.findViewById(R.id.xp_pi_chart_content));

        getActivity().setTitle(userName);
        needsDownload = true;

        if (savedInstanceState != null) {

            needsToShowDownloadFailure = savedInstanceState.getBoolean("needsToShowDownloadFailure");
            Log.e(TAG, "needsToShowDownloadFailure updated from state : " + needsToShowDownloadFailure);
            downloadResult = savedInstanceState.getIntArray(DownloadIntentService.PARAM_XP_PER_SKILL);
            downloadResult2 = savedInstanceState.getIntArray(DownloadIntentService.PARAM_XP_COLORS);
            downloadResult3 = savedInstanceState.getStringArray(DownloadIntentService.PARAM_XP_SKILL_NAMES);
            needsToShowUserGainedNoXP = savedInstanceState.getBoolean(DownloadIntentService.PARAM_XP_USER_GAINED_NO_XP);
            Log.e(TAG, "needsToShowUserGainedNoXP updated from state : " + needsToShowUserGainedNoXP);
            if (downloadResult != null) {
                needsDownload = false;
                applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
            }
            if (needsToShowUserGainedNoXP) {
                needsDownload = false;
                Log.e(TAG, "Hit this place");
                // switcherFailure.setDisplayedChild(1);
                // switcherUserGainedNoXP.setDisplayedChild(1);
                // switcherContent.setDisplayedChild(0);
            }
        }
        return rootView;
    }

    public void applyDownloadResult(final int[] downloadResult4, final int[] downloadResult2, String[] downloadResult3) {
        refreshComplete();
        mPiChart.setPiChartData(downloadResult4, downloadResult2, downloadResult3);

    }

    @Override
    protected boolean isWaitingForData() {
        return false;
    }

    @Override
    public boolean canScrollUp() {
        //Log.e(TAG,"XP Chart Scroll called, returned false");
        return false;
    }

    public class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Log.e(TAG, "before crash");
            downloadResult = intent.getIntArrayExtra(DownloadIntentService.PARAM_XP_PER_SKILL);
            downloadResult2 = intent.getIntArrayExtra(DownloadIntentService.PARAM_XP_COLORS);
            downloadResult3 = intent.getStringArrayExtra(DownloadIntentService.PARAM_XP_SKILL_NAMES);
            needsToShowUserGainedNoXP = intent.getBooleanExtra(DownloadIntentService.PARAM_XP_USER_GAINED_NO_XP, needsToShowUserGainedNoXP);
            Log.e(TAG, "needsToShowUserGainedNoXP:" + needsToShowUserGainedNoXP);
            if (downloadResult == null || downloadResult.length == 0) {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_RETRY);
                needsToShowDownloadFailure = true;
                if (needsToShowUserGainedNoXP) {
                    switcherUserGainedNoXP.setDisplayedChild(1);
                } else {
                    switcherUserGainedNoXP.setDisplayedChild(0);
                }
                // Toast.makeText(HistoryGraphFragment.this.getActivity(),
                // "Failure", Toast.LENGTH_SHORT).show();
            } else {
                setSwitchedView(FragmentBase.SWITCHED_VIEW_CONTENT);
                applyDownloadResult(downloadResult, downloadResult2, downloadResult3);
            }
        }
    }
}
