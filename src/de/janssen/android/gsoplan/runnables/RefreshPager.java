/*
 * RefreshPager.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.runnables;

import java.util.Calendar;

import de.janssen.android.gsoplan.MyContext;
import de.janssen.android.gsoplan.Tools;
import de.janssen.android.gsoplan.view.MyPagerAdapter;

public class RefreshPager implements Runnable
{

    private MyContext ctxt;
    private Boolean smoothScroll;

    public RefreshPager(MyContext ctxt, Boolean smoothScroll)
    {
	this.ctxt = ctxt;
	this.smoothScroll = smoothScroll;
    }

    @Override
    public void run()
    {
	if (ctxt.pager.pagerReady)
	{
	    int newPage;
	    if (ctxt.weekView)
	    {
		newPage = Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate, Calendar.WEEK_OF_YEAR);
	    }
	    else
	    {
		newPage = Tools.getPage(ctxt.pager.pageIndex, ctxt.getCurStupid().currentDate, Calendar.DAY_OF_YEAR);
	    }
	    ctxt.pager.currentPage = ctxt.pager.viewPager.getCurrentItem();
	    if (newPage != ctxt.pager.currentPage)// newPage != (parent.currentPage
					    // -1) && newPage !=
					    // (parent.currentPage + 1) &&
	    {

		if (newPage != 0) // Dort scheint ein bug zu sein; bei Seite 0
				  // wird onPageselected Event nicht gestartet,
				  // daher nicht disablen
		    ctxt.pager.disableNextPagerOnChangedEvent = true;

		ctxt.pager.currentPage = newPage;
		ctxt.pager.pageAdapter = new MyPagerAdapter(ctxt.pager.pages, ctxt.pager.headlines);

		ctxt.pager.viewPager.setAdapter(ctxt.pager.pageAdapter);
		ctxt.pager.viewPager.setCurrentItem(ctxt.pager.currentPage, smoothScroll);

		ctxt.pager.pageIndicator.setViewPager(ctxt.pager.viewPager);
		ctxt.pager.pageIndicator.notifyDataSetChanged();

		ctxt.pager.pageIndicator.setCurrentItem(newPage);
	    }
	}

    }

}
