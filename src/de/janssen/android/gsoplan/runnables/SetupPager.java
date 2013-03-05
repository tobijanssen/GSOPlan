/*
 * SetupPager.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.viewpagerindicator.TitlePageIndicator;
import de.janssen.android.gsoplan.Const;
import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.R;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.view.MyPagerAdapter;

public class SetupPager implements Runnable
{

    private MyContext ctxt;
    private List<Integer> states = new ArrayList<Integer>();
    private Integer lastPage = 0;

    public SetupPager(MyContext ctxt, List<Calendar> pageIndex)
    {
	this.ctxt = ctxt;
    }

    @Override
    public void run()
    {

	if (ctxt.weekView)
	{
	    ctxt.pager.currentPage = Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate, Calendar.WEEK_OF_YEAR);
	}
	else
	{
	    ctxt.pager.currentPage = Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate, Calendar.DAY_OF_YEAR);
	}
	ctxt.pager.pageAdapter = new MyPagerAdapter(ctxt.pager.pages, ctxt.pager.headlines);
	ctxt.pager.viewPager = (ViewPager) ctxt.activity.findViewById(R.id.pager);
	ctxt.pager.viewPager.setAdapter(ctxt.pager.pageAdapter);
	
	ctxt.pager.pageIndicator = (TitlePageIndicator) ctxt.activity.findViewById(R.id.indicator);
	ctxt.pager.pageIndicator.setViewPager(ctxt.pager.viewPager);
	ctxt.pager.disableNextPagerOnChangedEvent = true;
	ctxt.pager.pageIndicator.setOnPageChangeListener(new OnPageChangeListener()
	{

	    @Override
	    public void onPageScrollStateChanged(int state)
	    {
		// die states katalogisieren
		if (state == 1)
		    states.add(state);
		// bei state 2 wurde ein page turn gemacht
		if (state == 2)
		    states.add(state);
		// bei state 0 ist das ende des evtl. turns
		if (state == 0)
		{
		    // prüfen, ob der vorgänger die 1 war(denn dann wurde kein
		    // Pagetrun gemacht
		    if (states.get(states.size() - 1) == 1 && states.size() == 1)
		    {
			// kein Pageturn gemacht(also ende erreicht)
			// prüfen, ob anfang, oder ende
			if (ctxt.pager.viewPager.getCurrentItem() == 0)
			{
			    if (ctxt.pager.headlines.size() - 1 == 0)
			    {
				// anfang& ende
				if (ctxt.weekView)
				{
				    Calendar selectedWeek = ctxt.pager.pageIndex.get(0);
				    ctxt.getCurStupid().currentDate = (Calendar) selectedWeek.clone();

				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.NEXTWEEK);
				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.LASTWEEK);
				}
				else
				{
				    Calendar selectedDay = ctxt.pager.pageIndex.get(0);
				    ctxt.getCurStupid().currentDate = (Calendar) selectedDay.clone();
				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.NEXTWEEK);
				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.LASTWEEK);

				}
			    }
			    else
			    {
				// anfang
				if (ctxt.weekView)
				{
				    Calendar selectedWeek = ctxt.pager.pageIndex.get(0);
				    ctxt.getCurStupid().currentDate = (Calendar) selectedWeek.clone();

				    // ctxt.stupid.checkAvailibilityOfWeek(ctxt,Const.NEXTWEEK);
				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.LASTWEEK);
				}
				else
				{
				    Calendar selectedDay = ctxt.pager.pageIndex.get(0);
				    ctxt.getCurStupid().currentDate = (Calendar) selectedDay.clone();
				    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.LASTWEEK);

				}
			    }
			}
			else
			{
			    // ende erreicht
			    if (ctxt.weekView)
			    {
				Calendar selectedWeek = ctxt.pager.pageIndex.get(ctxt.pager.pageIndex.size() - 1);
				ctxt.getCurStupid().currentDate = (Calendar) selectedWeek.clone();
				ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.NEXTWEEK);
			    }
			    else
			    {
				Calendar selectedDay = ctxt.pager.pageIndex.get(ctxt.pager.pageIndex.size() - 1);
				ctxt.getCurStupid().currentDate = (Calendar) selectedDay.clone();
				ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.NEXTWEEK);
			    }
			}

		    }
		    // historie löschen
		    states.clear();
		}
	    }

	    @Override
	    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	    {

	    }

	    @Override
	    public void onPageSelected(int position)
	    {

		Calendar selectedDay = ctxt.pager.pageIndex.get(position);
		ctxt.getCurStupid().currentDate = (Calendar) selectedDay.clone();

		// prüfen, ob dieses Event unterdrückt werden sollte
		if (ctxt.pager.disableNextPagerOnChangedEvent)
		{
		    // wenn ja, den Listener wieder aktivieren
		    ctxt.pager.disableNextPagerOnChangedEvent = false;
		}
		else
		{
		    // wenn nicht, kann das event ausgeführt werden
		    if (ctxt.weekView)
		    {
			ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.THISWEEK);
		    }
		    else
		    {
			if (selectedDay.get(Calendar.DAY_OF_WEEK) == 6 && lastPage > position)
			{
			    // Freitag erreicht und von einem Donnerstag
			    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.THISWEEK);
			}
			else if (selectedDay.get(Calendar.DAY_OF_WEEK) == 2 && lastPage < position)
			{
			    // Anfang erreicht
			    ctxt.getCurStupid().checkAvailibilityOfWeek(ctxt, Const.THISWEEK);
			}
		    }
		}

		lastPage = position;
	    }

	});
	ctxt.pager.viewPager.setCurrentItem(ctxt.pager.currentPage);
	ctxt.pager.pagerReady = true;

    }

}
