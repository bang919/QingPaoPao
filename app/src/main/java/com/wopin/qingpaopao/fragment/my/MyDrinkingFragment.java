package com.wopin.qingpaopao.fragment.my;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.bean.response.DrinkListTodayRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTotalRsp;
import com.wopin.qingpaopao.fragment.BaseBarDialogFragment;
import com.wopin.qingpaopao.presenter.BasePresenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.disposables.Disposable;

public class MyDrinkingFragment extends BaseBarDialogFragment implements View.OnClickListener {

    public static final String TAG = "MyDrinkingFragment";
    private static final String DRINKING_TODAY = "DRINKING_TODAY";
    private static final String DRINKING_TOTAL = "DRINKING_TOTAL";
    private Disposable mDisposable;
    private View mTodayView;
    private View mThisWeekView;
    private View mThisMonth;
    private TextView mHeadTv, mDrinkingQuantityKey, mDrinkingQuantityValue, mDrinkingTimeKey, mDrinkingTimeValue;
    private TextView mDrinkingStatus;
    private LineChart mLineChart;

    private int mTodayDrinkCount, mWeekDrinkCount, mMonthDrinkCount;
    private int mWeekReachDays, mMonthReachDays;
    private String mTodayDrinkTime;

    private ArrayList<Entry> weekEntrys;
    private ArrayList<Entry> monthEntrys;
    private IValueFormatter mF;

    public static MyDrinkingFragment build(DrinkListTodayRsp drinkListTodayRsp, DrinkListTotalRsp drinkListTotalRsp) {
        MyDrinkingFragment myDrinkingFragment = new MyDrinkingFragment();
        Bundle args = new Bundle();
        args.putSerializable(DRINKING_TODAY, drinkListTodayRsp);
        args.putSerializable(DRINKING_TOTAL, drinkListTotalRsp);
        myDrinkingFragment.setArguments(args);
        return myDrinkingFragment;
    }


    @Override
    public void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    protected String setBarTitle() {
        return getString(R.string.my_drinking);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_my_drinking;
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initView(View rootView) {
        mHeadTv = rootView.findViewById(R.id.tv_table_head);

        mDrinkingQuantityKey = rootView.findViewById(R.id.drinking_quantity);
        mDrinkingQuantityValue = rootView.findViewById(R.id.value_drinking_quantity);
        mDrinkingTimeKey = rootView.findViewById(R.id.drinking_time);
        mDrinkingTimeValue = rootView.findViewById(R.id.value_drinking_time);
        mDrinkingStatus = rootView.findViewById(R.id.tv_drinking_status);
        mLineChart = rootView.findViewById(R.id.line_chart);

        mTodayView = rootView.findViewById(R.id.tv_today);
        mThisWeekView = rootView.findViewById(R.id.tv_this_week);
        mThisMonth = rootView.findViewById(R.id.tv_this_month);
        mTodayView.setOnClickListener(this);
        mThisWeekView.setOnClickListener(this);
        mThisMonth.setOnClickListener(this);
        setItemSelected(mTodayView);

        //设置chart是否可以触摸
        mLineChart.setTouchEnabled(false);
        //设置是否可以拖拽
        mLineChart.setDragEnabled(false);
        //设置是否可以缩放 x和y，默认true
        mLineChart.setScaleEnabled(false);
        //设置是否可以通过双击屏幕放大图表。默认是true
        mLineChart.setDoubleTapToZoomEnabled(false);

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setDrawGridBackground(false);

        mLineChart.getXAxis().setGranularity(1);
        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mLineChart.getAxisLeft().addLimitLine(new LimitLine(8));
        mLineChart.getAxisLeft().setAxisMinimum(0);

        mLineChart.getLegend().setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);
    }

