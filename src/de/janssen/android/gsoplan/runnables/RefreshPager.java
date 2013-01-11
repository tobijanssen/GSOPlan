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

public class RefreshPager implements Runnable{

	private MyContext ctxt;
	private Boolean smoothScroll;
	public RefreshPager(MyContext ctxt, Boolean smoothScroll){
		this.ctxt=ctxt;
		this.smoothScroll=smoothScroll;
	}
	
	@Override
	public void run() {
		if(ctxt.pagerReady)
		{
			int newPage;
			if(ctxt.weekView)
			{
				newPage=Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.WEEK_OF_YEAR);
			}
			else
			{
				newPage=Tools.getPage(ctxt.pageIndex,ctxt.stupid.currentDate,Calendar.DAY_OF_YEAR);
			}	
			ctxt.currentPage=ctxt.viewPager.getCurrentItem();
			if(newPage != ctxt.currentPage)//newPage != (parent.currentPage -1) && newPage != (parent.currentPage + 1) &&
			{
	
				if(newPage != 0)		//Dort scheint ein bug zu sein; bei Seite 0 wird onPageselected Event nicht gestartet, daher nicht disablen
					ctxt.disableNextPagerOnChangedEvent=true;
				
				ctxt.currentPage=newPage;
				ctxt.pageAdapter = new MyPagerAdapter(ctxt.pages,ctxt.headlines);
				
				
				ctxt.viewPager.setAdapter(ctxt.pageAdapter);
				ctxt.viewPager.setCurrentItem(ctxt.currentPage,smoothScroll);
		
				ctxt.pageIndicator.setViewPager(ctxt.viewPager);
				ctxt.pageIndicator.notifyDataSetChanged();
				
				ctxt.pageIndicator.setCurrentItem(newPage);
			}
		}
		
	}

}
