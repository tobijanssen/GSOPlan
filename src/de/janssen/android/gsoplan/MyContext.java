package de.janssen.android.gsoplan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.viewpagerindicator.TitlePageIndicator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

public class MyContext {
	public StupidCore stupid = new StupidCore();
	public Exception exception;
	public MyArrayAdapter adapter;
	public Calendar dateBackup;
	public Handler handler;
	public Boolean selfCheckIsRunning=false;
	public int weekDataIndexToShow;
	public WorkerQueue executor = new WorkerQueue();
	public List<View> pages = new ArrayList<View>();
	public List<Calendar> pageIndex = new ArrayList<Calendar>();;
	public List<String> headlines = new ArrayList<String>();
	public LayoutInflater inflater;
	public PagerAdapter pageAdapter;
	public ViewPager viewPager;
	public Boolean disableNextPagerOnChangedEvent = false;
	public TitlePageIndicator pageIndicator;
	public int currentPage;
	public Context context;
	public Activity activity;
	public Boolean weekView = false;
	public Boolean dayView = false;
	public Boolean initialLunch = true;
	public Boolean pagerReady = false;
	public int[] textSizes;
	public Class<?> defaultActivity;
	public Boolean forceView = false;
}