    @Override
    protected void initEvent() {

        weekEntrys = new ArrayList<>();
        monthEntrys = new ArrayList<>();
        mF = new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return String.valueOf((int) value);
            }
        };
        DrinkListTodayRsp drinkListTodayRsp = (DrinkListTodayRsp) getArguments().getSerializable(DRINKING_TODAY);
        DrinkListTotalRsp drinkListTotalRsp = (DrinkListTotalRsp) getArguments().getSerializable(DRINKING_TOTAL);
        if (drinkListTodayRsp != null && drinkListTodayRsp.getResult() != null) {
            List<DrinkListTodayRsp.ResultBean.DrinksBean> todayDrinks = drinkListTodayRsp.getResult().getDrinks();
            mTodayDrinkCount = todayDrinks.size();
            mTodayDrinkTime = todayDrinks.get(mTodayDrinkCount - 1).getTime();
        }

        if (drinkListTotalRsp != null && drinkListTotalRsp.getResult() != null && drinkListTotalRsp.getResult() != null) {
            ArrayMap<String, DrinkListTotalRsp.ResultBean> totalDrinksMap = new ArrayMap<>();
            for (DrinkListTotalRsp.ResultBean resultBean : drinkListTotalRsp.getResult()) {
                totalDrinksMap.put(resultBean.getDate(), resultBean);
            }


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            final Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
            int dayOfMonthInt = c.get(Calendar.DAY_OF_MONTH);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);


            int dayNeedWeek = (dayOfWeek + 6) % 7;
            int dayNeedMonth = dayOfMonthInt;
            while (dayNeedWeek > 0 || dayNeedMonth > 0) {
                String dateKey = simpleDateFormat.format(c.getTime());
                DrinkListTotalRsp.ResultBean resultBean = totalDrinksMap.get(dateKey);
                int day = resultBean == null ? 0 : resultBean.getDrinks().size();
                if (dayNeedWeek > 0) {
                    weekEntrys.add(0, new Entry(dayNeedWeek - 1, day));
                    mWeekDrinkCount += day;
                    if (resultBean != null && resultBean.get__v() + 1 >= resultBean.getTarget()) {
                        mWeekReachDays++;
                    }
                    dayNeedWeek--;
                }
                if (dayNeedMonth > 0) {
                    monthEntrys.add(0, new Entry(dayNeedMonth - 1, day));
                    mMonthDrinkCount += day;
                    if (resultBean != null && resultBean.get__v() + 1 >= resultBean.getTarget()) {
                        mMonthReachDays++;
                    }
                    dayNeedMonth--;
                }
                c.add(Calendar.DATE, -1);
            }
        }


        mTodayView.performClick();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_today:
                setItemSelected(v);
                mHeadTv.setText(getString(R.string.drinking_statistics, getString(R.string.today)));
                mDrinkingQuantityKey.setText(R.string.drinking_quantity);
                mDrinkingQuantityValue.setText(getString(R.string.cup, mTodayDrinkCount));
                mDrinkingTimeKey.setText(R.string.last_drinking_time);
                mDrinkingTimeValue.setText(TextUtils.isEmpty(mTodayDrinkTime) ? "--" : mTodayDrinkTime);
                mDrinkingStatus.setVisibility(View.GONE);
                mLineChart.setVisibility(View.GONE);
                break;
            case R.id.tv_this_week:
                setItemSelected(v);
                mHeadTv.setText(getString(R.string.drinking_statistics, getString(R.string.this_week)));
                mDrinkingQuantityKey.setText(R.string.total_drinking_quantity);
                mDrinkingQuantityValue.setText(getString(R.string.cup, mWeekDrinkCount));
                mDrinkingTimeKey.setText(R.string.reach_day_count);
                mDrinkingTimeValue.setText(getString(R.string.day_number, String.valueOf(mWeekReachDays)));

                mDrinkingStatus.setVisibility(View.VISIBLE);
                mDrinkingStatus.setText(getString(R.string.drinking_status, getString(R.string.this_week)));
                mLineChart.setVisibility(View.VISIBLE);

                LineData data = new LineData();
                data.addDataSet(new LineDataSet(weekEntrys, getString(R.string.drink_every_title)));
                data.setValueFormatter(mF);
                mLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        String s = "星期天";
                        if (value <= 0) {
                            s = "星期一";
                        } else if (value <= 1) {
                            s = "星期二";
                        } else if (value <= 2) {
                            s = "星期三";
                        } else if (value <= 3) {
                            s = "星期四";
                        } else if (value <= 4) {
                            s = "星期五";
                        } else if (value <= 5) {
                            s = "星期六";
                        }
                        return s;
                    }
                });
                mLineChart.setData(data);
                mLineChart.invalidate();
                break;
            case R.id.tv_this_month:
                setItemSelected(v);
                mHeadTv.setText(getString(R.string.drinking_statistics, getString(R.string.this_month)));
                mDrinkingQuantityKey.setText(R.string.total_drinking_quantity);
                mDrinkingQuantityValue.setText(getString(R.string.cup, mMonthDrinkCount));
                mDrinkingTimeKey.setText(R.string.reach_day_count);
                mDrinkingTimeValue.setText(getString(R.string.day_number, String.valueOf(mMonthReachDays)));

                mDrinkingStatus.setVisibility(View.VISIBLE);
                mDrinkingStatus.setText(getString(R.string.drinking_status, getString(R.string.this_month)));
                mLineChart.setVisibility(View.VISIBLE);

                LineData data2 = new LineData();
                data2.addDataSet(new LineDataSet(monthEntrys, getString(R.string.drink_every_title)));
                data2.setValueFormatter(mF);
                mLineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return String.valueOf((int) (value + 1));
                    }
                });
                mLineChart.setData(data2);
                mLineChart.invalidate();
                break;
        }
    }

    private void setItemSelected(View view) {
        mTodayView.setSelected(mTodayView == view);
        mThisWeekView.setSelected(mThisWeekView == view);
        mThisMonth.setSelected(mThisMonth == view);
    }
}
