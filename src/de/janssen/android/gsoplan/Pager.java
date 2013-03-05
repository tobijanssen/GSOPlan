package de.janssen.android.gsoplan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.view.MyArrayAdapter;

public class Pager
{
    public int weekDataIndexToShow;
    public ViewPager viewPager;
    public List<View> pages = new ArrayList<View>();
    public List<Calendar> pageIndex = new ArrayList<Calendar>();;
    public List<String> headlines = new ArrayList<String>();
    public PagerAdapter pageAdapter;
    public Boolean disableNextPagerOnChangedEvent = false;
    public TitlePageIndicator pageIndicator;
    public int currentPage;
    public Boolean pagerReady = false;
    public MyArrayAdapter adapter;
}
