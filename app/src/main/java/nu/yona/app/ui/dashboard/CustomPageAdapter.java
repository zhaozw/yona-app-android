/*
 *  Copyright (c) 2016 Stichting Yona Foundation
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 *
 */

package nu.yona.app.ui.dashboard;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import nu.yona.app.R;
import nu.yona.app.api.model.DayActivity;
import nu.yona.app.api.model.WeekActivity;
import nu.yona.app.customview.YonaFontTextView;
import nu.yona.app.customview.graph.SpreadGraph;
import nu.yona.app.enums.ChartTypeEnum;
import nu.yona.app.enums.GoalsEnum;
import nu.yona.app.ui.ChartItemHolder;

/**
 * Created by kinnarvasa on 13/06/16.
 */
public class CustomPageAdapter extends PagerAdapter {

    private Context mContext;
    private List<DayActivity> dayActivities;
    private List<WeekActivity> weekActivities;
    private YonaFontTextView goalScore;
    private YonaFontTextView goalDesc;
    private YonaFontTextView goalType;
    private SpreadGraph mSpreadGraph;
    private int SPREAD_CELL_MINUTE = 15;
    private int WEEK_DAYS = 7;

    /**
     * Instantiates a new Custom page adapter.
     *
     * @param context the context
     */
    public CustomPageAdapter(Context context) {
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        if (dayActivities != null) {
            return initiateDayActivityReport(collection, position);
        } else if (weekActivities != null) {
            return initiateWeekActivityReport(collection, position);
        } else {
            return null;
        }
    }

    private ViewGroup initiateDayActivityReport(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.detail_activity_fragment, collection, false);
        goalDesc = (YonaFontTextView) layout.findViewById(R.id.goalDesc);
        goalType = (YonaFontTextView) layout.findViewById(R.id.goalType);
        goalScore = (YonaFontTextView) layout.findViewById(R.id.goalScore);
        mSpreadGraph = (SpreadGraph) layout.findViewById(R.id.spreadGraph);
        DayActivity dayActivity = dayActivities.get(position);
        FrameLayout view = ((FrameLayout) layout.findViewById(R.id.graphView));
        view.addView(inflateActivityView(inflater, dayActivity.getChartTypeEnum(), layout));
        updateView(new ChartItemHolder(view, null, dayActivity.getChartTypeEnum()), null, dayActivity);
        collection.addView(layout);
        return layout;
    }

    private View inflateActivityView(LayoutInflater inflater, ChartTypeEnum chartTypeEnum, ViewGroup collection) {
        View layoutView;
        switch (chartTypeEnum) {
            case NOGO_CONTROL:
                layoutView = inflater.inflate(R.layout.nogo_chart_layout, collection, false);
                break;
            case TIME_BUCKET_CONTROL:
                layoutView = inflater.inflate(R.layout.time_budget_item, collection, false);
                break;
            case TIME_FRAME_CONTROL:
                layoutView = inflater.inflate(R.layout.time_frame_item, collection, false);
                break;
            default:
                layoutView = inflater.inflate(R.layout.goal_chart_item, collection, false);
                break;
        }

        return layoutView;
    }

    private ViewGroup initiateWeekActivityReport(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.detail_activity_fragment, collection, false);
        mSpreadGraph = (SpreadGraph) layout.findViewById(R.id.spreadGraph);
        WeekActivity weekActivity = weekActivities.get(position);
        goalDesc = (YonaFontTextView) layout.findViewById(R.id.goalDesc);
        goalType = (YonaFontTextView) layout.findViewById(R.id.goalType);
        goalScore = (YonaFontTextView) layout.findViewById(R.id.goalScore);
        layout.findViewById(R.id.week_chart).setVisibility(View.VISIBLE); // week control
        FrameLayout view = ((FrameLayout) layout.findViewById(R.id.graphView));
        GoalsEnum goalsEnum;
        goalsEnum = GoalsEnum.fromName(weekActivity.getYonaGoal().getType());
        if (goalsEnum == GoalsEnum.BUDGET_GOAL && weekActivity.getYonaGoal().getMaxDurationMinutes() == 0) {
            goalsEnum = GoalsEnum.NOGO;
        }
        view.addView(inflateActivityView(inflater, goalsEnum, layout));
        updateView(new ChartItemHolder(view, null, goalsEnum), weekActivity, null);
        collection.addView(layout);
        return layout;
    }

    private View inflateActivityView(LayoutInflater inflater, GoalsEnum chartTypeEnum, ViewGroup collection) {
        View layoutView;
        switch (chartTypeEnum) {
            case NOGO:
                layoutView = inflater.inflate(R.layout.nogo_chart_layout, collection, false);
                break;
            case BUDGET_GOAL:
                layoutView = inflater.inflate(R.layout.time_budget_item, collection, false);
                break;
            case TIME_ZONE_GOAL:
                layoutView = inflater.inflate(R.layout.time_frame_item, collection, false);
                break;
            default:
                layoutView = inflater.inflate(R.layout.goal_chart_item, collection, false);
                break;
        }

        return layoutView;
    }

    private void updateView(final ChartItemHolder holder, WeekActivity weekActivity, DayActivity dayActivity) {
        if (dayActivity != null) {
            switch (dayActivity.getChartTypeEnum()) {
                case TIME_FRAME_CONTROL:
                    loadTimeFrameControlForDay(dayActivity, holder);
                    break;
                case TIME_BUCKET_CONTROL:
                    loadTimeBucketControlForDay(dayActivity, holder);
                    break;
                case NOGO_CONTROL:
                    loadNoGoControlForDay(dayActivity, holder);
                    break;
                default:
                    break;
            }
        } else {
            switch (GoalsEnum.fromName(weekActivity.getYonaGoal().getType())) {
                case BUDGET_GOAL:
                    loadTimeBucketControlForWeek(weekActivity, holder);
                    break;
                case TIME_ZONE_GOAL:
                    loadTimeFrameControlForWeek(weekActivity, holder);
                    break;
                case NOGO:
                    loadNoGoControlForWeek(weekActivity, holder);
                    break;
            }
        }
        showSpreadGraph(dayActivity, weekActivity);
    }

    private void loadTimeFrameControlForDay(DayActivity dayActivity, ChartItemHolder holder) {
        int timeFrameGoalMinutes = (dayActivity.getYonaGoal().getSpreadCells().size() * 15) - dayActivity.getTotalActivityDurationMinutes();
        if (dayActivity.getTimeZoneSpread() != null) {
            holder.getTimeFrameGraph().chartValuePre(dayActivity.getTimeZoneSpread());
        }
        holder.getGoalType().setText(mContext.getString(R.string.score));
        holder.getGoalScore().setText(Math.abs(timeFrameGoalMinutes) + "");
        if (timeFrameGoalMinutes < 0) {
            holder.getGoalScore().setTextColor(ContextCompat.getColor(mContext, R.color.darkish_pink));
        } else {
            holder.getGoalScore().setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
    }

    private void loadTimeFrameControlForWeek(WeekActivity weekActivity, ChartItemHolder holder) {
        int timeFrameGoalMinutes = (weekActivity.getYonaGoal().getSpreadCells().size() * 15) - weekActivity.getTotalActivityDurationMinutes();
        if (weekActivity.getTimeZoneSpread() != null) {
            holder.getTimeFrameGraph().chartValuePre(weekActivity.getTimeZoneSpread());
        }
        holder.getGoalType().setText(mContext.getString(R.string.score));
        holder.getGoalScore().setText(Math.abs(timeFrameGoalMinutes) + "");
        if (timeFrameGoalMinutes < 0) {
            holder.getGoalScore().setTextColor(ContextCompat.getColor(mContext, R.color.darkish_pink));
        } else {
            holder.getGoalScore().setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
    }

    private void loadTimeBucketControlForDay(DayActivity dayActivity, ChartItemHolder holder) {
        int goalMinutes = ((int) dayActivity.getYonaGoal().getMaxDurationMinutes()) - dayActivity.getTotalActivityDurationMinutes();
        int maxDurationAllow = (int) dayActivity.getYonaGoal().getMaxDurationMinutes();
        if (maxDurationAllow > 0) {
            holder.getTimeBucketGraph().graphArguments(dayActivity.getTotalMinutesBeyondGoal(), (int) dayActivity.getYonaGoal().getMaxDurationMinutes(), dayActivity.getTotalActivityDurationMinutes());
        }
        holder.getGoalType().setText(mContext.getString(R.string.score));
        if (goalMinutes < 0) {
            holder.getGoalDesc().setText(mContext.getString(R.string.budgetgoalbeyondtime));
        } else {
            holder.getGoalDesc().setText(mContext.getString(R.string.budgetgoaltime));
        }
        if (goalMinutes < 0) {
            holder.getGoalScore().setTextColor(ContextCompat.getColor(mContext, R.color.darkish_pink));
        } else {
            holder.getGoalScore().setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
        holder.getGoalScore().setText(Math.abs(goalMinutes) + "");
    }

    private void loadTimeBucketControlForWeek(WeekActivity weekActivity, ChartItemHolder holder) {
        //TODO under discussion with Bastiaan
    }

    private void loadNoGoControlForDay(DayActivity dayActivity, ChartItemHolder holder) {
        if (dayActivity.getGoalAccomplished()) {
            holder.getNogoStatus().setImageResource(R.drawable.adult_happy);
            holder.getGoalDesc().setText(mContext.getString(R.string.nogogoalachieved));
        } else {
            holder.getNogoStatus().setImageResource(R.drawable.adult_sad);
            holder.getGoalDesc().setText(mContext.getString(R.string.nogogoalbeyond, dayActivity.getTotalMinutesBeyondGoal() + ""));
        }
        holder.getGoalType().setText(mContext.getString(R.string.score));
    }

    private void loadNoGoControlForWeek(WeekActivity weekActivity, ChartItemHolder holder) {
        //TODO under discussion with Bastiaan
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    private void showSpreadGraph(final DayActivity dayActivity, final WeekActivity weekActivity) {
        if (dayActivity != null && dayActivity.getTimeZoneSpread() != null) {
            mSpreadGraph.chartValuePre(dayActivity.getTimeZoneSpread());
        } else if (weekActivity != null && weekActivity.getTimeZoneSpread() != null) {
            mSpreadGraph.chartValuePre(weekActivity.getTimeZoneSpread());
        }
        goalType.setText(mContext.getString(R.string.spreiding));
        if (dayActivity != null) {
            goalScore.setText(dayActivity.getTotalActivityDurationMinutes() + "");
        }
        goalScore.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        goalDesc.setText(mContext.getString(R.string.goaltotalminute));
    }

    @Override
    public int getCount() {
        if (dayActivities != null) {
            return dayActivities.size();
        } else if (weekActivities != null) {
            return weekActivities.size();
        } else {
            return 0;
        }
    }

    /**
     * Notify data set changed.
     *
     * @param activityList the activity list
     */
    public void notifyDataSetChanged(List<?> activityList) {
        if (activityList != null && activityList.get(0) instanceof DayActivity) {
            dayActivities = (List<DayActivity>) activityList;
        } else {
            weekActivities = (List<WeekActivity>) activityList;
        }
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}